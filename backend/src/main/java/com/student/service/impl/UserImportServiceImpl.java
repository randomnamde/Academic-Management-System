package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.student.dto.ImportRoleType;
import com.student.dto.UserImportFailItemDTO;
import com.student.dto.UserImportResultDTO;
import com.student.dto.UserImportSuccessItemDTO;
import com.student.entity.Class;
import com.student.entity.College;
import com.student.entity.Student;
import com.student.entity.SysUser;
import com.student.entity.Teacher;
import com.student.exception.BusinessException;
import com.student.mapper.ClassMapper;
import com.student.mapper.CollegeMapper;
import com.student.mapper.StudentMapper;
import com.student.mapper.SysUserMapper;
import com.student.mapper.TeacherMapper;
import com.student.security.RoleCode;
import com.student.service.StudentService;
import com.student.service.SysUserService;
import com.student.service.UserImportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserImportServiceImpl implements UserImportService {

    private static final int MAX_IMPORT_ROWS = 2000;
    private static final int ACCOUNT_RETRY_LIMIT = 50;
    private static final String REQUIRED_MARK = "\uff08\u5fc5\u586b\uff09";
    private static final String OPTIONAL_MARK = "\uff08\u9009\u586b\uff09";
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-M-d"),
            DateTimeFormatter.ofPattern("yyyy/M/d"),
            DateTimeFormatter.ofPattern("yyyy.MM.dd")
    );
    private static final Map<String, String> HEADER_ALIAS_TO_CANONICAL = buildHeaderAliasMap();

    private final SysUserMapper userMapper;
    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final CollegeMapper collegeMapper;
    private final ClassMapper classMapper;
    private final SysUserService sysUserService;
    private final StudentService studentService;
    private final PasswordEncoder passwordEncoder;
    private final PlatformTransactionManager transactionManager;

    @Value("${initial-password.value:ChangeMe123!}")
    private String defaultPassword;

    private final DataFormatter dataFormatter = new DataFormatter();

    @Override
    public void writeTemplate(ImportRoleType roleType, String fileType, HttpServletResponse response) {
        String normalizedType = normalizeTemplateFileType(fileType);
        List<String> headers = templateHeaders(roleType);
        List<String> sample = templateSample(roleType);
        String datePart = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String baseName = "user-import-" + roleType.name().toLowerCase(Locale.ROOT) + "-" + datePart;
        if ("csv".equals(normalizedType)) {
            writeCsvTemplate(headers, sample, baseName + ".csv", response);
            return;
        }
        writeXlsxTemplate(headers, sample, baseName + ".xlsx", response);
    }

    @Override
    public UserImportResultDTO importUsers(ImportRoleType roleType, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Import file cannot be empty");
        }

        ParsedFile parsedFile = parseUploadFile(file);
        validateRequiredHeaders(roleType, parsedFile.headers());
        if (parsedFile.rows().size() > MAX_IMPORT_ROWS) {
            throw new BusinessException("Single import cannot exceed " + MAX_IMPORT_ROWS + " rows");
        }

        int importYear = Year.now().getValue();
        CollegeCache collegeCache = loadCollegeCache();
        UserImportResultDTO result = new UserImportResultDTO();
        result.setTotalCount(parsedFile.rows().size());

        DefaultTransactionDefinition txDef = new DefaultTransactionDefinition();
        txDef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        for (ImportRow row : parsedFile.rows()) {
            TransactionStatus tx = transactionManager.getTransaction(txDef);
            try {
                UserImportSuccessItemDTO successItem = switch (roleType) {
                    case COLLEGE_ADMIN -> importCollegeAdmin(row, roleType, importYear, collegeCache);
                    case HOMEROOM_TEACHER -> importHomeroomTeacher(row, roleType, importYear, collegeCache);
                    case COURSE_TEACHER -> importCourseTeacher(row, roleType, importYear, collegeCache);
                    case STUDENT -> importStudent(row, roleType, importYear, collegeCache);
                };
                transactionManager.commit(tx);
                result.getSuccessItems().add(successItem);
            } catch (Exception ex) {
                transactionManager.rollback(tx);
                String message = unwrapMessage(ex);
                log.warn("User import row {} failed: {}", row.rowNumber(), message);
                UserImportFailItemDTO failItem = new UserImportFailItemDTO();
                failItem.setRowNumber(row.rowNumber());
                failItem.setRoleType(roleType);
                failItem.setMessage(message);
                failItem.setRowData(row.values());
                result.getFailItems().add(failItem);
            }
        }

        result.setSuccessCount(result.getSuccessItems().size());
        result.setFailedCount(result.getFailItems().size());
        return result;
    }

    private UserImportSuccessItemDTO importCollegeAdmin(ImportRow row,
                                                        ImportRoleType roleType,
                                                        int importYear,
                                                        CollegeCache collegeCache) {
        String collegeCodeRaw = requiredValue(row, "collegeCode");
        String realName = requiredValue(row, "realName");
        int status = parseUserStatus(optionalValue(row, "status"));
        String phone = optionalValue(row, "phone");
        String email = optionalValue(row, "email");

        College college = resolveCollegeByInputCode(collegeCodeRaw, collegeCache);
        if (college.getAdminUserId() != null) {
            throw new BusinessException("College already has an admin bound");
        }

        String normalizedCollegeCode = normalizeCollegeCode(college.getCollegeCode());
        String username = generateUsername(roleType.accountPrefix(), normalizedCollegeCode, importYear);
        SysUser user = createSysUser(username, realName, phone, email, status, SysUser.Role.COLLEGE_ADMIN);
        sysUserService.grantRole(user.getUsername(), RoleCode.COLLEGE_ADMIN);

        College collegePatch = new College();
        collegePatch.setId(college.getId());
        collegePatch.setAdminUserId(user.getUsername());
        collegeMapper.updateById(collegePatch);

        return successRow(row.rowNumber(), roleType, username, realName);
    }

    private UserImportSuccessItemDTO importHomeroomTeacher(ImportRow row,
                                                           ImportRoleType roleType,
                                                           int importYear,
                                                           CollegeCache collegeCache) {
        String collegeCodeRaw = requiredValue(row, "collegeCode");
        String name = requiredValue(row, "name");
        Teacher.Gender gender = parseTeacherGender(requiredValue(row, "gender"));
        int status = parseUserStatus(optionalValue(row, "status"));
        String phone = optionalValue(row, "phone");
        String email = optionalValue(row, "email");
        Teacher.Title title = parseTeacherTitle(optionalValue(row, "title"));
        String department = optionalValue(row, "department");
        LocalDate hireDate = parseDate(optionalValue(row, "hireDate"), "hireDate");
        String classCode = optionalValue(row, "classCode");

        College college = resolveCollegeByInputCode(collegeCodeRaw, collegeCache);
        String normalizedCollegeCode = normalizeCollegeCode(college.getCollegeCode());

        String username = generateUsername(roleType.accountPrefix(), normalizedCollegeCode, importYear);
        SysUser user = createSysUser(username, name, phone, email, status, SysUser.Role.HOMEROOM_TEACHER);
        sysUserService.grantRole(user.getUsername(), RoleCode.HOMEROOM_TEACHER);

        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUsername());
        teacher.setTeacherNo(username);
        teacher.setName(name);
        teacher.setGender(gender);
        teacher.setPhone(phone);
        teacher.setEmail(email);
        teacher.setTitle(title);
        teacher.setDepartment(department);
        teacher.setCollegeCode(college.getCollegeCode());
        teacher.setHireDate(hireDate);
        teacher.setStatus(status);
        teacherMapper.insert(teacher);
        Teacher persistedTeacher = teacherMapper.selectByTeacherNo(teacher.getTeacherNo());

        if (StringUtils.hasText(classCode)) {
            Class clazz = classMapper.selectByClassCode(classCode.trim());
            if (clazz == null) {
                throw new BusinessException("Class not found: " + classCode);
            }
            if (clazz.getCollegeCode() == null || !clazz.getCollegeCode().equals(college.getCollegeCode())) {
                throw new BusinessException("Class does not belong to the selected college: " + classCode);
            }
            if (persistedTeacher == null || persistedTeacher.getTeacherNo() == null) {
                throw new BusinessException("Teacher number not found after import");
            }
            if (clazz.getTeacherNo() != null && !clazz.getTeacherNo().equals(persistedTeacher.getTeacherNo())) {
                throw new BusinessException("Class already has another homeroom teacher bound: " + classCode);
            }
            Class classPatch = new Class();
            classPatch.setId(clazz.getId());
            classPatch.setTeacherNo(persistedTeacher.getTeacherNo());
            classMapper.updateById(classPatch);
        }

        return successRow(row.rowNumber(), roleType, username, name);
    }

    private UserImportSuccessItemDTO importCourseTeacher(ImportRow row,
                                                         ImportRoleType roleType,
                                                         int importYear,
                                                         CollegeCache collegeCache) {
        String collegeCodeRaw = requiredValue(row, "collegeCode");
        String name = requiredValue(row, "name");
        Teacher.Gender gender = parseTeacherGender(requiredValue(row, "gender"));
        int status = parseUserStatus(optionalValue(row, "status"));
        String phone = optionalValue(row, "phone");
        String email = optionalValue(row, "email");
        Teacher.Title title = parseTeacherTitle(optionalValue(row, "title"));
        String department = optionalValue(row, "department");
        LocalDate hireDate = parseDate(optionalValue(row, "hireDate"), "hireDate");

        College college = resolveCollegeByInputCode(collegeCodeRaw, collegeCache);
        String normalizedCollegeCode = normalizeCollegeCode(college.getCollegeCode());

        String username = generateUsername(roleType.accountPrefix(), normalizedCollegeCode, importYear);
        SysUser user = createSysUser(username, name, phone, email, status, SysUser.Role.COURSE_TEACHER);
        sysUserService.grantRole(user.getUsername(), RoleCode.COURSE_TEACHER);

        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUsername());
        teacher.setTeacherNo(username);
        teacher.setName(name);
        teacher.setGender(gender);
        teacher.setPhone(phone);
        teacher.setEmail(email);
        teacher.setTitle(title);
        teacher.setDepartment(department);
        teacher.setCollegeCode(college.getCollegeCode());
        teacher.setHireDate(hireDate);
        teacher.setStatus(status);
        teacherMapper.insert(teacher);

        return successRow(row.rowNumber(), roleType, username, name);
    }

    private UserImportSuccessItemDTO importStudent(ImportRow row,
                                                   ImportRoleType roleType,
                                                   int importYear,
                                                   CollegeCache collegeCache) {
        String classCode = requiredValue(row, "classCode");
        String name = requiredValue(row, "name");
        Student.Gender gender = parseStudentGender(requiredValue(row, "gender"));
        String phone = optionalValue(row, "phone");
        String email = optionalValue(row, "email");
        String idCard = optionalValue(row, "idCard");
        String address = optionalValue(row, "address");
        LocalDate birthday = parseDate(optionalValue(row, "birthday"), "birthday");
        LocalDate enrollmentDate = parseDate(optionalValue(row, "enrollmentDate"), "enrollmentDate");
        LocalDate graduationDate = parseDate(optionalValue(row, "graduationDate"), "graduationDate");
        Student.Status studentStatus = parseStudentStatus(optionalValue(row, "status"));
        int userStatus = Student.Status.DROPPED == studentStatus ? 0 : 1;

        Class clazz = classMapper.selectByClassCode(classCode.trim());
        if (clazz == null) {
            throw new BusinessException("Class not found: " + classCode);
        }
        if (clazz.getCollegeCode() == null) {
            throw new BusinessException("Class has no bound college: " + classCode);
        }
        College college = collegeCache.byId().get(clazz.getCollegeCode());
        if (college == null) {
            throw new BusinessException("College not found for class: " + classCode);
        }
        String username = studentService.generateStudentNo(clazz.getClassCode(), enrollmentDate);
        SysUser user = createSysUser(username, name, phone, email, userStatus, SysUser.Role.STUDENT);
        sysUserService.grantRole(user.getUsername(), RoleCode.STUDENT);

        Student student = new Student();
        student.setUserId(user.getUsername());
        student.setStudentNo(username);
        student.setName(name);
        student.setGender(gender);
        student.setBirthday(birthday);
        student.setIdCard(idCard);
        student.setPhone(phone);
        student.setEmail(email);
        student.setAddress(address);
        student.setClassId(clazz.getClassCode());
        student.setEnrollmentDate(enrollmentDate);
        student.setGraduationDate(graduationDate);
        student.setStatus(studentStatus);
        studentMapper.insert(student);
        refreshClassStudentCount(clazz.getClassCode());

        return successRow(row.rowNumber(), roleType, username, name);
    }

    private ParsedFile parseUploadFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        String lower = fileName == null ? "" : fileName.trim().toLowerCase(Locale.ROOT);
        if (lower.endsWith(".csv")) {
            return parseCsvFile(file);
        }
        if (lower.endsWith(".xlsx")) {
            return parseXlsxFile(file);
        }
        throw new BusinessException("Only .csv and .xlsx files are supported");
    }

    private ParsedFile parseCsvFile(MultipartFile file) {
        try (InputStreamReader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .setIgnoreEmptyLines(true)
                     .build()
                     .parse(reader)) {
            Set<String> headers = new LinkedHashSet<>();
            for (String header : parser.getHeaderMap().keySet()) {
                String normalized = normalizeHeaderKey(header);
                if (StringUtils.hasText(normalized)) {
                    headers.add(normalized);
                }
            }
            if (headers.isEmpty()) {
                throw new BusinessException("Import header cannot be empty");
            }

            List<ImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                Map<String, String> row = new LinkedHashMap<>();
                for (String header : parser.getHeaderMap().keySet()) {
                    row.put(normalizeHeaderKey(header), normalizeCell(record.get(header)));
                }
                if (isBlankRow(row)) {
                    continue;
                }
                rows.add(new ImportRow((int) record.getRecordNumber() + 1, row));
            }
            return new ParsedFile(headers, rows);
        } catch (IOException ex) {
            throw new BusinessException("Failed to parse CSV file");
        }
    }

    private ParsedFile parseXlsxFile(MultipartFile file) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(file.getInputStream())) {
            if (workbook.getNumberOfSheets() == 0) {
                throw new BusinessException("Import file has no worksheet");
            }
            XSSFSheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new BusinessException("Import header cannot be empty");
            }

            Map<Integer, String> headerMap = new LinkedHashMap<>();
            Set<String> headers = new LinkedHashSet<>();
            short lastCellNum = headerRow.getLastCellNum();
            for (int i = 0; i < lastCellNum; i++) {
                String normalized = normalizeHeaderKey(readCell(headerRow, i));
                if (!StringUtils.hasText(normalized)) {
                    continue;
                }
                headerMap.put(i, normalized);
                headers.add(normalized);
            }
            if (headerMap.isEmpty()) {
                throw new BusinessException("Import header cannot be empty");
            }

            List<ImportRow> rows = new ArrayList<>();
            for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row rowData = sheet.getRow(rowIndex);
                if (rowData == null) {
                    continue;
                }
                Map<String, String> row = new LinkedHashMap<>();
                for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
                    row.put(entry.getValue(), normalizeCell(readCell(rowData, entry.getKey())));
                }
                if (isBlankRow(row)) {
                    continue;
                }
                rows.add(new ImportRow(rowIndex + 1, row));
            }
            return new ParsedFile(headers, rows);
        } catch (IOException ex) {
            throw new BusinessException("Failed to parse XLSX file");
        }
    }

    private void validateRequiredHeaders(ImportRoleType roleType, Set<String> headers) {
        List<String> requiredHeaders = switch (roleType) {
            case COLLEGE_ADMIN -> List.of("collegeCode", "realName");
            case HOMEROOM_TEACHER, COURSE_TEACHER -> List.of("collegeCode", "name", "gender");
            case STUDENT -> List.of("classCode", "name", "gender");
        };
        List<String> missing = requiredHeaders.stream()
                .map(this::normalizeHeaderKey)
                .filter(key -> !headers.contains(key))
                .toList();
        if (!missing.isEmpty()) {
            throw new BusinessException("Missing required columns: " + String.join(", ", missing));
        }
    }

    private CollegeCache loadCollegeCache() {
        List<College> colleges = collegeMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, College> byNormalizedCode = new HashMap<>();
        Map<Long, College> byId = new HashMap<>();
        for (College college : colleges) {
            String normalized = normalizeCollegeCode(college.getCollegeCode());
            College existing = byNormalizedCode.putIfAbsent(normalized, college);
            if (existing != null && !existing.getId().equals(college.getId())) {
                throw new BusinessException("Duplicate college code after normalization: " + normalized);
            }
            byId.put(college.getId(), college);
        }
        return new CollegeCache(byNormalizedCode, byId);
    }

    private College resolveCollegeByInputCode(String collegeCodeRaw, CollegeCache collegeCache) {
        String normalized = normalizeCollegeCode(collegeCodeRaw);
        College college = collegeCache.byNormalizedCode().get(normalized);
        if (college == null) {
            throw new BusinessException("College not found: " + collegeCodeRaw);
        }
        return college;
    }

    private SysUser createSysUser(String username,
                                  String realName,
                                  String phone,
                                  String email,
                                  int status,
                                  SysUser.Role role) {
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(defaultPassword));
        user.setRealName(realName);
        user.setPhone(phone);
        user.setEmail(email);
        user.setStatus(status);
        user.setRole(role);
        userMapper.insert(user);
        return user;
    }

    private String generateUsername(char prefix, String normalizedCollegeCode, int year) {
        String base = String.valueOf(prefix) + normalizedCollegeCode + year;
        for (int i = 0; i < ACCOUNT_RETRY_LIMIT; i++) {
            int random = ThreadLocalRandom.current().nextInt(10000);
            String username = base + String.format("%04d", random);
            Long count = userMapper.countByUsername(username);
            if (count == null || count == 0) {
                return username;
            }
        }
        throw new BusinessException("Failed to generate unique account after " + ACCOUNT_RETRY_LIMIT + " retries");
    }

    private String normalizeCollegeCode(String rawCode) {
        if (!StringUtils.hasText(rawCode)) {
            throw new BusinessException("College code cannot be empty");
        }
        String normalized = rawCode.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (!StringUtils.hasText(normalized)) {
            throw new BusinessException("College code cannot be empty");
        }
        if (normalized.length() > 4) {
            normalized = normalized.substring(0, 4);
        }
        if (normalized.length() < 4) {
            normalized = "0".repeat(4 - normalized.length()) + normalized;
        }
        return normalized;
    }

    private int parseUserStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return 1;
        }
        try {
            int value = Integer.parseInt(raw.trim());
            if (value == 0 || value == 1) {
                return value;
            }
        } catch (NumberFormatException ignored) {
        }
        throw new BusinessException("status must be 0 or 1");
    }

    private Teacher.Gender parseTeacherGender(String raw) {
        String normalized = normalizeEnumText(raw);
        return switch (normalized) {
            case "MALE", "M", "\u7537" -> Teacher.Gender.MALE;
            case "FEMALE", "F", "\u5973" -> Teacher.Gender.FEMALE;
            default -> throw new BusinessException("Invalid gender: " + raw);
        };
    }

    private Student.Gender parseStudentGender(String raw) {
        String normalized = normalizeEnumText(raw);
        return switch (normalized) {
            case "MALE", "M", "\u7537" -> Student.Gender.MALE;
            case "FEMALE", "F", "\u5973" -> Student.Gender.FEMALE;
            default -> throw new BusinessException("Invalid gender: " + raw);
        };
    }

    private Teacher.Title parseTeacherTitle(String raw) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        String normalized = normalizeEnumText(raw);
        return switch (normalized) {
            case "LECTURER", "\u8BB2\u5E08" -> Teacher.Title.LECTURER;
            case "ASSOCIATE_PROFESSOR", "\u526F\u6559\u6388" -> Teacher.Title.ASSOCIATE_PROFESSOR;
            case "PROFESSOR", "\u6559\u6388" -> Teacher.Title.PROFESSOR;
            default -> throw new BusinessException("Invalid title: " + raw);
        };
    }

    private Student.Status parseStudentStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            return Student.Status.ENROLLED;
        }
        String normalized = normalizeEnumText(raw);
        return switch (normalized) {
            case "ENROLLED", "\u5728\u8BFB" -> Student.Status.ENROLLED;
            case "GRADUATED", "\u6BD5\u4E1A" -> Student.Status.GRADUATED;
            case "SUSPENDED", "\u4F11\u5B66" -> Student.Status.SUSPENDED;
            case "DROPPED", "\u9000\u5B66" -> Student.Status.DROPPED;
            default -> throw new BusinessException("Invalid student status: " + raw);
        };
    }

    private LocalDate parseDate(String raw, String fieldName) {
        if (!StringUtils.hasText(raw)) {
            return null;
        }
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(raw.trim(), formatter);
            } catch (DateTimeParseException ignored) {
            }
        }
        throw new BusinessException("Invalid date format for " + fieldName + ": " + raw);
    }

    private void refreshClassStudentCount(String classId) {
        if (classId == null) {
            return;
        }
        Long count = studentMapper.countByClassId(classId);
        Class existing = classMapper.selectByClassCode(classId);
        if (existing == null) {
            return;
        }
        Class classPatch = new Class();
        classPatch.setId(existing.getId());
        classPatch.setStudentCount(count == null ? 0 : count.intValue());
        classMapper.updateById(classPatch);
    }

    private String normalizeTemplateFileType(String fileType) {
        String normalized = fileType == null ? "xlsx" : fileType.trim().toLowerCase(Locale.ROOT);
        if (!Arrays.asList("xlsx", "csv").contains(normalized)) {
            throw new BusinessException("fileType must be csv or xlsx");
        }
        return normalized;
    }

    private List<String> templateHeaders(ImportRoleType roleType) {
        return switch (roleType) {
            case COLLEGE_ADMIN -> List.of(
                    requiredHeader("\u5b66\u9662\u7f16\u7801"),
                    requiredHeader("\u59d3\u540d"),
                    optionalHeader("\u624b\u673a\u53f7"),
                    optionalHeader("\u90ae\u7bb1"),
                    optionalHeader("\u72b6\u6001"));
            case HOMEROOM_TEACHER -> List.of(
                    requiredHeader("\u5b66\u9662\u7f16\u7801"),
                    requiredHeader("\u59d3\u540d"),
                    requiredHeader("\u6027\u522b"),
                    optionalHeader("\u624b\u673a\u53f7"),
                    optionalHeader("\u90ae\u7bb1"),
                    optionalHeader("\u804c\u79f0"),
                    optionalHeader("\u9662\u7cfb"),
                    optionalHeader("\u5165\u804c\u65e5\u671f"),
                    optionalHeader("\u73ed\u7ea7\u7f16\u7801"),
                    optionalHeader("\u72b6\u6001"));
            case COURSE_TEACHER -> List.of(
                    requiredHeader("\u5b66\u9662\u7f16\u7801"),
                    requiredHeader("\u59d3\u540d"),
                    requiredHeader("\u6027\u522b"),
                    optionalHeader("\u624b\u673a\u53f7"),
                    optionalHeader("\u90ae\u7bb1"),
                    optionalHeader("\u804c\u79f0"),
                    optionalHeader("\u9662\u7cfb"),
                    optionalHeader("\u5165\u804c\u65e5\u671f"),
                    optionalHeader("\u72b6\u6001"));
            case STUDENT -> List.of(
                    requiredHeader("\u73ed\u7ea7\u7f16\u7801"),
                    requiredHeader("\u59d3\u540d"),
                    requiredHeader("\u6027\u522b"),
                    optionalHeader("\u624b\u673a\u53f7"),
                    optionalHeader("\u90ae\u7bb1"),
                    optionalHeader("\u8eab\u4efd\u8bc1\u53f7"),
                    optionalHeader("\u5730\u5740"),
                    optionalHeader("\u51fa\u751f\u65e5\u671f"),
                    optionalHeader("\u5165\u5b66\u65e5\u671f"),
                    optionalHeader("\u6bd5\u4e1a\u65e5\u671f"),
                    optionalHeader("\u72b6\u6001"));
        };
    }

    private String requiredHeader(String base) {
        return base + REQUIRED_MARK;
    }

    private String optionalHeader(String base) {
        return base + OPTIONAL_MARK;
    }
    private List<String> templateSample(ImportRoleType roleType) {
        return switch (roleType) {
            case COLLEGE_ADMIN -> List.of("CS", "College Admin", "13800000000", "admin@example.com", "1");
            case HOMEROOM_TEACHER -> List.of(
                    "CS", "Homeroom Teacher", "MALE", "13800000001", "homeroom@example.com", "LECTURER", "Computer Science",
                    "2023-09-01", "SE2301", "1");
            case COURSE_TEACHER -> List.of(
                    "CS", "Course Teacher", "FEMALE", "13800000002", "course@example.com", "ASSOCIATE_PROFESSOR",
                    "Computer Science", "2022-09-01", "1");
            case STUDENT -> List.of(
                    "SE2301", "Student Demo", "MALE", "13800000003", "student@example.com", "110101200501010011",
                    "Demo Street", "2005-01-01", "2023-09-01", "2027-06-30", "ENROLLED");
        };
    }

    private void writeCsvTemplate(List<String> headers, List<String> sample, String fileName, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", contentDisposition(fileName));
        try (BufferedWriter writer = new BufferedWriter(response.getWriter())) {
            writer.write('\uFEFF');
            writer.write(toCsvLine(headers));
            writer.newLine();
            writer.write(toCsvLine(sample));
            writer.flush();
        } catch (IOException ex) {
            throw new BusinessException("Failed to write CSV template");
        }
    }

    private void writeXlsxTemplate(List<String> headers, List<String> sample, String fileName, HttpServletResponse response) {
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", contentDisposition(fileName));
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            XSSFSheet sheet = workbook.createSheet("template");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.createCell(i).setCellValue(headers.get(i));
                sheet.setColumnWidth(i, 18 * 256);
            }
            Row sampleRow = sheet.createRow(1);
            for (int i = 0; i < sample.size(); i++) {
                sampleRow.createCell(i).setCellValue(sample.get(i));
            }
            workbook.write(response.getOutputStream());
        } catch (IOException ex) {
            throw new BusinessException("Failed to write XLSX template");
        }
    }

    private String contentDisposition(String fileName) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"" + fileName + "\"; filename*=UTF-8''" + encoded;
    }

    private String toCsvLine(List<String> values) {
        return values.stream().map(this::csvEscape).reduce((a, b) -> a + "," + b).orElse("");
    }

    private String csvEscape(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n") || safe.contains("\r")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private UserImportSuccessItemDTO successRow(int rowNumber, ImportRoleType roleType, String username, String realName) {
        UserImportSuccessItemDTO item = new UserImportSuccessItemDTO();
        item.setRowNumber(rowNumber);
        item.setRoleType(roleType);
        item.setUsername(username);
        item.setRealName(realName);
        return item;
    }

    private String requiredValue(ImportRow row, String key) {
        String value = row.values().get(normalizeHeaderKey(key));
        if (!StringUtils.hasText(value)) {
            throw new BusinessException("Missing required field: " + key);
        }
        return value.trim();
    }

    private String optionalValue(ImportRow row, String key) {
        String value = row.values().get(normalizeHeaderKey(key));
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private String unwrapMessage(Exception ex) {
        Throwable current = ex;
        while (current != null) {
            if (current instanceof BusinessException businessException) {
                return businessException.getMessage();
            }
            current = current.getCause();
        }
        return StringUtils.hasText(ex.getMessage()) ? ex.getMessage() : "Import failed";
    }

    private String normalizeHeaderKey(String raw) {
        if (raw == null) {
            return "";
        }
        String cleaned = raw.replace("\uFEFF", "").trim();
        if (!StringUtils.hasText(cleaned)) {
            return "";
        }
        String aliasKey = normalizeHeaderAlias(cleaned);
        return HEADER_ALIAS_TO_CANONICAL.getOrDefault(aliasKey, cleaned);
    }

    private String normalizeCell(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.replace("\uFEFF", "").trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String readCell(Row row, int cellIndex) {
        if (row == null || row.getCell(cellIndex) == null) {
            return null;
        }
        return dataFormatter.formatCellValue(row.getCell(cellIndex));
    }

    private boolean isBlankRow(Map<String, String> row) {
        for (String value : row.values()) {
            if (StringUtils.hasText(value)) {
                return false;
            }
        }
        return true;
    }

    private String normalizeEnumText(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private static Map<String, String> buildHeaderAliasMap() {
        Map<String, String> map = new HashMap<>();
        alias(map, "collegeCode", "collegeCode", "\u5b66\u9662\u7f16\u7801", "\u5b66\u9662\u4ee3\u7801", "college_code");
        alias(map, "realName", "realName", "\u771f\u5b9e\u59d3\u540d", "\u59d3\u540d", "real_name");
        alias(map, "name", "name", "\u59d3\u540d");
        alias(map, "gender", "gender", "\u6027\u522b");
        alias(map, "phone", "phone", "\u624b\u673a\u53f7", "\u624b\u673a", "\u7535\u8bdd");
        alias(map, "email", "email", "\u90ae\u7bb1", "\u7535\u5b50\u90ae\u7bb1");
        alias(map, "title", "title", "\u804c\u79f0");
        alias(map, "department", "department", "\u9662\u7cfb", "\u90e8\u95e8");
        alias(map, "hireDate", "hireDate", "\u5165\u804c\u65e5\u671f", "\u8058\u7528\u65e5\u671f", "hire_date");
        alias(map, "classCode", "classCode", "\u73ed\u7ea7\u7f16\u7801", "\u73ed\u7ea7\u4ee3\u7801", "class_code");
        alias(map, "status", "status", "\u72b6\u6001");
        alias(map, "idCard", "idCard", "\u8eab\u4efd\u8bc1\u53f7", "\u8eab\u4efd\u8bc1", "id_card");
        alias(map, "address", "address", "\u5730\u5740", "\u4f4f\u5740");
        alias(map, "birthday", "birthday", "\u51fa\u751f\u65e5\u671f", "\u751f\u65e5");
        alias(map, "enrollmentDate", "enrollmentDate", "\u5165\u5b66\u65e5\u671f", "enrollment_date");
        alias(map, "graduationDate", "graduationDate", "\u6bd5\u4e1a\u65e5\u671f", "graduation_date");
        return map;
    }
    private static void alias(Map<String, String> map, String canonical, String... aliases) {
        for (String value : aliases) {
            map.put(normalizeHeaderAlias(value), canonical);
        }
    }

    private static String normalizeHeaderAlias(String raw) {
        return raw
                .replace("\uFEFF", "")
                .trim()
                .replace("\uff08\u5fc5\u586b\uff09", "")
                .replace("\uff08\u9009\u586b\uff09", "")
                .replace("(\u5fc5\u586b)", "")
                .replace("(\u9009\u586b)", "")
                .replace("[\u5fc5\u586b]", "")
                .replace("[\u9009\u586b]", "")
                .replace("_", "")
                .replace("-", "")
                .replace(" ", "")
                .toLowerCase(Locale.ROOT);
    }

    private record ImportRow(int rowNumber, Map<String, String> values) {
    }

    private record ParsedFile(Set<String> headers, List<ImportRow> rows) {
    }

    private record CollegeCache(Map<String, College> byNormalizedCode, Map<Long, College> byId) {
    }
}



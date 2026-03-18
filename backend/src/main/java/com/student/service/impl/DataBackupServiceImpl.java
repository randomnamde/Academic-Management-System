package com.student.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.student.entity.DataBackup;
import com.student.entity.DataRestore;
import com.student.mapper.DataBackupMapper;
import com.student.mapper.DataRestoreMapper;
import com.student.service.DataBackupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class DataBackupServiceImpl extends ServiceImpl<DataBackupMapper, DataBackup> implements DataBackupService {

    @Value("${app.backup.path:./backups}")
    private String backupPath;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataRestoreMapper dataRestoreMapper;

    @Override
    @Transactional
    public DataBackup createBackup(String backupName, String backupType, String operatorNo) {
        DataBackup backup = new DataBackup();
        backup.setBackupName(backupName);
        backup.setBackupType(backupType);
        backup.setStatus("RUNNING");
        backup.setStartTime(LocalDateTime.now());
        backup.setOperatorNo(operatorNo);
        save(backup);

        try {
            // Get all tables
            List<String> tables = getAllTables();
            backup.setTableCount(tables.size());

            // Create backup directory
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            String backupDir = backupPath + File.separator + "backup_" + timestamp;
            Files.createDirectories(Paths.get(backupDir));

            // Export each table
            for (String table : tables) {
                exportTable(table, backupDir);
            }

            // Create ZIP file
            String zipFileName = backupDir + ".zip";
            createZip(backupDir, zipFileName);

            // Clean up temp directory
            deleteDirectory(Paths.get(backupDir));

            // Update backup record
            Path zipPath = Paths.get(zipFileName);
            long fileSize = Files.size(zipPath);

            backup.setFilePath(zipFileName);
            backup.setFileSize(fileSize);
            backup.setStatus("SUCCESS");
            backup.setEndTime(LocalDateTime.now());
            updateById(backup);

        } catch (Exception e) {
            backup.setStatus("FAILED");
            backup.setEndTime(LocalDateTime.now());
            backup.setErrorMessage(e.getMessage());
            updateById(backup);
        }

        return backup;
    }

    @Override
    @Transactional
    public boolean restoreFromBackup(Long backupId, String operatorNo) {
        DataBackup backup = getById(backupId);
        if (backup == null || !"SUCCESS".equals(backup.getStatus())) {
            return false;
        }

        DataRestore restore = new DataRestore();
        restore.setBackupId(backupId);
        restore.setRestoreName("Restore_" + System.currentTimeMillis());
        restore.setBackupPath(backup.getFilePath());
        restore.setStatus("RUNNING");
        restore.setStartTime(LocalDateTime.now());
        restore.setOperatorNo(operatorNo);
        dataRestoreMapper.insert(restore);

        try {
            // Extract ZIP file
            String extractDir = backupPath + File.separator + "restore_" + System.currentTimeMillis();
            extractZip(backup.getFilePath(), extractDir);

            // Get table files
            File dir = new File(extractDir);
            File[] files = dir.listFiles((d, name) -> name.endsWith(".sql"));

            if (files != null) {
                for (File sqlFile : files) {
                    String sql = Files.readString(sqlFile.toPath());
                    jdbcTemplate.execute(sql);
                }
                restore.setTablesRestored(files.length);
            }

            restore.setStatus("SUCCESS");
            restore.setEndTime(LocalDateTime.now());
            dataRestoreMapper.updateById(restore);

            // Clean up
            deleteDirectory(Paths.get(extractDir));

            return true;
        } catch (Exception e) {
            restore.setStatus("FAILED");
            restore.setEndTime(LocalDateTime.now());
            restore.setErrorMessage(e.getMessage());
            dataRestoreMapper.updateById(restore);
            return false;
        }
    }

    private List<String> getAllTables() {
        return jdbcTemplate.queryForList("SHOW TABLES", String.class);
    }

    private void exportTable(String tableName, String backupDir) throws IOException {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT * FROM " + tableName);
        if (rows.isEmpty()) return;

        StringBuilder sql = new StringBuilder();
        sql.append("-- Table: ").append(tableName).append("\n\n");

        for (Map<String, Object> row : rows) {
            List<String> columns = new ArrayList<>(row.keySet());
            List<String> values = new ArrayList<>();

            for (Object value : row.values()) {
                if (value == null) {
                    values.add("NULL");
                } else if (value instanceof Number) {
                    values.add(value.toString());
                } else {
                    values.add("'" + value.toString().replace("'", "''") + "'");
                }
            }

            sql.append("INSERT INTO ").append(tableName)
               .append(" (").append(String.join(", ", columns)).append(")")
               .append(" VALUES (").append(String.join(", ", values)).append(");\n");
        }

        Files.writeString(Paths.get(backupDir, tableName + ".sql"), sql.toString());
    }

    private void createZip(String sourceDir, String zipFileName) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFileName))) {
            Path sourcePath = Paths.get(sourceDir);
            Files.walk(sourcePath)
                .filter(path -> !Files.isDirectory(path))
                .forEach(path -> {
                    try {
                        String relativePath = sourcePath.relativize(path).toString();
                        zos.putNextEntry(new ZipEntry(relativePath));
                        Files.copy(path, zos);
                        zos.closeEntry();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        }
    }

    private void extractZip(String zipFileName, String extractDir) throws IOException {
        java.util.zip.ZipFile zipFile = new java.util.zip.ZipFile(zipFileName);
        Files.createDirectories(Paths.get(extractDir));

        zipFile.entries().asIterator().forEachRemaining(entry -> {
            try {
                Path entryPath = Paths.get(extractDir, entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream is = zipFile.getInputStream(entry);
                         OutputStream os = Files.newOutputStream(entryPath)) {
                        is.transferTo(os);
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        zipFile.close();
    }

    private void deleteDirectory(Path path) throws IOException {
        if (Files.exists(path)) {
            Files.walk(path)
                .sorted(java.util.Comparator.reverseOrder())
                .forEach(p -> {
                    try {
                        Files.delete(p);
                    } catch (IOException e) {
                        // ignore
                    }
                });
        }
    }
}

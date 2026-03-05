package com.student.controller;

import com.student.dto.ImportRoleType;
import com.student.dto.UserImportResultDTO;
import com.student.service.UserImportService;
import com.student.vo.ResultVO;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user/import")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SCHOOL_ADMIN')")
public class UserImportController {

    private final UserImportService userImportService;

    @GetMapping("/template")
    public void downloadTemplate(@RequestParam ImportRoleType roleType,
                                 @RequestParam(defaultValue = "xlsx") String fileType,
                                 HttpServletResponse response) {
        userImportService.writeTemplate(roleType, fileType, response);
    }

    @PostMapping
    public ResultVO<UserImportResultDTO> importUsers(@RequestParam ImportRoleType roleType,
                                                     @RequestPart("file") MultipartFile file) {
        return ResultVO.success(userImportService.importUsers(roleType, file));
    }
}

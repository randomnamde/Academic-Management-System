package com.student.service;

import com.student.dto.ImportRoleType;
import com.student.dto.UserImportResultDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

public interface UserImportService {

    void writeTemplate(ImportRoleType roleType, String fileType, HttpServletResponse response);

    UserImportResultDTO importUsers(ImportRoleType roleType, MultipartFile file);
}

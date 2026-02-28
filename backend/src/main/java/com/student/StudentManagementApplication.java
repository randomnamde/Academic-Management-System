package com.student;

import com.student.config.StartupResultListener;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan("com.student.mapper")
public class StudentManagementApplication {
    
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(StudentManagementApplication.class);
        application.addListeners(new StartupResultListener());
        application.run(args);
    }
}

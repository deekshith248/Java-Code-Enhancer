package com.codeenhancer.java_code_enhancer.service.impl;

import com.codeenhancer.java_code_enhancer.dto.ValidationResponse;
//import com.github.javaparser.StaticJavaParser;
import com.codeenhancer.java_code_enhancer.service.CodeValidationService;
import com.github.javaparser.StaticJavaParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class CodeValidationServiceImpl implements CodeValidationService {

    @Override
    public ValidationResponse validateTextInput(String code) {
        try {
            // Attempt to parse the code
            StaticJavaParser.parse(code);
            return ValidationResponse.builder()
                    .valid(true)
                    .message("Valid Java code")
                    .build();
        } catch (Exception e) {
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Invalid Java syntax: " + e.getMessage().split("Problem stacktrace :")[0].trim())
                    .build();
        }
    }

    @Override
    public ValidationResponse validateFileInput(MultipartFile file) {
        // Check if file is empty
        if (file.isEmpty()) {
            return ValidationResponse.builder()
                    .valid(false)
                    .message("File is empty")
                    .build();
        }

        // Check file extension
        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".java")) {
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Invalid file type. Only .java files are allowed")
                    .fileName(fileName)
                    .build();
        }

        // Validate file content
        try {
            String content = new String(file.getBytes());
            ValidationResponse textValidation = validateTextInput(content);

            return ValidationResponse.builder()
                    .valid(textValidation.isValid())
                    .message(textValidation.getMessage())
                    .fileName(fileName)
                    .build();
        } catch (IOException e) {
            return ValidationResponse.builder()
                    .valid(false)
                    .message("Error reading file: " + e.getMessage().split("Problem stacktrace :")[0].trim())
                    .fileName(fileName)
                    .build();
        }
    }
}
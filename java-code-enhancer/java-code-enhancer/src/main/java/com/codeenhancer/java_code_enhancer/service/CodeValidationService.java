package com.codeenhancer.java_code_enhancer.service;

import com.codeenhancer.java_code_enhancer.dto.ValidationResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CodeValidationService {
    ValidationResponse validateTextInput(String code);

    ValidationResponse validateFileInput(MultipartFile file);
}

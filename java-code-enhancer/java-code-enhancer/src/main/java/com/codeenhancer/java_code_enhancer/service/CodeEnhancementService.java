package com.codeenhancer.java_code_enhancer.service;

import com.codeenhancer.java_code_enhancer.dto.EnhancementResponse;
import org.springframework.web.multipart.MultipartFile;

public interface CodeEnhancementService {
    EnhancementResponse enhanceTextCode(String code);

    EnhancementResponse enhanceFileCode(MultipartFile file);
}
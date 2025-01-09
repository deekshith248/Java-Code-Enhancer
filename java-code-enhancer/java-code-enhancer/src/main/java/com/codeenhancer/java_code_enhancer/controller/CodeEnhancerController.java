package com.codeenhancer.java_code_enhancer.controller;

import com.codeenhancer.java_code_enhancer.dto.EnhancementResponse;
import com.codeenhancer.java_code_enhancer.dto.ValidationResponse;
import com.codeenhancer.java_code_enhancer.service.CodeEnhancementService;
import com.codeenhancer.java_code_enhancer.service.CodeValidationService;

import java.util.Map;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/code-enhancer")
@RequiredArgsConstructor
public class CodeEnhancerController {
    private final CodeValidationService codeValidationService;
    private final CodeEnhancementService codeEnhancementService;

    @PostMapping("/validate/text")
    public ValidationResponse validateText(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        return codeValidationService.validateTextInput(code);
    }


    @PostMapping("/validate/file")
    public ValidationResponse validateFile(@RequestParam("file") MultipartFile file) {
        return codeValidationService.validateFileInput(file);
    }

    @PostMapping("/enhance/text")
    public EnhancementResponse enhanceText(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        return codeEnhancementService.enhanceTextCode(code);
    }

    @PostMapping("/enhance/file")
    public EnhancementResponse enhanceFile(@RequestParam("file") MultipartFile file) {
        return codeEnhancementService.enhanceFileCode(file);
    }
}
package com.codeenhancer.java_code_enhancer.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ValidationResponse {
    private boolean valid;
    private String message;
    private String fileName;
}
package com.codeenhancer.java_code_enhancer.dto;

import com.codeenhancer.java_code_enhancer.util.CodeMetrics;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EnhancementResponse {
    private String originalCode;
    private String enhancedCode;
    private CodeMetrics codeMetrics;
    private String[] warnings;
}
package com.codeenhancer.java_code_enhancer.service.impl;

import com.codeenhancer.java_code_enhancer.dto.EnhancementResponse;
import com.codeenhancer.java_code_enhancer.service.CodeEnhancementService;
import com.codeenhancer.java_code_enhancer.util.CodeMetrics;
import com.codeenhancer.java_code_enhancer.util.CodeTransformationUtils;
import com.github.javaparser.StaticJavaParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
public class CodeEnhancementServiceImpl implements CodeEnhancementService {

    @Autowired
    DashboardServiceImpl dashboardService;

    @Override
    public EnhancementResponse enhanceTextCode(String code) {
        try {
            var cu = StaticJavaParser.parse(code);

            CodeMetrics codeMetrics = dashboardService.analyzeCode(code);

            String enhancedCode = CodeTransformationUtils.transformToLatestVersion(cu);

            String[] warnings = CodeTransformationUtils.detectWarnings(cu);

            return EnhancementResponse.builder()
                    .originalCode(code)
                    .enhancedCode(enhancedCode)
                    .warnings(warnings)
                    .codeMetrics(codeMetrics)
                    .build();
        } catch (Exception e) {
            return EnhancementResponse.builder()
                    .originalCode(code)
                    .enhancedCode(null)
                    .warnings(new String[]{"Error during enhancement: " + e.getMessage().split("Problem stacktrace :")[0].trim()})
                    .codeMetrics(null)
                    .build();
        }
    }

    @Override
    public EnhancementResponse enhanceFileCode(MultipartFile file) {
        try {
            String content = new String(file.getBytes());
            return enhanceTextCode(content);
        } catch (IOException e) {
            return EnhancementResponse.builder()
                    .originalCode(null)
                    .enhancedCode(null)
                    .warnings(new String[]{"Error reading file: " + e.getMessage().split("Problem stacktrace :")[0].trim()})
                    .build();
        }
    }
}

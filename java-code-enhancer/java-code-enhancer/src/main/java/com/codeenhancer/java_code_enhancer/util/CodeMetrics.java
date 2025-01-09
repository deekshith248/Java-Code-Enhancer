package com.codeenhancer.java_code_enhancer.util;


public class CodeMetrics {
    private int totalLinesOfCode;
    private int cyclomaticComplexity;
    private int deprecatedApiCount;
    private int modernizationOpportunities;

    // Getters and Setters

    public int getTotalLinesOfCode() {
        return totalLinesOfCode;
    }

    public void setTotalLinesOfCode(int totalLinesOfCode) {
        this.totalLinesOfCode = totalLinesOfCode;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getDeprecatedApiCount() {
        return deprecatedApiCount;
    }

    public void setDeprecatedApiCount(int deprecatedApiCount) {
        this.deprecatedApiCount = deprecatedApiCount;
    }

    public int getModernizationOpportunities() {
        return modernizationOpportunities;
    }

    public void setModernizationOpportunities(int modernizationOpportunities) {
        this.modernizationOpportunities = modernizationOpportunities;
    }
}
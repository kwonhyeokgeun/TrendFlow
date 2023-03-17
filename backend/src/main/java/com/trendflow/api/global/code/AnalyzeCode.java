package com.trendflow.api.global.code;

public enum AnalyzeCode implements BasicCode {
    FAIL("410", "분석 기능 오류");

    private String code;
    private String message;

    @Override
    public String getCode() { return this.code; }
    @Override
    public String getMessage() { return this.message; }

    AnalyzeCode(String code, String message){
        this.code = code;
        this.message = message;
    }
}

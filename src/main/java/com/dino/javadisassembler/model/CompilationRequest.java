package com.dino.javadisassembler.model;

public class CompilationRequest {
    private String sourceCode;
    private String className;

    // Default constructor for deserialization
    public CompilationRequest() {
    }

    public CompilationRequest(String sourceCode, String className) {
        this.sourceCode = sourceCode;
        this.className = className;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}

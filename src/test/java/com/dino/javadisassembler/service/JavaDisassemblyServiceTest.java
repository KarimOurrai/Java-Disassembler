package com.dino.javadisassembler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class JavaDisassemblyServiceTest {

    private JavaDisassemblyService disassemblyService;
    private static final String TEST_CLASS_NAME = "TestClass";
    private static final String SIMPLE_CLASS = 
        "public class TestClass {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Hello, World!\");\n" +
        "    }\n" +
        "}";

    @BeforeEach
    void setUp() {
        disassemblyService = new JavaDisassemblyService();
    }

    @Test
    void getBytecode_ShouldReturnBytecodeForValidJavaCode() throws Exception {
        String result = disassemblyService.getBytecode(SIMPLE_CLASS, TEST_CLASS_NAME);
        
        assertNotNull(result);
        assertTrue(result.contains("public static void main(java.lang.String[])"));
        assertTrue(result.contains("getstatic"));
        assertTrue(result.contains("invokevirtual"));
    }

    @Test
    void getBytecode_ShouldThrowExceptionForInvalidJavaCode() {
        String invalidCode = "invalid java code";
        
        Exception exception = assertThrows(Exception.class, () -> {
            disassemblyService.getBytecode(invalidCode, TEST_CLASS_NAME);
        });
        
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }

    @Test
    void getJitAssembly_ShouldReturnAssemblyOrHsdisMessage() throws Exception {
        String result = disassemblyService.getJitAssembly(SIMPLE_CLASS, TEST_CLASS_NAME);
        
        assertNotNull(result);
        assertTrue(
            result.contains("Assembly") || 
            result.contains("hsdis") ||
            result.contains("PrintAssembly")
        );
    }

    @Test
    void getAotAssembly_ShouldReturnAssemblyForValidJavaCode() throws Exception {
        // Skip if native-image is not available
        if (!isNativeImageAvailable()) {
            return;
        }

        String result = disassemblyService.getAotAssembly(SIMPLE_CLASS, TEST_CLASS_NAME);
        
        assertNotNull(result);
        assertTrue(result.contains("main:") || result.contains("_main:"));
    }

    @Test
    void getAotAssembly_ShouldThrowExceptionForInvalidJavaCode() {
        // Skip if native-image is not available
        if (!isNativeImageAvailable()) {
            return;
        }

        String invalidCode = "invalid java code";
        
        Exception exception = assertThrows(Exception.class, () -> {
            disassemblyService.getAotAssembly(invalidCode, TEST_CLASS_NAME);
        });
        
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }

    private boolean isNativeImageAvailable() {
        try {
            Process process = new ProcessBuilder("native-image", "--version")
                .redirectErrorStream(true)
                .start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

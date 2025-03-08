package com.dino.javadisassembler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JavaDisassemblyServiceTest {

    @InjectMocks
    private JavaDisassemblyService disassemblyService;
    private static final String TEST_CLASS_NAME = "TestClass";
    private static final String SIMPLE_CLASS = """
                               class TestClass {
                                  static void main(String[] args) {
                                       System.out.println("Hello");
                                  }
                               }""";

    @BeforeEach
    void setUp() {
        disassemblyService = new JavaDisassemblyService();
    }

    @Test
    void getBytecode_ValidCode_ShouldReturnBytecode() throws Exception {
        String sourceCode = """
                          class Test {
                             static void main(String[] args) {
                                  System.out.println("Hello");
                             }
                          }""";
        String result = disassemblyService.getBytecode(sourceCode, "Test");
        assertNotNull(result);
        assertTrue(result.contains("static void main(java.lang.String[])"));
    }

    @Test
    void getBytecode_InvalidCode_ShouldThrowException() {
        String sourceCode = "invalid code";
        Exception exception = assertThrows(Exception.class, () -> 
            disassemblyService.getBytecode(sourceCode, "Test")
        );
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }

    @Test
    void getJitAssembly_ValidCode_ShouldReturnAssembly() throws Exception {
        String sourceCode = """
                          class Test {
                             static void main(String[] args) {
                                  System.out.println("Hello");
                             }
                          }""";
        String result = disassemblyService.getJitAssembly(sourceCode, "Test");
        assertNotNull(result);
    }

    @Test
    void getBytecode_ShouldReturnBytecodeForValidJavaCode() throws Exception {
        String result = disassemblyService.getBytecode(SIMPLE_CLASS, TEST_CLASS_NAME);
        
        assertNotNull(result);
        assertTrue(result.contains("static void main(java.lang.String[])"));
        assertTrue(result.contains("getstatic"));
        assertTrue(result.contains("invokevirtual"));
    }

    @Test
    void getBytecode_ShouldThrowExceptionForInvalidJavaCode() {
        String invalidCode = "invalid java code";
        
        Exception exception = assertThrows(Exception.class, () -> disassemblyService
                .getBytecode(invalidCode, TEST_CLASS_NAME));
        
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
    void getAotAssembly_ShouldReturnAssemblyForValidJavaCode() {
        // Skip if native-image is not available
        if (isNoNativeImageAvailable()) {
            return;
        }

        String result = disassemblyService.getAotAssembly(SIMPLE_CLASS, TEST_CLASS_NAME);
        
        assertNotNull(result);
        assertTrue(result.contains("main:") || result.contains("_main:"));
    }

    @Test
    void getAotAssembly_ShouldThrowExceptionForInvalidJavaCode() {
        // Skip if native-image is not available
        if (isNoNativeImageAvailable()) {
            return;
        }

        String invalidCode = "invalid java code";
        
        Exception exception = assertThrows(Exception.class, () -> disassemblyService
                .getAotAssembly(invalidCode, TEST_CLASS_NAME));
        
        assertTrue(exception.getMessage().contains("Compilation failed"));
    }

    private boolean isNoNativeImageAvailable() {
        try {
            Process process = new ProcessBuilder("native-image", "--version")
                .redirectErrorStream(true)
                .start();
            return process.waitFor() != 0;
        } catch (Exception e) {
            return true;
        }
    }
}

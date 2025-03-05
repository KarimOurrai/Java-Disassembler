package com.dino.javadisassembler.controller;

import com.dino.javadisassembler.config.TestSecurityConfig;
import com.dino.javadisassembler.model.CompilationRequest;
import com.dino.javadisassembler.service.JavaDisassemblyService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DisassemblyController.class)
@Import(TestSecurityConfig.class)
public class DisassemblyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JavaDisassemblyService disassemblyService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void getBytecode_ShouldReturnSuccess() throws Exception {
        String testCode = "public class Test { }";
        String expectedOutput = "Compiled from \"Test.java\"";
        when(disassemblyService.getBytecode(anyString(), anyString()))
            .thenReturn(expectedOutput);

        CompilationRequest request = new CompilationRequest();
        request.setSourceCode(testCode);
        request.setClassName("Test");

        mockMvc.perform(post("/api/disassemble/bytecode")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").value(expectedOutput));
    }

    @Test
    public void getBytecode_ShouldHandleError() throws Exception {
        when(disassemblyService.getBytecode(anyString(), anyString()))
            .thenThrow(new RuntimeException("Compilation failed"));

        CompilationRequest request = new CompilationRequest();
        request.setSourceCode("testCode");
        request.setClassName("Test");

        mockMvc.perform(post("/api/disassemble/bytecode")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Compilation failed"));
    }

    @Test
    public void getJitAssembly_ShouldReturnSuccess() throws Exception {
        String testCode = "public class Test { }";
        String expectedOutput = "Assembly output";
        when(disassemblyService.getJitAssembly(anyString(), anyString()))
            .thenReturn(expectedOutput);

        CompilationRequest request = new CompilationRequest();
        request.setSourceCode(testCode);
        request.setClassName("Test");

        mockMvc.perform(post("/api/disassemble/jit")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.result").value(expectedOutput));
    }
}

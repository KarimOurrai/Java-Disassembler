package com.dino.javadisassembler.controller;

import com.dino.javadisassembler.service.JavaDisassemblyService;
import com.dino.javadisassembler.model.CompilationRequest;
import com.dino.javadisassembler.model.CompilationResponse;
import com.dino.javadisassembler.util.InputSanitizer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/disassemble")
public class DisassemblyController {

    private static final Logger logger = LoggerFactory.getLogger(DisassemblyController.class);
    private final JavaDisassemblyService disassemblyService;

    public DisassemblyController(JavaDisassemblyService disassemblyService) {
        this.disassemblyService = disassemblyService;
    }

    @PostMapping("/bytecode")
    public ResponseEntity<CompilationResponse> getBytecode(@RequestBody CompilationRequest request) {
        String sanitizedClassName = InputSanitizer.sanitizeClassName(request.getClassName());
        String sanitizedSourceCode = InputSanitizer.sanitizeSourceCode(request.getSourceCode());
        logger.info("Received bytecode disassembly request for class: {}", sanitizedSourceCode);
        try {
            String result = disassemblyService.getBytecode(
                    sanitizedSourceCode,
                    sanitizedClassName
            );
            logger.info("Successfully processed bytecode request for class: {}", 
                sanitizedClassName);
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            logger.error("Error processing bytecode request for class {}: {}", 
                sanitizedClassName, 
                InputSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/jit")
    public ResponseEntity<CompilationResponse> getJitAssembly(@RequestBody CompilationRequest request) {
        String sanitizedClassName = InputSanitizer.sanitizeClassName(request.getClassName());
        String sanitizedSourceCode = InputSanitizer.sanitizeSourceCode(request.getSourceCode());
        logger.info("Received JIT assembly request for class: {}", 
            sanitizedClassName);
        try {
            String result = disassemblyService.getJitAssembly(
                    sanitizedSourceCode,
                    sanitizedClassName
            );
            logger.info("Successfully processed JIT assembly request for class: {}", 
                sanitizedClassName);
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Interrupted JIT assembly request for class {}: {}",
                sanitizedClassName,
                InputSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error processing JIT assembly request for class {}: {}", 
                sanitizedClassName, 
                InputSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/aot")
    public ResponseEntity<CompilationResponse> getAotAssembly(@RequestBody CompilationRequest request) {
        String sanitizedClassName = InputSanitizer.sanitizeClassName(request.getClassName());
        String sanitizedSourceCode = InputSanitizer.sanitizeSourceCode(request.getSourceCode());
        logger.info("Received AOT assembly request for class: {}", 
            sanitizedClassName);
        try {
            String result = disassemblyService.getAotAssembly(
                    sanitizedSourceCode,
                    sanitizedClassName
            );
            logger.info("Successfully processed AOT assembly request for class: {}", 
                sanitizedClassName);
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            logger.error("Error processing AOT assembly request for class {}: {}", 
                sanitizedClassName, 
                InputSanitizer.sanitizeForLog(e.getMessage()), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }
}
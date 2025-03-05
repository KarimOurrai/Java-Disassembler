package com.dino.javadisassembler.controler;

import com.dino.javadisassembler.service.JavaDisassemblyService;
import com.dino.javadisassembler.model.CompilationRequest;
import com.dino.javadisassembler.model.CompilationResponse;
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
        logger.info("Received bytecode disassembly request for class: {}", request.getClassName());
        try {
            String result = disassemblyService.getBytecode(
                    request.getSourceCode(),
                    request.getClassName()
            );
            logger.info("Successfully processed bytecode request for class: {}", request.getClassName());
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            logger.error("Error processing bytecode request for class {}: {}", 
                request.getClassName(), e.getMessage(), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/jit")
    public ResponseEntity<CompilationResponse> getJitAssembly(@RequestBody CompilationRequest request) {
        logger.info("Received JIT assembly request for class: {}", request.getClassName());
        try {
            String result = disassemblyService.getJitAssembly(
                    request.getSourceCode(),
                    request.getClassName()
            );
            logger.info("Successfully processed JIT assembly request for class: {}", request.getClassName());
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            logger.error("Error processing JIT assembly request for class {}: {}", 
                request.getClassName(), e.getMessage(), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/aot")
    public ResponseEntity<CompilationResponse> getAotAssembly(@RequestBody CompilationRequest request) {
        logger.info("Received AOT assembly request for class: {}", request.getClassName());
        try {
            String result = disassemblyService.getAotAssembly(
                    request.getSourceCode(),
                    request.getClassName()
            );
            logger.info("Successfully processed AOT assembly request for class: {}", request.getClassName());
            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            logger.error("Error processing AOT assembly request for class {}: {}", 
                request.getClassName(), e.getMessage(), e);
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }
}
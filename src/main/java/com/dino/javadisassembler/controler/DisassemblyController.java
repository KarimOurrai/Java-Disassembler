package com.dino.javadisassembler.controler;

import com.dino.javadisassembler.service.JavaDisassemblyService;
import com.dino.javadisassembler.model.CompilationRequest;
import com.dino.javadisassembler.model.CompilationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disassemble")
public class DisassemblyController {

    private final JavaDisassemblyService disassemblyService;

    @Autowired
    public DisassemblyController(JavaDisassemblyService disassemblyService) {
        this.disassemblyService = disassemblyService;
    }

    @PostMapping("/bytecode")
    public ResponseEntity<CompilationResponse> getBytecode(@RequestBody CompilationRequest request) {
        try {
            String result = disassemblyService.getBytecode(
                    request.getSourceCode(),
                    request.getClassName()
            );

            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/jit")
    public ResponseEntity<CompilationResponse> getJitAssembly(@RequestBody CompilationRequest request) {
        try {
            String result = disassemblyService.getJitAssembly(
                    request.getSourceCode(),
                    request.getClassName()
            );

            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }

    @PostMapping("/aot")
    public ResponseEntity<CompilationResponse> getAotAssembly(@RequestBody CompilationRequest request) {
        try {
            String result = disassemblyService.getAotAssembly(
                    request.getSourceCode(),
                    request.getClassName()
            );

            return ResponseEntity.ok(new CompilationResponse(true, result, null));
        } catch (Exception e) {
            return ResponseEntity.ok(new CompilationResponse(false, null, e.getMessage()));
        }
    }
}
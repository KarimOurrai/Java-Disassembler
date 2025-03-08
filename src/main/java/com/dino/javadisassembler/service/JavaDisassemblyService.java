package com.dino.javadisassembler.service;

import com.dino.javadisassembler.exception.CompilationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JavaDisassemblyService {

    private static final Logger logger = LoggerFactory.getLogger(JavaDisassemblyService.class);
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Compiles Java source code to bytecode and returns the bytecode disassembly
     */
    public String getBytecode(String sourceCode, String className) throws CompilationException, IOException {
        logger.info("Starting bytecode disassembly for class: {}", className);
        // Create a unique working directory
        String workingDirName = UUID.randomUUID().toString();
        File workingDir = new File(TEMP_DIR, workingDirName);
        workingDir.mkdir();
        logger.debug("Created working directory: {}", workingDir);

        try {
            // Write source to file
            File sourceFile = new File(workingDir, className + ".java");
            logger.debug("Writing source code to file: {}", sourceFile);
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compile the Java file
            logger.debug("Compiling Java file: {}", sourceFile);
            boolean compiled = compileJavaFile(sourceFile);
            if (!compiled) {
                logger.error("Compilation failed for class: {}", className);
                throw new CompilationException("Compilation failed");
            }
            logger.info("Successfully compiled class: {}", className);

            // Get bytecode using javap
            return getBytecodeDisassembly(workingDir, className);
        } catch (InterruptedException e) {
            logger.error("Interuption {}:", className);
            Thread.currentThread().interrupt();
            throw new CompilationException(e);
        }catch (Exception e) {
            logger.error("Error during bytecode disassembly for class {}:", className);
            throw new CompilationException(e);
        } finally {
            // Clean up
            deleteDirectory(workingDir);
            logger.debug("Cleaned up working directory: {}", workingDir);
        }
    }

    /**
     * Removed AOT assembly functionality for now
     */
    public String getAotAssembly(String sourceCode, String className) {
        return """
               AOT assembly currently disabled.
               We are working on improving this feature.
               Please try the bytecode or JIT assembly views instead.""" + " \n" +sourceCode + "\n" + className;
    }

    /**
     * Returns JIT compiler output using -XX:+PrintAssembly
     */
    public String getJitAssembly(String sourceCode, String className) throws CompilationException, IOException, InterruptedException {
        logger.info("Starting JIT assembly for class: {}", className);
        // Create a unique working directory
        String workingDirName = UUID.randomUUID().toString();
        File workingDir = new File(TEMP_DIR, workingDirName);
        workingDir.mkdir();
        logger.debug("Created working directory: {}", workingDir);

        try {
            // Write source to file
            File sourceFile = new File(workingDir, className + ".java");
            logger.debug("Writing source code to file: {}", sourceFile);
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compile the Java file
            logger.debug("Compiling Java file: {}", sourceFile);
            boolean compiled = compileJavaFile(sourceFile);
            if (!compiled) {
                logger.error("Compilation failed for class: {}", className);
                throw new CompilationException("Compilation failed");
            }
            logger.info("Successfully compiled class: {}", className);

            // Get JIT assembly using hsdis and PrintAssembly
            return getJitAssemblyOutput(workingDir, className);
        } catch (Exception e) {
            logger.error("Error during JIT assembly for class {}", className);
            throw e;
        } finally {
            // Clean up
            deleteDirectory(workingDir);
            logger.debug("Cleaned up working directory: {}", workingDir);
        }
    }

    private boolean compileJavaFile(File sourceFile) throws IOException, InterruptedException {
        logger.debug("Starting compilation of file: {}", sourceFile);
        ProcessBuilder processBuilder = new ProcessBuilder("javac", sourceFile.getAbsolutePath());
        Process process = processBuilder.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            logger.warn("Compilation timed out for file: {}", sourceFile);
            process.destroyForcibly();
            throw new InterruptedException("Compilation timed out");
        }

        boolean success = process.exitValue() == 0;
        logger.debug("Compilation finished with status: {}", success);
        return success;
    }

    private String getBytecodeDisassembly(File workingDir, String className) throws IOException, CompilationException, InterruptedException {
        logger.debug("Starting bytecode disassembly for class: {}", className);
        ProcessBuilder processBuilder = new ProcessBuilder(
                "javap", "-c", "-verbose", "-p", className
        );
        processBuilder.directory(workingDir);

        Process process = processBuilder.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            logger.warn("Disassembly timed out for class: {}", className);
            process.destroyForcibly();
            throw new CompilationException("Disassembly timed out");
        }

        return new String(process.getInputStream().readAllBytes());
    }

    private String getJitAssemblyOutput(File workingDir, String className) throws IOException, InterruptedException {
        logger.debug("Starting JIT assembly output for class: {}", className);
        // This requires hsdis (HotSpot Disassembler) plugin to be installed
        ProcessBuilder processBuilder = new ProcessBuilder(
                "java",
                "-XX:+UnlockDiagnosticVMOptions",
                "-XX:+PrintAssembly",
                "-XX:CompileOnly=" + className + "::*",
                className
        );
        processBuilder.directory(workingDir);
        processBuilder.redirectErrorStream(true); // Merge stderr and stdout

        Process process = processBuilder.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            logger.warn("JIT disassembly timed out for class: {}", className);
            process.destroyForcibly();
            throw new InterruptedException("JIT disassembly timed out");
        }

        String output = new String(process.getInputStream().readAllBytes());
        
        // Check if the output contains actual assembly or just the warning message
        if (output.contains("Could not load hsdis") || 
            (!output.contains("<nmethod>") && !output.contains("Assembly") && 
             output.contains("PrintAssembly is enabled"))) {
            return "HotSpot Disassembler (hsdis) plugin is not installed.\n\n" +
                   "To use JIT assembly view, you need to install the hsdis plugin for your JVM:\n\n" +
                   "1. Download the appropriate hsdis plugin for your platform from:\n" +
                   "   https://github.com/openjdk/jdk/tree/master/src/utils/hsdis\n\n" +
                   "2. Place the library file (hsdis-<arch>.so or hsdis-<arch>.dll) in your JRE's lib directory:\n" +
                   "   - For Linux/Mac: $JAVA_HOME/lib/\n" +
                   "   - For Windows: $JAVA_HOME\\lib\\\n\n" +
                   "Original output:\n" + output;
        }
        
        return output;
    }

    private void deleteDirectory(File directory) throws IOException {
        logger.debug("Deleting directory: {}", directory);
        try (var dir = Files.walk(directory.toPath())) {
                dir.sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
             }
    }
}
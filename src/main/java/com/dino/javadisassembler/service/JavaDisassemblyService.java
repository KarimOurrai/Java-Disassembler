package com.dino.javadisassembler.service;

import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class JavaDisassemblyService {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final int TIMEOUT_SECONDS = 10;

    /**
     * Compiles Java source code to bytecode and returns the bytecode disassembly
     */
    public String getBytecode(String sourceCode, String className) throws Exception {
        // Create a unique working directory
        String workingDirName = UUID.randomUUID().toString();
        File workingDir = new File(TEMP_DIR, workingDirName);
        workingDir.mkdir();

        try {
            // Write source to file
            File sourceFile = new File(workingDir, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compile the Java file
            boolean compiled = compileJavaFile(sourceFile);
            if (!compiled) {
                throw new Exception("Compilation failed");
            }

            // Get bytecode using javap
            return getBytecodeDisassembly(workingDir, className);
        } finally {
            // Clean up
            deleteDirectory(workingDir);
        }
    }

    /**
     * Compiles and returns the AOT assembly for the given Java code
     */
    public String getAotAssembly(String sourceCode, String className) throws Exception {
        // Create a unique working directory
        String workingDirName = UUID.randomUUID().toString();
        File workingDir = new File(TEMP_DIR, workingDirName);
        workingDir.mkdir();

        try {
            // Write source to file
            File sourceFile = new File(workingDir, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compile the Java file
            boolean compiled = compileJavaFile(sourceFile);
            if (!compiled) {
                throw new Exception("Compilation failed");
            }

            // Use GraalVM native-image to get assembly
            return getAotAssemblyOutput(workingDir, className);
        } finally {
            // Clean up
            deleteDirectory(workingDir);
        }
    }

    /**
     * Returns JIT compiler output using -XX:+PrintAssembly
     */
    public String getJitAssembly(String sourceCode, String className) throws Exception {
        // Create a unique working directory
        String workingDirName = UUID.randomUUID().toString();
        File workingDir = new File(TEMP_DIR, workingDirName);
        workingDir.mkdir();

        try {
            // Write source to file
            File sourceFile = new File(workingDir, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }

            // Compile the Java file
            boolean compiled = compileJavaFile(sourceFile);
            if (!compiled) {
                throw new Exception("Compilation failed");
            }

            // Get JIT assembly using hsdis and PrintAssembly
            return getJitAssemblyOutput(workingDir, className);
        } finally {
            // Clean up
            deleteDirectory(workingDir);
        }
    }

    private boolean compileJavaFile(File sourceFile) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("javac", sourceFile.getAbsolutePath());
        Process process = processBuilder.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new InterruptedException("Compilation timed out");
        }

        return process.exitValue() == 0;
    }

    private String getBytecodeDisassembly(File workingDir, String className) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(
                "javap", "-c", "-verbose", "-p", className
        );
        processBuilder.directory(workingDir);

        Process process = processBuilder.start();
        boolean completed = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            process.destroyForcibly();
            throw new InterruptedException("Disassembly timed out");
        }

        return new String(process.getInputStream().readAllBytes());
    }

    private String getJitAssemblyOutput(File workingDir, String className) throws IOException, InterruptedException {
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

    private String getAotAssemblyOutput(File workingDir, String className) throws IOException, InterruptedException {
        // This requires GraalVM to be installed
        // First compile to native using native-image
        ProcessBuilder nativeImageBuilder = new ProcessBuilder(
                "native-image", "--no-fallback", className
        );
        nativeImageBuilder.directory(workingDir);

        Process nativeProcess = nativeImageBuilder.start();
        boolean completed = nativeProcess.waitFor(60, TimeUnit.SECONDS); // AOT compilation may take longer

        if (!completed) {
            nativeProcess.destroyForcibly();
            throw new InterruptedException("AOT compilation timed out");
        }

        if (nativeProcess.exitValue() != 0) {
            throw new IOException("AOT compilation failed: " +
                    new String(nativeProcess.getErrorStream().readAllBytes()));
        }

        // Now use objdump to get the assembly
        File executableFile = new File(workingDir, className);
        ProcessBuilder objdumpBuilder = new ProcessBuilder(
                "objdump", "-d", executableFile.getAbsolutePath()
        );

        Process objdumpProcess = objdumpBuilder.start();
        completed = objdumpProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        if (!completed) {
            objdumpProcess.destroyForcibly();
            throw new InterruptedException("Disassembly timed out");
        }

        return new String(objdumpProcess.getInputStream().readAllBytes());
    }

    private void deleteDirectory(File directory) throws IOException {
        Files.walk(directory.toPath())
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
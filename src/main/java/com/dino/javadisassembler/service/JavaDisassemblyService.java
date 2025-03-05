package com.dino.javadisassembler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
    public String getBytecode(String sourceCode, String className) throws Exception {
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
                throw new Exception("Compilation failed");
            }
            logger.info("Successfully compiled class: {}", className);

            // Get bytecode using javap
            return getBytecodeDisassembly(workingDir, className);
        } catch (Exception e) {
            logger.error("Error during bytecode disassembly for class {}: {}", className, e.getMessage(), e);
            throw e;
        } finally {
            // Clean up
            deleteDirectory(workingDir);
            logger.debug("Cleaned up working directory: {}", workingDir);
        }
    }

    /**
     * Compiles and returns the AOT assembly for the given Java code
     */
    public String getAotAssembly(String sourceCode, String className) throws Exception {
        logger.info("Starting AOT assembly for class: {}", className);
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
                throw new Exception("Compilation failed");
            }
            logger.info("Successfully compiled class: {}", className);

            // Use GraalVM native-image to get assembly
            return getAotAssemblyOutput(workingDir, className);
        } catch (Exception e) {
            logger.error("Error during AOT assembly for class {}: {}", className, e.getMessage(), e);
            throw e;
        } finally {
            // Clean up
            deleteDirectory(workingDir);
            logger.debug("Cleaned up working directory: {}", workingDir);
        }
    }

    /**
     * Returns JIT compiler output using -XX:+PrintAssembly
     */
    public String getJitAssembly(String sourceCode, String className) throws Exception {
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
                throw new Exception("Compilation failed");
            }
            logger.info("Successfully compiled class: {}", className);

            // Get JIT assembly using hsdis and PrintAssembly
            return getJitAssemblyOutput(workingDir, className);
        } catch (Exception e) {
            logger.error("Error during JIT assembly for class {}: {}", className, e.getMessage(), e);
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

    private String getBytecodeDisassembly(File workingDir, String className) throws IOException, InterruptedException {
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
            throw new InterruptedException("Disassembly timed out");
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

    private String getAotAssemblyOutput(File workingDir, String className) throws IOException, InterruptedException {
        logger.debug("Starting AOT assembly output for class: {} in directory: {}", className, workingDir);
        
        File classFile = new File(workingDir, className + ".class");
        File outputFile = new File(workingDir, className);
        
        if (!classFile.exists()) {
            logger.error("Class file not found at: {}", classFile.getAbsolutePath());
            throw new IOException("Class file not found");
        }
        
        // First compile with native-image
        ProcessBuilder nativeImageBuilder = new ProcessBuilder(
                "native-image",
                "--no-fallback",
                "-H:+ReportExceptionStackTraces",
                "-H:Name=" + outputFile.getAbsolutePath(),
                "-cp", workingDir.getAbsolutePath(),
                "--initialize-at-build-time=" + className,
                className
        );
        nativeImageBuilder.directory(workingDir);
        nativeImageBuilder.redirectErrorStream(true);

        logger.debug("Native image command: {}", String.join(" ", nativeImageBuilder.command()));
        Process nativeProcess = nativeImageBuilder.start();
        boolean completed = nativeProcess.waitFor(60, TimeUnit.SECONDS);

        String nativeOutput = new String(nativeProcess.getInputStream().readAllBytes());
        if (!completed || nativeProcess.exitValue() != 0 || !outputFile.exists()) {
            logger.error("Native image compilation failed: {}", nativeOutput);
            throw new IOException("Native image compilation failed:\n" + nativeOutput);
        }

        // Ensure file is executable
        outputFile.setExecutable(true);
        logger.debug("Generated native file at: {} (exists: {}, executable: {})", 
            outputFile.getAbsolutePath(), outputFile.exists(), outputFile.canExecute());

        // Try different objdump commands in order of preference
        String[] objdumpCommands = {
            "objdump",                    // Standard path
            "/usr/bin/objdump",          // Common Linux path
            "gobjdump",                  // MacOS (from binutils)
            "/usr/local/opt/binutils/bin/objdump"  // MacOS Homebrew path
        };

        Process objdumpProcess = null;
        String result = null;

        for (String objdumpCmd : objdumpCommands) {
            try {
                ProcessBuilder objdumpBuilder = new ProcessBuilder(
                        objdumpCmd,
                        "-d",                    // Disassemble
                        "-C",                    // Demangle names
                        "-M", "intel",          // Intel syntax
                        "--no-show-raw-insn",   // Don't show raw bytes
                        outputFile.getAbsolutePath()
                );
                objdumpBuilder.directory(workingDir);
                objdumpBuilder.redirectErrorStream(true);

                logger.debug("Trying objdump command: {}", String.join(" ", objdumpBuilder.command()));
                objdumpProcess = objdumpBuilder.start();
                completed = objdumpProcess.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
                
                result = new String(objdumpProcess.getInputStream().readAllBytes());
                String error = new String(objdumpProcess.getErrorStream().readAllBytes());
                
                logger.debug("objdump exit code: {}, output length: {}, error: {}", 
                    objdumpProcess.exitValue(), result.length(), error);
                
                if (completed && objdumpProcess.exitValue() == 0 && !result.trim().isEmpty()) {
                    break; // Found working objdump
                }
            } catch (Exception e) {
                logger.debug("Failed to execute {}: {}", objdumpCmd, e.getMessage());
                continue;
            }
        }

        if (result == null || result.trim().isEmpty()) {
            logger.error("All objdump attempts failed for file: {}", outputFile.getAbsolutePath());
            throw new IOException("Could not disassemble the native executable. Make sure objdump (binutils) is installed.");
        }

        // Filter output to show only relevant methods
        StringBuilder filtered = new StringBuilder();
        String[] lines = result.split("\n");
        boolean inRelevantSection = false;
        
        // Build regex pattern for method detection
        String methodPattern = String.format("^[0-9a-f]+\\s+<%s[^>]*>:", className);
        
        for (String line : lines) {
            // Skip empty lines and debug sections
            if (line.trim().isEmpty() || line.contains(".debug_")) {
                continue;
            }

            // Detect start of method
            if (line.matches(methodPattern) || line.contains(" main>:")) {
                inRelevantSection = true;
                filtered.append("\n").append(line).append("\n");
                continue;
            }
            
            // Inside relevant section
            if (inRelevantSection) {
                // Detect end of method
                if (line.matches("^[0-9a-f]+\\s+<[^>]+>:")) {
                    inRelevantSection = false;
                    filtered.append("\n");
                } else {
                    String trimmed = line.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith(".") && trimmed.contains("\t")) {
                        filtered.append("  ").append(trimmed.replaceAll("\\s+", " ")).append("\n");
                    }
                }
            }
        }

        String filteredOutput = filtered.toString().trim();
        if (filteredOutput.isEmpty()) {
            logger.warn("No relevant assembly sections found for class: {}", className);
            return "No relevant assembly sections found in the output. This might happen if:\n" +
                   "1. The methods were inlined\n" +
                   "2. The code was optimized away\n" +
                   "3. The methods have different names in the native image\n\n" +
                   "Try adding more complex logic to prevent optimization.";
        }

        return filteredOutput;
    }

    private void deleteDirectory(File directory) throws IOException {
        logger.debug("Deleting directory: {}", directory);
        Files.walk(directory.toPath())
                .sorted((p1, p2) -> -p1.compareTo(p2))
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
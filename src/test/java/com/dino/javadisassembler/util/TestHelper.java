package com.dino.javadisassembler.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestHelper {
    
    public static void cleanDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            try (Stream<Path> walk = Files.walk(directory)) {
                walk.sorted((a, b) -> -a.compareTo(b))
                    .map(Path::toFile)
                    .forEach(File::delete);
            }
        }
    }

    public static boolean isProcessAvailable(String... command) {
        try {
            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}

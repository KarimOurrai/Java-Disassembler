package com.dino.javadisassembler.util;

import org.apache.commons.text.StringEscapeUtils;

public class InputSanitizer {
    public static String sanitizeClassName(String className) {
        if (className == null) {
            return "";
        }
        return className.replaceAll("[^a-zA-Z0-9.$_]", "");
    }

    public static String sanitizeForLog(String input) {
        if (input == null) {
            return "";
        }
        return StringEscapeUtils.escapeJava(input);
    }

    public static String sanitizeSourceCode(String sourceCode) {
        if (sourceCode == null) {
            return "";
        }
        // Remove any null bytes and other potentially harmful characters
        return sourceCode.replaceAll("\\x00", "")
                        .replaceAll("\u0000", "")
                        .trim();
    }
}

package com.vihu.edi;

import com.vihu.edi.exception.EdiException;
import com.vihu.edi.exception.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public class Main {
    private static final String BOM = "\uFEFF";
    private static final String ISA_TAG = "ISA";
    private static final int ISA_LENGTH = 106;

    public static void main(String[] args) {

        try {
            String content = readFile(Path.of("po_850.edi"));
            Separators separators = validateIsa(content);
        } catch (EdiException e) {
            System.err.println(e.getErrorCode() + ": " + e.getMessage());
            System.exit(e.getErrorCode().getExitCode());
        }
    }

    private static String readFile(Path path) throws EdiException {
        try {
            return stripBOM(Files.readString(path));
        } catch (NoSuchFileException e) {
            throw new EdiException(ErrorCode.FILE_NOT_FOUND, "File not found: " + path.toAbsolutePath(), e);
        } catch (IOException e) {
            throw new EdiException(ErrorCode.FILE_READ_ERROR, "Can't read file: " + path.toAbsolutePath(), e);
        }
    }

    private static String stripBOM(String content) {
        return content.startsWith(BOM) ? content.substring(1) : content;
    }

    private static Separators validateIsa(String content) throws EdiException {
        if (content.isEmpty()) {
            throw new EdiException(ErrorCode.NOT_X12, "File is empty.");
        }
        if (!content.startsWith(ISA_TAG)) {
            throw new EdiException(ErrorCode.NOT_X12, "Not an X12 file: content must start with " + ISA_TAG + " segment, found '" + content.substring(0, Math.min(3, content.length())) + "'");
        }
        if (content.length() < ISA_LENGTH) {
            throw new EdiException(ErrorCode.INVALID_ISA, "File too short to contain ISA segment: expected at least " + ISA_LENGTH + " characters, found " + content.length());
        }
        Separators sep = Separators.fromIsa(content);
        if (!sep.areDistinct()) {
            throw new EdiException(ErrorCode.INVALID_ISA, "All three separators must be distinct, found: " + sep.describe());
        }
        if (!sep.areSpecialCharacters()) {
            throw new EdiException(ErrorCode.INVALID_ISA, "Separators must not be letters or digits, found: " + sep.describe());
        }
        return sep;
    }
}

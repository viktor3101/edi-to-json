package com.vihu.edi;

import com.vihu.edi.exception.EdiException;
import com.vihu.edi.exception.ErrorCode;
import com.vihu.edi.model.Element;
import com.vihu.edi.model.Segment;
import com.vihu.edi.model.Separators;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
    private static final String BOM = "\uFEFF";
    private static final String ISA_TAG = "ISA";

    public static void main(String[] args) {
        try {
            String content = readFile(Path.of("po_850.edi"));
            Separators separators = validateIsa(content);
            List<Segment> segments = parseSegments(content, separators);

            System.out.println("Segments: " + segments.size());
            System.out.println(segments.get(0));
            System.out.println(segments.get(5));
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
            throw new EdiException(ErrorCode.NOT_X12, "Not an X12 file: content must start with " + ISA_TAG + " segment, found '"
                    + content.substring(0, Math.min(ISA_TAG.length(), content.length())) + "'");
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

    private static List<Segment> parseSegments(String content, Separators separators) {
        return splitSegments(content, separators).stream()
                .map(raw -> parseSegment(raw, separators))
                .toList();
    }

    private static List<String> splitSegments(String content, Separators separators) {
        List<String> segments = new ArrayList<>();
        for (String part : splitBy(content, separators.segment())) {
            String cleaned = part.replace("\r", "").replace("\n", "");
            if (!cleaned.isEmpty()) {
                segments.add(cleaned);
            }
        }
        return segments;
    }

    private static Segment parseSegment(String raw, Separators separators) {
        String[] parts = splitBy(raw, separators.element());
        String segmentId = parts[0];

        List<Element> elements = new ArrayList<>();
        for (int i = 1; i < parts.length; i++) {
            List<String> components = segmentId.equals(ISA_TAG)
                    ? List.of(parts[i])
                    : List.of(splitBy(parts[i], separators.component()));
            elements.add(new Element(i, components));
        }
        return new Segment(segmentId, elements);
    }

    private static String[] splitBy(String text, char separator) {
        return text.split(Pattern.quote(String.valueOf(separator)), -1);
    }
}

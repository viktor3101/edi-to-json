package com.vihu.edi;

import com.vihu.edi.exception.EdiException;
import com.vihu.edi.exception.ErrorCode;

import java.util.Set;

public record Separators(char element, char component, char segment) {

    private static final int ELEMENT_POS = 3;
    private static final int ISA_ELEMENT_COUNT = 16;
    private static final int ISA_MAX_LENGTH = 106;
    private static final int LAST_SEPARATOR_POS = ISA_MAX_LENGTH - 3; // 103
    private static final int MAX_TAG_LENGTH = 3;
    private static final Set<String> ALLOWED_AFTER_ISA = Set.of("GS", "TA1", "IEA");

    public boolean areDistinct() {
        return element != component && element != segment && component != segment;
    }

    public boolean areSpecialCharacters() {
        return !Character.isLetterOrDigit(element)
                && !Character.isLetterOrDigit(component)
                && !Character.isLetterOrDigit(segment);
    }

    public String describe() {
        return "element '" + element + "', segment '" + segment + "', component '" + component + "'";
    }

    public static Separators fromIsa(String content) throws EdiException {
        // Крок 1
        if (content.length() <= ELEMENT_POS) {
            throw new EdiException(ErrorCode.INVALID_ISA,
                    "ISA is too short to contain an element separator");
        }
        char element = content.charAt(ELEMENT_POS);

        // Крок 2
        int lastPos = Math.min(LAST_SEPARATOR_POS, content.length() - 3);
        int separatorCount = 0;

        for (int i = ELEMENT_POS; i <= lastPos; i++) {
            if (content.charAt(i) == element) {
                separatorCount++;
                if (separatorCount == ISA_ELEMENT_COUNT) {
                    char component = content.charAt(i + 1);
                    char segment = content.charAt(i + 2);
                    // Крок 3
                    checkSegmentAfterIsa(content, i + 3, element);
                    return new Separators(element, component, segment);
                }
            }
        }

        throw new EdiException(ErrorCode.INVALID_ISA,
                "ISA must contain " + ISA_ELEMENT_COUNT + " element separators within the first "
                        + ISA_MAX_LENGTH + " characters, found " + separatorCount);
    }

    private static void checkSegmentAfterIsa(String content, int start, char element) throws EdiException {
        int pos = start;
        while (pos < content.length() && (content.charAt(pos) == '\r' || content.charAt(pos) == '\n')) {
            pos++;
        }

        int tagEnd = content.indexOf(element, pos);
        if (tagEnd == -1 || tagEnd - pos > MAX_TAG_LENGTH) {
            String found = content.substring(pos, Math.min(pos + MAX_TAG_LENGTH, content.length()));
            throw new EdiException(ErrorCode.INVALID_ISA,
                    "Segment after ISA must be one of " + ALLOWED_AFTER_ISA + ", found '" + found + "'");
        }

        String tag = content.substring(pos, tagEnd);
        if (!ALLOWED_AFTER_ISA.contains(tag)) {
            throw new EdiException(ErrorCode.INVALID_ISA,
                    "Segment after ISA must be one of " + ALLOWED_AFTER_ISA + ", found '" + tag + "'");
        }
    }
}

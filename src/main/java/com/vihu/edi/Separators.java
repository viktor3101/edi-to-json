package com.vihu.edi;

public record Separators(char element, char component, char segment) {

    private static final int ELEMENT_POS = 3;
    private static final int COMPONENT_POS = 104;
    private static final int SEGMENT_POS = 105;

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

    public static Separators fromIsa(String content) {
        return new Separators(content.charAt(ELEMENT_POS), content.charAt(COMPONENT_POS), content.charAt(SEGMENT_POS));
    }
}

package com.vihu.edi.model;

import java.util.List;

public record Segment(String segmentId, List<Element> elements) {
    public Segment {
        elements = List.copyOf(elements);
    }

    public String value(int position) {
        return position <= elements.size() ? elements.get(position - 1).value() : "";
    }
}

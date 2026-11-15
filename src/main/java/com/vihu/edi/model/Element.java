package com.vihu.edi.model;

import java.util.List;

public record Element(int position, List<String> components) {

    public Element {
        components = List.copyOf(components);
    }

    public boolean isComposite() {
        return components.size() > 1;
    }

    public String value() {
        return components.getFirst();
    }

    public String value(int i) {
        return components.get(i);
    }
}

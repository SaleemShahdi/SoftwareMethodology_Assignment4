package com.example.android61;

import java.io.Serializable;

public class Tag implements Serializable {
    String name;
    String value;

    public Tag(String name, String value) {
        this.name = name;
        this.value = value;
    }

    // get name of tag
    public String getName() {
        return name;
    }

    // get value of tag
    public String getValue() {
        return value;
    }
}

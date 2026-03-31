package com.selling.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusEnum {
    ACTIVE,
    INACTIVE;

    @JsonValue
    public String getValue() {
        return this.name().toLowerCase();
    }

    @JsonCreator
    public static StatusEnum fromValue(String value) {
        return StatusEnum.valueOf(value.toUpperCase());
    }
}

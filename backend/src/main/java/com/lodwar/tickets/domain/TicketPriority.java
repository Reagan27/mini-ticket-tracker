package com.lodwar.tickets.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;


public enum TicketPriority {
    LOW, MEDIUM, HIGH;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TicketPriority fromValue(String value) {
        return Arrays.stream(values())
                .filter(p -> p.value().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid priority '" + value + "'. Allowed: low, medium, high"));
    }
}

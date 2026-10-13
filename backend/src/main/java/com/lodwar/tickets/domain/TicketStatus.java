package com.lodwar.tickets.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;


public enum TicketStatus {
    OPEN, IN_PROGRESS, CLOSED;

    @JsonValue
    public String value() {
        return name().toLowerCase();
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TicketStatus fromValue(String value) {
        return Arrays.stream(values())
                .filter(s -> s.value().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid status '" + value + "'. Allowed: open, in_progress, closed"));
    }
}

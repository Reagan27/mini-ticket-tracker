package com.lodwar.tickets.dto;

import com.lodwar.tickets.domain.Ticket;
import com.lodwar.tickets.domain.TicketPriority;
import com.lodwar.tickets.domain.TicketStatus;

import java.time.Instant;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String title,
        String description,
        TicketStatus status,
        TicketPriority priority,
        Instant createdAt
) {
    public static TicketResponse from(Ticket t) {
        return new TicketResponse(t.getId(), t.getTitle(), t.getDescription(),
                t.getStatus(), t.getPriority(), t.getCreatedAt());
    }
}

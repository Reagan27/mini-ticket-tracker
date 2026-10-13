package com.lodwar.tickets.dto;

import com.lodwar.tickets.domain.TicketPriority;
import com.lodwar.tickets.domain.TicketStatus;

public record UpdateTicketRequest(TicketStatus status, TicketPriority priority) {}

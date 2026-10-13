package com.lodwar.tickets.web;

import com.lodwar.tickets.domain.TicketStatus;
import com.lodwar.tickets.dto.*;
import com.lodwar.tickets.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request) {
        return service.create(request);
    }

    @GetMapping
    public PageResponse<TicketResponse> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        TicketStatus filter = status == null || status.isBlank() ? null : TicketStatus.fromValue(status);
        return service.list(filter, page, limit);
    }

    @PatchMapping("/{id}")
    public TicketResponse update(@PathVariable UUID id, @RequestBody UpdateTicketRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/stats")
    public Map<String, Long> stats() {
        return service.stats();
    }
}

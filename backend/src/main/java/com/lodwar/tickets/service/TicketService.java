package com.lodwar.tickets.service;

import com.lodwar.tickets.domain.Ticket;
import com.lodwar.tickets.domain.TicketPriority;
import com.lodwar.tickets.domain.TicketStatus;
import com.lodwar.tickets.dto.*;
import com.lodwar.tickets.repository.TicketRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TicketService {

    static final int MAX_LIMIT = 100;

    private final TicketRepository repository;

    public TicketService(TicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest req) {
        TicketPriority priority = req.priority() != null ? req.priority() : TicketPriority.MEDIUM;
        String description = req.description() == null || req.description().isBlank()
                ? null : req.description().trim();
        Ticket saved = repository.save(new Ticket(req.title().trim(), description, priority));
        return TicketResponse.from(saved);
    }

    public PageResponse<TicketResponse> list(TicketStatus status, int page, int limit) {
        if (page < 1) throw new IllegalArgumentException("page must be >= 1");
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new IllegalArgumentException("limit must be between 1 and " + MAX_LIMIT);
        }
    
        PageRequest pageable = PageRequest.of(page - 1, limit,
                Sort.by(Sort.Order.desc("createdAt"), Sort.Order.asc("id")));
        Page<Ticket> result = status == null
                ? repository.findAll(pageable)
                : repository.findByStatus(status, pageable);
        return PageResponse.of(result.map(TicketResponse::from));
    }

    @Transactional
    public TicketResponse update(UUID id, UpdateTicketRequest req) {
        if (req.status() == null && req.priority() == null) {
            throw new IllegalArgumentException("Provide at least one of: status, priority");
        }
        Ticket ticket = repository.findById(id).orElseThrow(() -> new TicketNotFoundException(id));
        if (req.status() != null) ticket.setStatus(req.status());
        if (req.priority() != null) ticket.setPriority(req.priority());
        return TicketResponse.from(ticket); 
    }

   
    public Map<String, Long> stats() {
        Map<TicketStatus, Long> counts = new EnumMap<>(TicketStatus.class);
        for (TicketStatus s : TicketStatus.values()) counts.put(s, 0L);
        for (Object[] row : repository.countGroupedByStatus()) {
            counts.put((TicketStatus) row[0], (Long) row[1]);
        }
        Map<String, Long> out = new LinkedHashMap<>();
        counts.forEach((s, c) -> out.put(s.value(), c));
        return out;
    }
}

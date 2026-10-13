package com.lodwar.tickets.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(List<T> items, int page, int limit, long total, int totalPages) {
    public static <T> PageResponse<T> of(Page<T> p) {
    
        return new PageResponse<>(p.getContent(), p.getNumber() + 1, p.getSize(),
                p.getTotalElements(), p.getTotalPages());
    }
}

package com.tlu.tsms.response;

import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> items;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean hasNext;
    private boolean hasPrevious;
    private int numberOfElements;

    public static <T> PageResponse<T> toResponse(Page<T> p) {
        return PageResponse.<T>builder()
                .items(p.getContent())
                .page(p.getNumber())
                .size(p.getSize())
                .totalElements(p.getTotalElements())
                .totalPages(p.getTotalPages())
                .first(p.isFirst())
                .last(p.isLast())
                .hasNext(p.hasNext())
                .hasPrevious(p.hasPrevious())
                .numberOfElements(p.getNumberOfElements())
                .build();
    }
}

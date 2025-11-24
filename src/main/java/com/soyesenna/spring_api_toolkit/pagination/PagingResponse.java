package com.soyesenna.spring_api_toolkit.pagination;

import java.util.List;
import org.springframework.data.domain.Page;

public record PagingResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last,
    boolean hasNext,
    boolean hasPrevious,
    List<SortRequest> sort
) {

  public PagingResponse {
    content = content == null ? List.of() : List.copyOf(content);
    sort = sort == null ? List.of() : List.copyOf(sort);
  }

  public static <T> PagingResponse<T> from(Page<T> page) {
    List<SortRequest> sortRequests = page.getSort().isUnsorted()
        ? List.of()
        : page.getSort().stream()
            .map(order -> new SortRequest(order.getProperty(), order.getDirection()))
            .toList();

    return new PagingResponse<>(
        page.getContent(),
        page.getNumber() + 1,
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isFirst(),
        page.isLast(),
        page.hasNext(),
        page.hasPrevious(),
        sortRequests
    );
  }
}

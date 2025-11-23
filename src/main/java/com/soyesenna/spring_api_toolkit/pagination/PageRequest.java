package com.soyesenna.spring_api_toolkit.pagination;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public record PageRequest(int page, int size, List<SortRequest> sorts) {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 20;

  public PageRequest {
    page = page < 1 ? DEFAULT_PAGE : page;
    size = size < 1 ? DEFAULT_SIZE : size;
    sorts = sorts == null ? List.of() : List.copyOf(sorts);
  }

  public Pageable toPageable() {
    Sort sort = sorts.isEmpty()
        ? Sort.unsorted()
        : Sort.by(sorts.stream().map(SortRequest::toOrder).toList());

    return org.springframework.data.domain.PageRequest.of(page - 1, size, sort);
  }
}

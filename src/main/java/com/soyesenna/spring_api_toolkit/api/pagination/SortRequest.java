package com.soyesenna.spring_api_toolkit.api.pagination;

import org.springframework.data.domain.Sort;

public record SortRequest(String property, Sort.Direction direction) {

  public SortRequest {
    if (property == null || property.isBlank()) {
      throw new IllegalArgumentException("정렬 대상 컬럼은 비워둘 수 없습니다.");
    }
    direction = direction == null ? Sort.Direction.ASC : direction;
  }

  public static SortRequest asc(String property) {
    return new SortRequest(property, Sort.Direction.ASC);
  }

  public static SortRequest desc(String property) {
    return new SortRequest(property, Sort.Direction.DESC);
  }

  public Sort.Order toOrder() {
    return new Sort.Order(direction, property);
  }
}

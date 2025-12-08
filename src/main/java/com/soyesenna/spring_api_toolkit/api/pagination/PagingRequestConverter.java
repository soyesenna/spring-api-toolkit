package com.soyesenna.spring_api_toolkit.api.pagination;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class PagingRequestConverter implements Converter<String, PagingRequest> {

  private final ObjectMapper objectMapper;

  public PagingRequestConverter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public PagingRequest convert(String source) {
    try {
      return this.objectMapper.readValue(source, PagingRequest.class);
    } catch (Exception e) {
      throw new IllegalArgumentException("JSON parsing error", e);
    }
  }
}
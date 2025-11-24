package com.soyesenna.spring_api_toolkit.api.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

@JsonSerialize
public class ApiData<T> {

  private static final String SUCCESS_CODE = "SUCCESS";
  private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";

  @JsonIgnore
  private final HttpStatus httpStatus;

  @JsonIgnore
  private final List<ApiHeader> headers;

  @JsonIgnore
  private final MediaType contentType;

  private final Boolean success;
  private final T data;
  private final String code;
  private final Object message;

  private ApiData(Builder<T> builder) {
    this.httpStatus = builder.httpStatus;
    this.headers = List.copyOf(builder.headers);
    this.contentType = builder.contentType;
    this.success = builder.success;
    this.data = builder.data;
    this.code = builder.code;
    this.message = builder.message;
  }

  public static <T> Builder<T> builder() {
    return new Builder<>();
  }

  public static <T> ApiData<T> ok(T data) {
    return ApiData.ok(data, MediaType.APPLICATION_JSON);
  }

  public static <T> ApiData<T> ok(T data, MediaType contentType) {
    return ApiData.<T>builder()
        .httpStatus(HttpStatus.OK)
        .success(true)
        .data(data)
        .contentType(contentType)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static <T> ApiData<T> created(T data) {
    return ApiData.created(data, MediaType.APPLICATION_JSON);
  }

  public static <T> ApiData<T> created(T data, MediaType contentType) {
    return ApiData.<T>builder()
        .httpStatus(HttpStatus.CREATED)
        .success(true)
        .data(data)
        .contentType(contentType)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static <T> ApiData<T> from(HttpStatus httpStatus, T data) {
    return ApiData.from(httpStatus, data, MediaType.APPLICATION_JSON);
  }

  public static <T> ApiData<T> from(HttpStatus httpStatus, T data, MediaType contentType) {
    return ApiData.<T>builder()
        .httpStatus(httpStatus)
        .success(true)
        .data(data)
        .contentType(contentType)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static ApiData<Void> noContent() {
    return ApiData.<Void>builder()
        .httpStatus(HttpStatus.NO_CONTENT)
        .success(true)
        .code(SUCCESS_CODE)
        .message(SUCCESS_MESSAGE)
        .build();
  }

  public static <T> ApiData<T> error(HttpStatus httpStatus, String code, Object message) {
    return ApiData.<T>builder()
        .httpStatus(httpStatus)
        .success(false)
        .code(code)
        .message(message)
        .build();
  }

  public static ApiData<Map<String, String>> validationErrors(List<FieldError> fieldErrors) {
    return validationErrors(HttpStatus.BAD_REQUEST, fieldErrors);
  }

  public static ApiData<Map<String, String>> validationErrors(HttpStatus httpStatus,
      List<FieldError> fieldErrors) {
    Map<String, String> errors = new HashMap<>();
    if (fieldErrors != null) {
      for (FieldError fieldError : fieldErrors) {
        errors.put(fieldError.getField(), fieldError.getDefaultMessage());
      }
    }

    return ApiData.<Map<String, String>>builder()
        .httpStatus(httpStatus)
        .success(false)
        .code("VALIDATION_ERROR")
        .message("요청 유효성 검증에 실패했습니다.")
        .data(errors)
        .build();
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  public List<ApiHeader> getHeaders() {
    return Collections.unmodifiableList(headers);
  }

  public MediaType getContentType() {
    return contentType;
  }

  public Boolean getSuccess() {
    return success;
  }

  public T getData() {
    return data;
  }

  public String getCode() {
    return code;
  }

  public Object getMessage() {
    return message;
  }

  public ResponseEntity<Object> toResponseEntity() {
    Object body = contentType == MediaType.APPLICATION_JSON ? this : data;
    return new ResponseEntity<>(body, toHttpHeaders(), httpStatus);
  }

  private HttpHeaders toHttpHeaders() {
    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(contentType);
    for (ApiHeader header : headers) {
      httpHeaders.add(header.getName(), header.getValue());
    }
    return httpHeaders;
  }

  public static final class Builder<T> {

    private HttpStatus httpStatus = HttpStatus.OK;
    private final List<ApiHeader> headers = new ArrayList<>();
    private MediaType contentType = MediaType.APPLICATION_JSON;
    private Boolean success = true;
    private T data;
    private String code = SUCCESS_CODE;
    private Object message = SUCCESS_MESSAGE;

    public Builder<T> httpStatus(HttpStatus httpStatus) {
      this.httpStatus = httpStatus;
      return this;
    }

    public Builder<T> header(String name, String value) {
      this.headers.add(new ApiHeader(name, value));
      return this;
    }

    public Builder<T> headers(List<ApiHeader> headers) {
      this.headers.clear();
      if (headers != null) {
        this.headers.addAll(headers);
      }
      return this;
    }

    public Builder<T> contentType(MediaType contentType) {
      this.contentType = contentType;
      return this;
    }

    public Builder<T> success(Boolean success) {
      this.success = success;
      return this;
    }

    public Builder<T> data(T data) {
      this.data = data;
      return this;
    }

    public Builder<T> code(String code) {
      this.code = code;
      return this;
    }

    public Builder<T> message(Object message) {
      this.message = message;
      return this;
    }

    public ApiData<T> build() {
      if (httpStatus == null) {
        httpStatus = HttpStatus.OK;
      }
      if (contentType == null) {
        contentType = MediaType.APPLICATION_JSON;
      }
      return new ApiData<>(this);
    }
  }
}

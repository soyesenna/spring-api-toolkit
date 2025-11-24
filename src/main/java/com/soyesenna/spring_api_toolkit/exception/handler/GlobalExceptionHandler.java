package com.soyesenna.spring_api_toolkit.exception.handler;

import com.soyesenna.spring_api_toolkit.api.core.ApiData;
import com.soyesenna.spring_api_toolkit.exception.CoreException;
import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(CoreException.class)
  public ApiData<Map<String, Object>> handleCoreException(CoreException exception,
      HttpServletRequest request) {
    return this.createApiError(exception.getErrorCode(), exception.getArgs(), request);
  }

  @ExceptionHandler(Exception.class)
  public ApiData<Map<String, Object>> handleUnexpected(Exception exception,
      HttpServletRequest request) {
    return this.createFallbackApiError(exception, request);
  }

  private ApiData<Map<String, Object>> createApiError(BaseErrorCode errorCode, Object[] args,
      HttpServletRequest request) {
    return ApiData.<Map<String, Object>>builder()
        .httpStatus(errorCode.getHttpStatus())
        .success(false)
        .code(this.resolveCode(errorCode))
        .message(this.resolveMessage(errorCode, args))
        .data(this.createMetadata(errorCode, request))
        .build();
  }

  private ApiData<Map<String, Object>> createFallbackApiError(Exception exception,
      HttpServletRequest request) {
    return ApiData.<Map<String, Object>>builder()
        .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
        .success(false)
        .code("INTERNAL_SERVER_ERROR")
        .message(
            exception.getMessage() != null ? exception.getMessage() : "Unexpected server error")
        .data(this.createFallbackMetadata(request))
        .build();
  }

  private Map<String, Object> createMetadata(BaseErrorCode errorCode, HttpServletRequest request) {
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("path", this.buildInstancePath(request));
    metadata.put("type", errorCode.getDomain());
    metadata.put("timestamp", Instant.now().toString());
    metadata.put("logLevel", errorCode.getLogLevel().name());
    return metadata;
  }

  private Map<String, Object> createFallbackMetadata(HttpServletRequest request) {
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("path", this.buildInstancePath(request));
    metadata.put("timestamp", Instant.now().toString());
    return metadata;
  }

  private String buildInstancePath(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    return requestUri != null ? requestUri : "";
  }

  private String resolveCode(BaseErrorCode errorCode) {
    return errorCode.resolveCode();
  }

  private String resolveMessage(BaseErrorCode errorCode, Object[] args) {
    Object[] safeArgs = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
    String messagePattern = errorCode.getMessage();
    if (safeArgs.length == 0) {
      return messagePattern;
    }
    return MessageFormat.format(messagePattern, safeArgs);
  }
}

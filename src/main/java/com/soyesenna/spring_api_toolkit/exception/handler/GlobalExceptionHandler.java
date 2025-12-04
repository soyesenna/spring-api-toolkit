package com.soyesenna.spring_api_toolkit.exception.handler;

import com.soyesenna.spring_api_toolkit.api.core.ApiData;
import com.soyesenna.spring_api_toolkit.config.ApiLogProperties;
import com.soyesenna.spring_api_toolkit.exception.CoreException;
import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private final ApiLogProperties logProperties;

  public GlobalExceptionHandler(ApiLogProperties logProperties) {
    this.logProperties = logProperties;
  }

  @ExceptionHandler(CoreException.class)
  public ApiData<Void> handleCoreException(CoreException exception, HttpServletRequest request) {
    BaseErrorCode errorCode = exception.getErrorCode();
    String resolvedMessage = this.resolveMessage(errorCode, exception.getArgs());

    this.logException(errorCode, resolvedMessage, request, exception);

    return ApiData.error(errorCode.getHttpStatus(), errorCode.resolveCode(), resolvedMessage);
  }

  @ExceptionHandler(Exception.class)
  public ApiData<Void> handleUnexpected(Exception exception, HttpServletRequest request) {
    this.logUnexpectedException(request, exception);

    return ApiData.error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "INTERNAL_SERVER_ERROR",
        "Unexpected server error");
  }

  private void logException(BaseErrorCode errorCode, String message, HttpServletRequest request,
      CoreException exception) {
    LogLevel level = errorCode.getLogLevel();
    String path = this.buildInstancePath(request);
    String domain = errorCode.getDomain();
    String code = errorCode.resolveCode();

    String logMessage = "[{}] {} - {} (path: {})";
    Object[] logArgs = {domain, code, message, path};

    if (this.logProperties.isStackTraceEnabled()) {
      this.logWithStackTrace(level, logMessage, logArgs, exception);
    } else {
      this.logWithoutStackTrace(level, logMessage, logArgs);
    }
  }

  private void logUnexpectedException(HttpServletRequest request, Exception exception) {
    String path = this.buildInstancePath(request);
    String logMessage = "[UNEXPECTED] {} (path: {})";

    if (this.logProperties.isStackTraceEnabled()) {
      log.error(logMessage, exception.getMessage(), path, exception);
    } else {
      log.error(logMessage, exception.getMessage(), path);
    }
  }

  private void logWithStackTrace(LogLevel level, String message, Object[] args,
      Exception exception) {
    Object[] argsWithException = Arrays.copyOf(args, args.length + 1);
    argsWithException[args.length] = exception;

    switch (level) {
      case TRACE -> log.trace(message, argsWithException);
      case DEBUG -> log.debug(message, argsWithException);
      case INFO -> log.info(message, argsWithException);
      case WARN -> log.warn(message, argsWithException);
      default -> log.error(message, argsWithException);
    }
  }

  private void logWithoutStackTrace(LogLevel level, String message, Object[] args) {
    switch (level) {
      case TRACE -> log.trace(message, args);
      case DEBUG -> log.debug(message, args);
      case INFO -> log.info(message, args);
      case WARN -> log.warn(message, args);
      default -> log.error(message, args);
    }
  }

  private String buildInstancePath(HttpServletRequest request) {
    String requestUri = request.getRequestURI();
    return requestUri != null ? requestUri : "";
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

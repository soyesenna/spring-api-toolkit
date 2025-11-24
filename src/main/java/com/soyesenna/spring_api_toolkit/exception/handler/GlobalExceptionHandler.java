package com.soyesenna.spring_api_toolkit.exception.handler;

import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import com.soyesenna.spring_api_toolkit.exception.exception.CoreException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ProblemDetail> handleCoreException(CoreException exception, HttpServletRequest request) {
        ProblemDetail body = this.createProblemDetail(exception.getErrorCode(), exception.getArgs(), request);
        return ResponseEntity.status(exception.getErrorCode().getHttpStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception exception, HttpServletRequest request) {
        ProblemDetail body = this.createFallbackProblemDetail(exception, request);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ProblemDetail createProblemDetail(BaseErrorCode errorCode, Object[] args, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(errorCode.getHttpStatus());
        problemDetail.setTitle(this.resolveTitle(errorCode));
        problemDetail.setDetail(this.resolveMessage(errorCode, args));
        problemDetail.setInstance(this.buildInstanceUri(request));
        problemDetail.setType(this.buildTypeUri(errorCode));
        problemDetail.setProperty("type", errorCode.getDomain());
        problemDetail.setProperty("errorCode", this.resolveTitle(errorCode));
        problemDetail.setProperty("timestamp", Instant.now().toString());
        problemDetail.setProperty("logLevel", errorCode.getLogLevel().name());
        return problemDetail;
    }

    private ProblemDetail createFallbackProblemDetail(Exception exception, HttpServletRequest request) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("UNEXPECTED_ERROR");
        problemDetail.setDetail(exception.getMessage() != null ? exception.getMessage() : "Unexpected server error");
        problemDetail.setInstance(this.buildInstanceUri(request));
        problemDetail.setType(URI.create("urn:error-code:UNEXPECTED_ERROR"));
        problemDetail.setProperty("type", "UNEXPECTED_ERROR");
        problemDetail.setProperty("timestamp", Instant.now().toString());
        return problemDetail;
    }

    private URI buildInstanceUri(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        return URI.create(requestUri != null ? requestUri : "");
    }

    private URI buildTypeUri(BaseErrorCode errorCode) {
        return URI.create("urn:error-code:" + errorCode.getDomain());
    }

    private String resolveTitle(BaseErrorCode errorCode) {
        return errorCode.getCode();
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

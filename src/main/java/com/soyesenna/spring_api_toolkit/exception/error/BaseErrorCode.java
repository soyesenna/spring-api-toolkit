package com.soyesenna.spring_api_toolkit.exception.error;

import com.soyesenna.spring_api_toolkit.exception.CoreException;
import com.soyesenna.spring_api_toolkit.exception.ExceptionSupplier;
import java.util.Arrays;
import java.util.function.Supplier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

/**
 * Base contract for every error enum used inside the toolkit.
 */
public interface BaseErrorCode extends ExceptionSupplier {

  HttpStatus getHttpStatus();

  String getCode();

  String getMessage();

  default LogLevel getLogLevel() {
    return LogLevel.ERROR;
  }

  default String getDomain() {
    Class<?> source =
        this instanceof Enum<?> enumConstant ? enumConstant.getDeclaringClass() : this.getClass();
    return source.getSimpleName();
  }

  default String resolveCode() {
    String code = this.getCode();
    if (code == null || code.isBlank()) {
      if (this instanceof Enum<?> enumConstant) {
        return enumConstant.name();
      }
      return this.getClass().getSimpleName();
    }
    return code;
  }

  @Override
  default CoreException throwException() {
    return new CoreException(this);
  }

  default ExceptionSupplier args(Object... values) {
    Object[] safeArgs = values == null ? new Object[0] : Arrays.copyOf(values, values.length);
    return () -> new CoreException(this, safeArgs);
  }

  default CoreException getWithoutStackTrace() {
    return new CoreException(this, false);
  }

  default Supplier<CoreException> argsWithoutStackTrace(Object... values) {
    Object[] safeArgs = values == null ? new Object[0] : Arrays.copyOf(values, values.length);
    return () -> new CoreException(this, false, safeArgs);
  }
}

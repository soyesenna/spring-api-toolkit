package com.soyesenna.spring_api_toolkit.exception.error;

import com.soyesenna.spring_api_toolkit.exception.CoreException;
import java.util.Arrays;
import java.util.function.Supplier;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;

/**
 * Base contract for every error enum used inside the toolkit.
 */
public interface BaseErrorCode {

    HttpStatus getHttpStatus();

    String getMessage();

    default LogLevel getLogLevel() {
        return LogLevel.ERROR;
    }

    default String getDomain() {
        Class<?> source = this instanceof Enum<?> enumConstant ? enumConstant.getDeclaringClass() : this.getClass();
        return source.getSimpleName();
    }

    default String getCode() {
        return this instanceof Enum<?> enumConstant ? enumConstant.name() : this.getClass().getSimpleName();
    }

    default CoreException throwException() {
        return new CoreException(this);
    }

    default Supplier<CoreException> args(Object... values) {
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

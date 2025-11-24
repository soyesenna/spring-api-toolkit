package com.soyesenna.spring_api_toolkit.exception.exception;

import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import java.util.Arrays;
import java.util.Objects;

public class CoreException extends RuntimeException {

    private static final StackTraceElement[] EMPTY_STACK_TRACE = new StackTraceElement[0];
    private static final Object[] EMPTY_ARGS = new Object[0];

    private final BaseErrorCode errorCode;
    private final Object[] args;
    private final boolean stackTraceEnabled;

    public CoreException(BaseErrorCode errorCode) {
        this(errorCode, true, CoreException.EMPTY_ARGS);
    }

    public CoreException(BaseErrorCode errorCode, Object... args) {
        this(errorCode, true, args);
    }

    public CoreException(BaseErrorCode errorCode, boolean stackTraceEnabled) {
        this(errorCode, stackTraceEnabled, CoreException.EMPTY_ARGS);
    }

    public CoreException(BaseErrorCode errorCode, boolean stackTraceEnabled, Object... args) {
        super(errorCode != null ? errorCode.getMessage() : null, null, true, stackTraceEnabled);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        this.stackTraceEnabled = stackTraceEnabled;
        this.args = args == null ? new Object[0] : Arrays.copyOf(args, args.length);
        if (!this.stackTraceEnabled) {
            this.setStackTrace(CoreException.EMPTY_STACK_TRACE);
        }
    }

    public BaseErrorCode getErrorCode() {
        return this.errorCode;
    }

    public Object[] getArgs() {
        return Arrays.copyOf(this.args, this.args.length);
    }

    public boolean isStackTraceEnabled() {
        return this.stackTraceEnabled;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        if (!this.stackTraceEnabled) {
            return this;
        }
        return super.fillInStackTrace();
    }
}

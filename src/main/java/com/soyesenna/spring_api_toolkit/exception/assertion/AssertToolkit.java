package com.soyesenna.spring_api_toolkit.exception.assertion;

import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import com.soyesenna.spring_api_toolkit.exception.CoreException;
import java.util.Collection;
import java.util.Map;
import org.springframework.util.StringUtils;

public final class AssertToolkit {

    private AssertToolkit() {
        throw new IllegalStateException("Utility class");
    }

    public static void isTrue(boolean expression, BaseErrorCode errorCode, Object... args) {
        if (!expression) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void state(boolean expression, BaseErrorCode errorCode, Object... args) {
        AssertToolkit.isTrue(expression, errorCode, args);
    }

    public static void notNull(Object object, BaseErrorCode errorCode, Object... args) {
        if (object == null) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void isNull(Object object, BaseErrorCode errorCode, Object... args) {
        if (object != null) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void hasText(String text, BaseErrorCode errorCode, Object... args) {
        if (!StringUtils.hasText(text)) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void notEmpty(Collection<?> collection, BaseErrorCode errorCode, Object... args) {
        if (collection == null || collection.isEmpty()) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void notEmpty(Map<?, ?> map, BaseErrorCode errorCode, Object... args) {
        if (map == null || map.isEmpty()) {
            throw new CoreException(errorCode, args);
        }
    }

    public static void noNullElements(Collection<?> collection, BaseErrorCode errorCode, Object... args) {
        if (collection == null) {
            throw new CoreException(errorCode, args);
        }
        for (Object element : collection) {
            if (element == null) {
                throw new CoreException(errorCode, args);
            }
        }
    }

    public static void isInstanceOf(Class<?> type, Object candidate, BaseErrorCode errorCode, Object... args) {
        if (type == null || !type.isInstance(candidate)) {
            throw new CoreException(errorCode, args);
        }
    }
}

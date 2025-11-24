package com.soyesenna.spring_api_toolkit.exception.swagger;

import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiErrorCode {

    ErrorRef[] value();

    @interface ErrorRef {
        /**
         * Enum type that implements BaseErrorCode.
         */
        Class<? extends Enum<? extends BaseErrorCode>> type();

        /**
         * Enum constant names to include. If empty, all constants of the enum are used.
         */
        String[] codes() default {};
    }
}

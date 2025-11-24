package com.soyesenna.spring_api_toolkit.config;

import com.soyesenna.spring_api_toolkit.api.advice.ApiDataAdvice;
import com.soyesenna.spring_api_toolkit.exception.handler.GlobalExceptionHandler;
import com.soyesenna.spring_api_toolkit.exception.swagger.ApiErrorCodeOperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configures ApiData wrappers, exception handling, and Swagger examples when the library is on
 * the classpath.
 */
@AutoConfiguration
@Import({
        ApiDataAdvice.class,
        GlobalExceptionHandler.class,
        ApiErrorCodeOperationCustomizer.class
})
public class SpringApiToolkitAutoConfiguration {
}

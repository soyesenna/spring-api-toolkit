package com.soyesenna.spring_api_toolkit.config;

import com.soyesenna.spring_api_toolkit.api.advice.ApiDataAdvice;
import com.soyesenna.spring_api_toolkit.exception.handler.GlobalExceptionHandler;
import com.soyesenna.spring_api_toolkit.exception.swagger.ApiErrorCodeOperationCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * Auto-configures ApiData wrappers, exception handling, and Swagger examples when the library is on
 * the classpath.
 */
@AutoConfiguration
@EnableConfigurationProperties(ApiLogProperties.class)
@Import({
    ApiDataAdvice.class,
    ApiErrorCodeOperationCustomizer.class
})
public class SpringApiToolkitAutoConfiguration {

  @Bean
  public GlobalExceptionHandler globalExceptionHandler(ApiLogProperties logProperties) {
    return new GlobalExceptionHandler(logProperties);
  }
}

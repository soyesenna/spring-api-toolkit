package com.soyesenna.spring_api_toolkit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for API logging behavior.
 *
 * <p>Example configuration in application.yml:
 * <pre>
 * api:
 *   log:
 *     stack-trace-enabled: true
 * </pre>
 */
@ConfigurationProperties(prefix = "api.log")
public class ApiLogProperties {

  /**
   * Whether to log stack traces for exceptions. Defaults to true. Set to false in production to
   * reduce log verbosity and hide internal details.
   */
  private boolean stackTraceEnabled = true;

  public boolean isStackTraceEnabled() {
    return this.stackTraceEnabled;
  }

  public void setStackTraceEnabled(boolean stackTraceEnabled) {
    this.stackTraceEnabled = stackTraceEnabled;
  }
}

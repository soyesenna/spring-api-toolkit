package com.soyesenna.spring_api_toolkit.api.pagination;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Configuration properties for pagination defaults.
 * <p>
 * Configure in application.yml or application.properties:
 * <pre>
 * api.page.default-page=1
 * api.page.default-size=20
 * </pre>
 */
@Component
@ConfigurationProperties(prefix = "api.page")
public class PagingProperties {

  private int defaultPage = 1;
  private int defaultSize = 20;

  @PostConstruct
  public void init() {
    validateAndSetDefaults();
  }

  private void validateAndSetDefaults() {
    if (defaultPage < 1) {
      defaultPage = 1;
    }
    if (defaultSize < 1) {
      defaultSize = 20;
    }
    PagingRequest.setDefaultValues(defaultPage, defaultSize);
  }

  public int getDefaultPage() {
    return defaultPage;
  }

  public void setDefaultPage(int defaultPage) {
    this.defaultPage = defaultPage;
  }

  public int getDefaultSize() {
    return defaultSize;
  }

  public void setDefaultSize(int defaultSize) {
    this.defaultSize = defaultSize;
  }
}

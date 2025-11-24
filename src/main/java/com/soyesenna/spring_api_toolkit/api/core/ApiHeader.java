package com.soyesenna.spring_api_toolkit.api.core;

public class ApiHeader {

  private String name;
  private String value;

  public ApiHeader(String name, String value) {
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}

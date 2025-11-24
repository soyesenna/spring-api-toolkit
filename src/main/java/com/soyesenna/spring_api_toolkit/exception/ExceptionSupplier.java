package com.soyesenna.spring_api_toolkit.exception;

@FunctionalInterface
public interface ExceptionSupplier {

  CoreException throwException();
}

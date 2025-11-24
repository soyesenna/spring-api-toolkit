package com.soyesenna.spring_api_toolkit.exception.swagger;

import com.soyesenna.spring_api_toolkit.exception.error.BaseErrorCode;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

@Component
public class ApiErrorCodeOperationCustomizer implements OperationCustomizer {

  @Override
  public Operation customize(Operation operation, HandlerMethod handlerMethod) {
    ApiErrorCode annotation = handlerMethod.getMethodAnnotation(ApiErrorCode.class);
    if (annotation == null) {
      return operation;
    }
    ApiResponses responses = operation.getResponses();
    for (ApiErrorCode.ErrorRef ref : annotation.value()) {
      this.registerByRef(responses, ref);
    }
    return operation;
  }

  private void registerByRef(ApiResponses responses, ApiErrorCode.ErrorRef ref) {
    Class<? extends Enum<? extends BaseErrorCode>> enumClass = ref.type();
    BaseErrorCode[] errorCodes = (BaseErrorCode[]) enumClass.getEnumConstants();
    if (errorCodes == null) {
      return;
    }
    Set<String> includes = this.resolveIncludes(ref.codes());
    for (BaseErrorCode errorCode : errorCodes) {
      if (!includes.isEmpty() && !includes.contains(this.enumNameOf(errorCode))) {
        continue;
      }
      this.registerExample(responses, errorCode);
    }
  }

  private Set<String> resolveIncludes(String[] codes) {
    if (codes == null || codes.length == 0) {
      return Set.of();
    }
    Set<String> includes = new HashSet<>();
    Arrays.stream(codes)
        .filter(code -> code != null && !code.isBlank())
        .map(String::trim)
        .forEach(includes::add);
    return includes;
  }

  private void registerExample(ApiResponses responses, BaseErrorCode errorCode) {
    String statusCode = String.valueOf(errorCode.getHttpStatus().value());
    ApiResponse apiResponse = responses.computeIfAbsent(statusCode,
        code -> new ApiResponse().description("Auto generated from ApiErrorCode"));
    if (apiResponse.getDescription() == null || apiResponse.getDescription().isBlank()) {
      apiResponse.setDescription("Auto generated from ApiErrorCode");
    }

    Example example = new Example();
    String exampleKey = this.resolveExampleKey(errorCode);
    example.setSummary(exampleKey);
    example.setValue(this.createExamplePayload(errorCode));

    Content content = apiResponse.getContent();
    if (content == null) {
      content = new Content();
      apiResponse.setContent(content);
    }
    MediaType mediaType = content.computeIfAbsent(
        org.springframework.http.MediaType.APPLICATION_JSON_VALUE, value -> new MediaType());
    mediaType.addExamples(exampleKey, example);
  }

  private Map<String, Object> createExamplePayload(BaseErrorCode errorCode) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("success", false);
    payload.put("code", this.resolveExampleKey(errorCode));
    payload.put("message", errorCode.getMessage());
    payload.put("data", this.createMetadata(errorCode));
    return payload;
  }

  private Map<String, Object> createMetadata(BaseErrorCode errorCode) {
    Map<String, Object> metadata = new LinkedHashMap<>();
    metadata.put("type", errorCode.getDomain());
    metadata.put("httpStatus", errorCode.getHttpStatus().value());
    metadata.put("path", "{request-path}");
    metadata.put("timestamp", OffsetDateTime.now().toString());
    metadata.put("logLevel", errorCode.getLogLevel().name());
    return metadata;
  }

  private String resolveExampleKey(BaseErrorCode errorCode) {
    return errorCode.resolveCode();
  }

  private String enumNameOf(BaseErrorCode errorCode) {
    if (errorCode instanceof Enum<?> enumConstant) {
      return enumConstant.name();
    }
    return errorCode.resolveCode();
  }
}

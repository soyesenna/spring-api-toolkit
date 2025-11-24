package com.soyesenna.spring_api_toolkit.exception.swagger;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
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
        for (Class<? extends BaseErrorCode> enumClass : annotation.value()) {
            BaseErrorCode[] errorCodes = enumClass.getEnumConstants();
            if (errorCodes == null) {
                continue;
            }
            for (BaseErrorCode errorCode : errorCodes) {
                this.registerExample(responses, errorCode);
            }
        }
        return operation;
    }

    private void registerExample(ApiResponses responses, BaseErrorCode errorCode) {
        String statusCode = String.valueOf(errorCode.getHttpStatus().value());
        ApiResponse apiResponse = responses.computeIfAbsent(statusCode, code -> new ApiResponse().description("Auto generated from ApiErrorCode"));
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
        MediaType mediaType = content.computeIfAbsent(org.springframework.http.MediaType.APPLICATION_JSON_VALUE, value -> new MediaType());
        mediaType.addExamples(exampleKey, example);
    }

    private Map<String, Object> createExamplePayload(BaseErrorCode errorCode) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", errorCode.getDomain());
        payload.put("title", this.resolveExampleKey(errorCode));
        payload.put("status", errorCode.getHttpStatus().value());
        payload.put("detail", errorCode.getMessage());
        payload.put("instance", "{request-path}");
        payload.put("timestamp", OffsetDateTime.now().toString());
        return payload;
    }

    private String resolveExampleKey(BaseErrorCode errorCode) {
        return errorCode.getCode();
    }
}

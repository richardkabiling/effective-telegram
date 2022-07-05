package com.example.demo.test.app.adapter.controller.data;

import java.util.Map;

public record ErrorResponse(String code, String message, Map<String, Object> details) {
}

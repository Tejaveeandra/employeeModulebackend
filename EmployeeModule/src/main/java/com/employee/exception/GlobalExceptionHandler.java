package com.employee.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	
	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
		logger.warn("Resource not found: {}", ex.getMessage());
		Map<String, Object> response = new HashMap<>();
		response.put("message", ex.getMessage());
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.NOT_FOUND.value());
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
	
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
		logger.error("JSON parse error: {}", ex.getMessage());
		
		String errorMessage = ex.getMessage();
		String fieldName = null;
		
		// Extract field name from error message if available
		if (ex.getCause() != null && ex.getCause().getMessage() != null) {
			String causeMessage = ex.getCause().getMessage();
			// Look for field name in pattern like: "through reference chain: com.employee.dto.SalaryInfoDTO[\"pfNo\"]"
			int fieldStart = causeMessage.indexOf("[\"");
			int fieldEnd = causeMessage.indexOf("\"]", fieldStart);
			if (fieldStart > 0 && fieldEnd > fieldStart) {
				fieldName = causeMessage.substring(fieldStart + 2, fieldEnd);
			}
		}
		
		if (errorMessage != null && errorMessage.contains("CTRL-CHAR")) {
			if (fieldName != null) {
				errorMessage = String.format(
					"Invalid JSON format: Unescaped newline or special character found in field '%s'. " +
					"Please escape newlines as \\n in your JSON. " +
					"Example: Use \"line1\\nline2\" instead of actual line breaks. " +
					"Field '%s' contains unescaped newline characters that must be escaped.",
					fieldName, fieldName);
			} else {
				errorMessage = "Invalid JSON format: Unescaped newline or special character found. " +
						"Please escape newlines as \\n in your JSON. " +
						"Example: Use \"line1\\nline2\" instead of actual line breaks.";
			}
		} else {
			if (fieldName != null) {
				errorMessage = String.format("Invalid JSON format in field '%s'. Please check your request body syntax.", fieldName);
			} else {
				errorMessage = "Invalid JSON format. Please check your request body syntax.";
			}
		}
		
		Map<String, Object> response = new HashMap<>();
		response.put("message", errorMessage);
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("exception", "HttpMessageNotReadableException");
		
		if (fieldName != null) {
			response.put("field", fieldName);
		}
		
		if (ex.getCause() != null) {
			response.put("cause", ex.getCause().getMessage());
		}
		
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(Exception.class)
	public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
		// Log the full exception with stack trace to console
		logger.error("❌ ERROR occurred in API: {}", ex.getMessage(), ex);
		System.err.println("❌ ERROR: " + ex.getMessage());
		ex.printStackTrace();
		
		Map<String, Object> response = new HashMap<>();
		response.put("message", ex.getMessage() != null ? ex.getMessage() : "Internal server error");
		response.put("timestamp", LocalDateTime.now());
		response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
		
		// Include exception class name and cause for debugging
		if (ex.getCause() != null) {
			response.put("cause", ex.getCause().getMessage());
		}
		response.put("exception", ex.getClass().getSimpleName());
		
		return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}


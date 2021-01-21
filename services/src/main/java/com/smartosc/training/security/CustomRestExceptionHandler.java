package com.smartosc.training.security;

import com.smartosc.training.entity.APIError;
import com.smartosc.training.entity.APIErrorDetail;
import com.smartosc.training.exception.EntityNotFoundException;
import com.smartosc.training.exception.FieldDuplicateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class CustomRestExceptionHandler extends ResponseEntityExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomRestExceptionHandler.class);

	@ExceptionHandler(EntityNotFoundException.class)
	protected ResponseEntity<Object> handleEntityNotFound(
			EntityNotFoundException ex) {
		APIError apiError = new APIError(HttpStatus.NOT_FOUND);
		apiError.setMessage(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@ExceptionHandler(FieldDuplicateException.class)
	protected ResponseEntity<Object> handleFieldDuplicate(
			FieldDuplicateException ex) {
		APIError apiError = new APIError(HttpStatus.BAD_REQUEST);
		apiError.setMessage(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(
			MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(status);
		apiError.setMessage("Validation errors");
		List<APIErrorDetail> errorDetails = new ArrayList<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			APIErrorDetail apiSubError = new APIErrorDetail();
			apiSubError.setField(((FieldError) error).getField());
			apiSubError.setObject(error.getObjectName());
			apiSubError.setRejectedValue(((FieldError) error).getRejectedValue());
			apiSubError.setMessage(error.getDefaultMessage());
			errorDetails.add(apiSubError);
		});
		apiError.setErrorDetails(errorDetails);
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleNoHandlerFoundException(
			NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(status);
		apiError.setMessage(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
			HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(status);
		apiError.setMessage(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

	@Override
	protected ResponseEntity<Object> handleMissingPathVariable(
			MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
		APIError apiError = new APIError(status);
		apiError.setMessage(ex.getMessage());
		LOGGER.error(ex.getMessage());
		return new ResponseEntity<>(apiError, apiError.getStatus());
	}

}

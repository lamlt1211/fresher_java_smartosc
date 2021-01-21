package com.smartosc.training.exception;

public class RestTemplateException extends RuntimeException {

	private static final long serialVersionUID = -3242126885270683621L;

	public RestTemplateException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public RestTemplateException(String message) {
		super(message);
	}

	public RestTemplateException(String message, Throwable cause) {
		super(message, cause);
	}

}
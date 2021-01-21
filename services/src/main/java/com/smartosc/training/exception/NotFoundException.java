package com.smartosc.training.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */
public class NotFoundException extends RuntimeException {


    public NotFoundException(String message) {
        super(message);
    }
}

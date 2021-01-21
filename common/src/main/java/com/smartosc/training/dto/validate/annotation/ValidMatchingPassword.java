package com.smartosc.training.dto.validate.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.validation.Constraint;
import javax.validation.Payload;

import com.smartosc.training.dto.validate.PasswordMatchesValidator;

@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface ValidMatchingPassword {
	
	String message() default "Password don't match";
	Class<?>[] groups() default {};
	Class<? extends Payload>[] payload() default {};
	
}

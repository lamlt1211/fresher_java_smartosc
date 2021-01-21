package com.smartosc.training.dto.validate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.dto.validate.annotation.ValidMatchingPassword;

public class PasswordMatchesValidator
	implements ConstraintValidator<ValidMatchingPassword, Object> {

	@Override
	public void initialize(ValidMatchingPassword constraintAnnotation) {
	}

	@Override
	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		UserDTO user = (UserDTO) obj;
		return user.getPassword().equals(user.getMatchingPassword());
	}

}

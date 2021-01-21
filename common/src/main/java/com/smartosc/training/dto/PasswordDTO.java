package com.smartosc.training.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.smartosc.training.dto.validate.annotation.ValidMatchingPassword;
import com.smartosc.training.dto.validate.annotation.ValidPassword;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PasswordDTO {
	
	@NotNull
	@NotEmpty
	@ValidPassword
	private String newPassword;
	private String confirmPassword;
	
}

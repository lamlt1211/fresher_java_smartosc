package com.smartosc.training.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import com.smartosc.training.dto.validate.annotation.ValidEmail;
import com.smartosc.training.dto.validate.annotation.ValidMatchingPassword;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ValidMatchingPassword
public class UserDTO extends AbstractDTO {
	
	private int userId;
	
	@NotNull
	@NotEmpty
	private String fullName;
	
	@ValidEmail
	@NotNull
	@NotEmpty
	private String email;
	
	@NotNull
	@NotEmpty
	private String userName;
	
	@NotNull
	@NotEmpty
	private String password;
	private String matchingPassword;
	
	private boolean enabled;
		
}

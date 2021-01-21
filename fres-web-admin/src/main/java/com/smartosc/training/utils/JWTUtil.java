package com.smartosc.training.utils;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.smartosc.training.entity.AppUserDetails;

@Component
public class JWTUtil {

	public String getJwtTokenFromSecurityContext() {
		return ((AppUserDetails) SecurityContextHolder.getContext()
				.getAuthentication().getPrincipal()).getJwtToken();
	}

}

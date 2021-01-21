package com.smartosc.training.controller;

import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.LoginRequest;
import com.smartosc.training.utils.JWTUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class JwtAuthenticactionController {
	
	private static final Logger LOGGER =
			LoggerFactory.getLogger(JwtAuthenticactionController.class);
	
	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private JWTUtils jwtTokenUtil;
	
	@PostMapping("generate_token")
	public ResponseEntity<Object> createAuthenticationToken (
			@RequestBody LoginRequest loginRequest) {
		Authentication auth;
		try {
			auth = authenticationManager.authenticate(
	                new UsernamePasswordAuthenticationToken(
	                		loginRequest.getUsername(), loginRequest.getPassword()));
		} catch (Exception e) {
			LOGGER.error("Responding with Bad credentials error. Message - {}", e.getMessage());
			throw new BadCredentialsException("Responding with Bad credentials error");
		}
		final String jwt = jwtTokenUtil.generateToken(auth);
		APIResponse<String> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Generate token successful!");
        responseData.setData(jwt);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
	}
	
}

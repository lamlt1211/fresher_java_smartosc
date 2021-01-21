package com.smartosc.training.controller;

import com.smartosc.training.dto.PasswordDTO;
import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.dto.VerificationTokenDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.event.OnRegistrationCompleteEvent;
import com.smartosc.training.event.OnResetPasswordEvent;
import com.smartosc.training.exception.RestTemplateException;
import com.smartosc.training.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.*;

@Controller
@RequestMapping("/")
public class RegisterController {
	
	@Value("${api.url}")
	private String url;
	
	@Value("${prefix.user}")
	private String prefixUrl;
	
	@Autowired
	private RestService restService;

	@Qualifier("messageSource")
	@Autowired
	private MessageSource messages;
	
	@Autowired
	private ApplicationEventPublisher eventPublisher;
	
	@GetMapping("badUser")
	public String getBadUserPage() {
		return "badUser";
	}
	
	@GetMapping("signup")
    public String getSignUpPage(Model model) {
    	model.addAttribute("user", new UserDTO());
        return "signup";
    }
    
    @PostMapping("signup")
    public String registerUserAccount(@ModelAttribute("user") @Valid UserDTO userDTO,
    		BindingResult result, HttpServletRequest request, Model model) {
    	UserDTO user = new UserDTO();
    	if(!result.hasErrors()) {
    		user = createUserAccount(userDTO);
    	}
    	if(user == null) {
    		result.rejectValue("email", "message.mail.regError", "This email or username already exist");
    	}
    	
    	if(result.hasErrors()) {
    		model.addAttribute("user", userDTO);
    		return "signup";
    	} else {
    		try {
        		String appUrl = request.getLocalName();
        		eventPublisher.publishEvent(
        				new OnRegistrationCompleteEvent(user, request.getLocale(), appUrl));
        	} catch (Exception e) {
    			return "emailError";
    		}
    		return "successRegister";
    	}
    }
    
    @GetMapping("registrationConfirm")
    public String confirmRegistration(@RequestParam("token") String token,
    		Model model, WebRequest request) {
    	Locale locale = request.getLocale();
    	
    	Map<String, Object> values = new HashMap<>();
    	values.put("token", token);
    	VerificationTokenDTO veryfyToken = null;
    	APIResponse<VerificationTokenDTO> response = restService.execute(
    			new StringBuilder(url).append(prefixUrl).append("/verify-token/{token}").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<VerificationTokenDTO>>() {},
				values);
    	if(response.getStatus() == 200) {
    		veryfyToken = response.getData();
		}
    	
    	if(veryfyToken == null) {
    		String message = messages.getMessage(
    				"auth.message.invalidToken", null, "This is invalid token", locale);
    		model.addAttribute("message", message);
    		return "forward:/badUser?lang=" + locale.getLanguage();
    	}
    	
    	Calendar cal = Calendar.getInstance();
    	if((veryfyToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
    		String messageValue = messages.getMessage(
    				"auth.message.expired", null, "This token has expired!", locale);
    		model.addAttribute("message", messageValue);
    		return "forward:/badUser?lang=" + locale.getLanguage();
    	}
    	
    	UserDTO user = veryfyToken.getUser();
    	if(activeRegisteredUser(user) != null) {
    		return "redirect:/login?lang=" + request.getLocale().getLanguage();
    	} else {
    		String messageValue = messages.getMessage(
    				"auth.message.resError", null, "Something wrong!", locale);
    		model.addAttribute("message", messageValue);
    		return "forward:/badUser?lang=" + locale.getLanguage();
    	}
    }
    
    @GetMapping("resetPass")
	public String getResetPasswordPage(Model model) {
		return "resetPass";
	}
    
    @PostMapping("resetPass")
    public String resetPassword(@RequestParam("email") String email,
    		HttpServletRequest request, Model model) {
    	Locale locale = request.getLocale();
    	Map<String, Object> values = new HashMap<>();
    	values.put("email", email);
    	UserDTO user = null;
    	APIResponse<UserDTO> responseData = restService.execute(
    			new StringBuilder(url).append(prefixUrl).append("/email/{email}").toString(),
    			HttpMethod.GET,
    			null,
    			null,
    			new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
    			values);
    	if(responseData.getStatus() == 200) {
    		user = responseData.getData();
    		if(user != null) {
    			try {
            		String appUrl = request.getLocalName();
            		eventPublisher.publishEvent(
            				new OnResetPasswordEvent(user, request.getLocale(), appUrl));
            	} catch (Exception e) {
        			return "emailError";
        		}
        		return "successResetPass";
    		} else {
    			String messageValue = messages.getMessage(
        				"message.resetPasswordError", null, "This email not found!", locale);
        		model.addAttribute("message", messageValue);
        		return "resetPass";
    		}
    	} else {
    		String messageValue = messages.getMessage(
    				"auth.message.resError", null, "Something wrong happen!", locale);
    		model.addAttribute("message", messageValue);
    		return "resetPass";
    	}
    }
    
    @GetMapping("user/changePassword")
    public String verifyResetPasswordToken(@RequestParam("id") Long id,
    		@RequestParam("token") String token, Model model, Locale locale) {
    	Map<String, Object> values = new HashMap<>();
    	values.put("token", token);
    	VerificationTokenDTO veryfyToken = null;
    	APIResponse<VerificationTokenDTO> response = restService.execute(
    			new StringBuilder(url).append(prefixUrl).append("/pass-reset-token/{token}").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<VerificationTokenDTO>>() {},
				values);
    	if(response.getStatus() == 200) {
    		veryfyToken = response.getData();
		}
    	
    	if(veryfyToken == null) {
    		String message = messages.getMessage(
    				"auth.message.invalidToken", null, "This is invalid token", locale);
    		model.addAttribute("message", message);
    		return "forward:/badUser?lang=" + locale.getLanguage();
    	}
    	
    	Calendar cal = Calendar.getInstance();
    	if((veryfyToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0) {
    		String messageValue = messages.getMessage(
    				"auth.message.expired", null, "This token has expired!", locale);
    		model.addAttribute("message", messageValue);
    		return "forward:/badUser?lang=" + locale.getLanguage();
    	}
    	
    	UserDTO user = veryfyToken.getUser();
    	Authentication auth = new UsernamePasswordAuthenticationToken(
    		      user, null, Arrays.asList(
    		      new SimpleGrantedAuthority("CHANGE_PASSWORD_PRIVILEGE")));
    	SecurityContextHolder.getContext().setAuthentication(auth);
    	return "forward:/changePassword";
    }
    
    @PostMapping("user/changePassword")
    public String updateNewPassword(@Valid PasswordDTO password, BindingResult result,
    		HttpServletRequest request, Locale locale, Model model) {
    	if(result.hasErrors()) {
    		String messageValue = messages.getMessage(
    				"auth.message.passError", null, "Password format is not correct!", locale);
    		model.addAttribute("message", messageValue);
    		return "changePassword";
    	} else {
    		if(!password.getNewPassword().equals(password.getConfirmPassword())) {
    			String messageValue = messages.getMessage(
        				"auth.message.passNotCompareError", null, "Password confirm is not equal!", locale);
        		model.addAttribute("message", messageValue);
        		return "changePassword";
    		}
    	}
    	UserDTO user = (UserDTO) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    	user.setPassword(password.getNewPassword());
    	APIResponse<UserDTO> responseData = restService.execute(
    			new StringBuilder(url).append(prefixUrl).append("/changePassword").toString(),
    			HttpMethod.POST,
    			null,
    			user,
    			new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
    			new HashMap<String, Object>());
    	if(responseData.getStatus()==200) {
    		HttpSession session = request.getSession();
    		SecurityContextHolder.clearContext();
    		if(session != null)
    		    session.invalidate();
    		String messageValue = messages.getMessage(
    				"auth.message.resetPasswordSuc", null, "Reset password successfully!", locale);
    		model.addAttribute("message", messageValue);
        	return "changePassword";
    	}
    	String messageValue = messages.getMessage(
				"auth.message.resetPassError", null, "Can't save your new password!", locale);
		model.addAttribute("message", messageValue);
		return "changePassword";
    }
    
    @GetMapping("changePassword")
    public String getChangePasswordPage(Model model) {
    	model.addAttribute("password", new PasswordDTO());
   	    return "changePassword";
    }

	private UserDTO createUserAccount(UserDTO userDTO) {
		UserDTO user = null;
		try {
			APIResponse<UserDTO> response = restService.execute(
					new StringBuilder(url).append(prefixUrl).toString(),
					HttpMethod.POST,
					null,
					userDTO,
					new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
					new HashMap<String, Object>());
			if(response.getStatus() == 200) {
				user = response.getData();
			}
		} catch (RestTemplateException e) {
			return null;
		}
		return user;
	}
	
	private UserDTO activeRegisteredUser(UserDTO userDTO) {
		UserDTO user = null;
		try {
			APIResponse<UserDTO> response = restService.execute(
					new StringBuilder(url).append(prefixUrl).append("/active").toString(),
					HttpMethod.POST,
					null,
					userDTO,
					new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
					new HashMap<String, Object>());
			if(response.getStatus() == 200) {
				user = response.getData();
			}
		} catch (RestTemplateException e) {
			return null;
		}
		return user;
	}
	
}

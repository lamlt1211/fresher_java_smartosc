package com.smartosc.training.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.smartosc.training.entity.AppUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartosc.training.service.impl.UserDetailServiceImpl;

@Controller
public class LoginController {
	
	@Autowired
	private UserDetailServiceImpl userDetailsService;
	
	@Value("${api.url}")
	private String url;
	
	@GetMapping("login")
	public String getLoginPage(Model model) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(!auth.getPrincipal().toString().equals("anonymousUser"))
			return "redirect:/home";
		return "login";
	}
	
	@PostMapping("authenticate")
	public String authenticateUser(
			@RequestParam(name = "username") String username,
			@RequestParam(name = "password") String password,
			Model model){
		UserDetails userDetail = userDetailsService.loadUserByUsername(
				username);
		
		Authentication auth = new UsernamePasswordAuthenticationToken(
				userDetail, null, userDetail.getAuthorities());
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		return "redirect:/home";
	}

	@GetMapping("logout")
	public String logoutPage(HttpServletRequest request, HttpServletResponse response) {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if(auth!=null) {
			new SecurityContextLogoutHandler().logout(request, response, auth);
		}
		return "redirect:/login?logout";
	}
	
}

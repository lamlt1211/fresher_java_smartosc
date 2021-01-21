package com.smartosc.training.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartosc.training.dto.RoleDTO;
import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.AppUserDetails;
import com.smartosc.training.entity.LoginRequest;
import com.smartosc.training.service.RestService;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
	
	@Autowired
	public BCryptPasswordEncoder encoder;
	
	@Autowired
	private RestService restService;
	
	@Autowired
	private HttpServletRequest request;
	
	@Value("${api.url}")
	private String url;
	
	@Value("${prefix.user}")
	private String prefixUserUrl;
	
	@Value("${prefix.role}")
	private String prefixRoleUrl;
	
	@Override
	public UserDetails loadUserByUsername(String username){
		try {
			String password = request.getParameter("password");
			String token = restService.getToken(
					new StringBuilder(url).append("generate_token").toString(),
					HttpMethod.POST,
					new LoginRequest(username, password)).getData();
			
			Map<String, Object> values = new HashMap<>();
			values.put("username", username);
			
			HttpHeaders header = new HttpHeaders();
			header.setBearerAuth(token);
			UserDTO user = restService.execute(
					new StringBuilder(url).append(prefixUserUrl+"/username/{username}").toString(),
					HttpMethod.GET,
					header,
					null,
					new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
					values).getData();
			
			List<RoleDTO> roles = restService.execute(
					new StringBuilder(url).append(prefixRoleUrl+"/username/{username}").toString(),
					HttpMethod.GET,
					header,
					null,
					new ParameterizedTypeReference<APIResponse<List<RoleDTO>>>() {},
					values).getData();
			
			List<GrantedAuthority> grantList = new ArrayList<>();
			if (!roles.isEmpty()) {
				roles.forEach(o -> {
					GrantedAuthority authority = new SimpleGrantedAuthority(o.getName());
					grantList.add(authority);
				});
			}
			return new AppUserDetails(username, encoder.encode(password), user.getFullName(), token, grantList);
		} catch (Exception e) {
			throw new UsernameNotFoundException("Username not found!", e);
		}
	}

}

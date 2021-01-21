package com.smartosc.training.service.impl;

import com.smartosc.training.dto.RoleDTO;
import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.AppUserDetails;
import com.smartosc.training.entity.LoginRequest;
import com.smartosc.training.service.RestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserDetailServiceImpl.class);

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
	public UserDetails loadUserByUsername(String username) {
		try {
			UserDTO user;
			List<RoleDTO> roles;
			List<GrantedAuthority> grantList = new ArrayList<>();
			String password = request.getParameter("password");
			String token = this.generateToken(username, password);
			
			if(token != null) {
				HttpHeaders header = new HttpHeaders();
				header.setBearerAuth(token);
				Map<String, Object> values = new HashMap<>();
				values.put("username", username);
				user = restService.execute(
						new StringBuilder(url).append(prefixUserUrl+"/username/{username}").toString(),
						HttpMethod.GET,
						header,
						null,
						new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
						values).getData();

				roles = restService.execute(
						new StringBuilder(url).append(prefixRoleUrl+"/username/{username}").toString(),
						HttpMethod.GET,
						header,
						null,
						new ParameterizedTypeReference<APIResponse<List<RoleDTO>>>() {},
						values).getData();

				if (!roles.isEmpty()) {
					roles.forEach(o -> {
						GrantedAuthority authority = new SimpleGrantedAuthority(o.getName());
						grantList.add(authority);
					});
				}
				return new AppUserDetails(username, encoder.encode(password), user.getFullName(), token, grantList);
			} else {
				return new AppUserDetails(username, encoder.encode(password), null, token, grantList);
			}
		} catch (Exception e) {
			LOGGER.error("Not found information for username like: {}", username);
			throw e;
		}
	}

	private String generateToken(String username, String password) {
		try {
			APIResponse<String> data = restService.getToken(
					new StringBuilder(url).append("generate_token").toString(),
					HttpMethod.POST,
					new LoginRequest(username, password));
			if(data.getStatus() == 200) {
				return data.getData();
			}
		} catch (Exception e) {
			LOGGER.error("Not found information for username like: {}", username);
			throw e;
		}
		return null;
	}

}

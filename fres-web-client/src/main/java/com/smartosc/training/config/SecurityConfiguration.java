package com.smartosc.training.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserDetailsService userDetailService;

	@Autowired
	public BCryptPasswordEncoder encoder;
	
	@Autowired
	private AuthenticationFailureHandler authenticationFailureHandler;
	
	@Bean(BeanIds.AUTHENTICATION_MANAGER)
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailService).passwordEncoder(encoder);
		auth.eraseCredentials(false);
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().authorizeRequests()
		.antMatchers("/css/**", "/js/**","/images/**", "/fonts/**").permitAll()
		.antMatchers(HttpMethod.POST, "/user/changePassword").hasAuthority("CHANGE_PASSWORD_PRIVILEGE")
		.anyRequest().permitAll()
		.and().formLogin()
		.loginPage("/login")
		.loginProcessingUrl("/user/authenticate")
		.defaultSuccessUrl("/home")
				/* .failureUrl("/login?error") */
		.failureHandler(authenticationFailureHandler)
		.usernameParameter("username")
		.passwordParameter("password")
		.and().logout().permitAll()
		.logoutUrl("/logout")
		.logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
		.logoutSuccessUrl("/home").deleteCookies("JSESSIONID");
		
		}

}

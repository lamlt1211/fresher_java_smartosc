package com.smartosc.training.configration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.BeanIds;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.smartosc.training.security.AuthEntryPointJwt;
import com.smartosc.training.security.JwtRequestFilter;
import com.smartosc.training.security.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
        securedEnabled = true,
        jsr250Enabled = true,
        prePostEnabled = true
)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserDetailsServiceImpl customUserDetailsService;

    @Autowired
    public JwtRequestFilter jwtRequestFilter;

    @Autowired
    private AuthEntryPointJwt authEntryPointJwt;

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(customUserDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .and()
                .csrf().disable().authorizeRequests()
                .antMatchers("users/username/**", "/categories/count").permitAll()
                .antMatchers(HttpMethod.POST, "/generate_token", "/user", "/user/generate-verify-token",
                        "/user/generate-pass-reset-token", "/user/active", "/user/changePassword").permitAll()
                .antMatchers(HttpMethod.GET, "/order/**", "/users/**", "/user/**", "/products/**", "/categories/**", "/role/**", "/promotions/**", "/bankDirectConfiguration/**", "/intermediary-banks/**", "/direct-banks/**", "/bank/**").permitAll()
                .antMatchers(HttpMethod.POST, "/products/**", "/categories/**", "/promotions/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/products/**", "/categories/**", "/promotions/**").permitAll().antMatchers(HttpMethod.DELETE, "/changeStatus/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/products/**", "/categories/**", "/promotions/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/user/update", "/changeStatus/**").hasAnyRole("USER", "ADMIN")
                .antMatchers(HttpMethod.POST, "/order/**").hasAnyRole("USER", "ADMIN")
                .anyRequest().authenticated()
                .and().exceptionHandling().authenticationEntryPoint(authEntryPointJwt)
                .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/resources/**", "/static/**", "/css/**", "/js/**", "/images/**");
    }


}

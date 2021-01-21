package com.smartosc.training.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.AppUserDetails;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.JWTUtils;

@Controller
@RequestMapping("/")
public class UserController {
	
	@Autowired
    private RestService restService;

    @Autowired
    private JWTUtils jwtTokenUtil;

    @Value("${prefix.user}")
    private String prefixUrl;

    @Value("${api.url}")
    private String url;

    @GetMapping("profile")
    public String profile(Model model) {
        AppUserDetails userLogin = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDTO user = null;
        Map values = new HashMap<String, Object>();
        values.put("username", userLogin.getUsername());
        APIResponse<UserDTO> responseData = restService.execute(
                new StringBuilder(url).append(prefixUrl).append("/username/{username}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<UserDTO>>() {},
                values);

        if(responseData.getStatus()==200) {
            user = responseData.getData();
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("profile")
    public String alo(@ModelAttribute("user") UserDTO userDTO){
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        header.setContentType(MediaType.APPLICATION_JSON);
        restService.execute(
        		url + prefixUrl+"/update",
                HttpMethod.PUT,
                header,
                userDTO,
                new ParameterizedTypeReference<APIResponse<UserDTO>>() {});
        return "redirect:/profile";
    }
	
}

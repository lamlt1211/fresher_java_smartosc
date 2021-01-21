package com.smartosc.training.controller;

import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {

	@Value("${api.url}")
	private String url;

	@Value("${prefix.product}")
	private String prefixProductUrl;
	
	@Value("${prefix.user}")
	private String prefixUserUrl;
	
	@Value("${prefix.order}")
	private String prefixOrderUrl;
	
	@Autowired
	private RestService restService;

	@GetMapping
	public String redirectToHomePage() {
		return "redirect:/home";
	}

	@GetMapping("home")
	public String getIndex(Model model) {
		Long orderNum = restService.execute(
				new StringBuilder(url).append(prefixOrderUrl).append("/count").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<Long>>() {}).getData();
		
		Long userNum = restService.execute(
				new StringBuilder(url).append(prefixUserUrl).append("/count").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<Long>>() {}).getData();
		
		Long categoryNum = restService.execute(
				new StringBuilder(url).append("categories").append("/count").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<Long>>() {}).getData();
		
		Long productNum = restService.execute(
				new StringBuilder(url).append(prefixProductUrl+"s").append("/count").toString(),
				HttpMethod.GET,
				null,
				null,
				new ParameterizedTypeReference<APIResponse<Long>>() {}).getData();
		
		model.addAttribute("userNum", userNum);
		model.addAttribute("categoryNum", categoryNum);
		model.addAttribute("productNum", productNum);
		model.addAttribute("orderNum", orderNum);
		
		return "index";
	}

}

package com.smartosc.training.controller;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HandleErrorController implements ErrorController {
	
	@GetMapping("/error")
	public String handleError(HttpServletRequest request, Model model) {
		Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
		if(status != null) {
			Integer statusCode = Integer.valueOf(status.toString());
			model.addAttribute("status", statusCode);
			switch (statusCode) {
			case 500:
				model.addAttribute("message", "Internal Server Error");
				break;
			case 404:
				model.addAttribute("message", "Resource not found");
				break;
			case 403:
				model.addAttribute("message", "Forbidden");
				break;
			default:
				model.addAttribute("message", "Unknow error");
				break;
			}
		}
		return "error";
	}
	
	@Override
	public String getErrorPath() {
		return "/error";
	}

}

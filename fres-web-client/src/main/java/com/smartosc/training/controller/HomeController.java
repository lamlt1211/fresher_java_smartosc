package com.smartosc.training.controller;

import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.CartInfo;
import com.smartosc.training.service.ProductService;
import com.smartosc.training.utils.CartSupportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {
	private static final Logger LOGGER = LoggerFactory.getLogger(HomeController.class);
	@Autowired
    private ProductService productService;
	
	@GetMapping
	public String redirectToHomePage() {
		return "redirect:/home";
	}

	@GetMapping("home")
	public String getHomePage(Model model, HttpServletRequest request) {
		LOGGER.info("getHomePage");
		CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
		model.addAttribute("sizeCart", cartInfo.getCartLines().size());


		List<ProductDTO> listHotProducts = productService.getHotProducts();
		model.addAttribute("listHotProducts", listHotProducts);

		List<ProductDTO> listNewestProducts = productService.getTop8NewestPromotedProducts();
		model.addAttribute("listNewestProducts", listNewestProducts);

		List<ProductDTO> products = productService.getTopNewProduct();
		model.addAttribute("products", products);
		LOGGER.info("products = {}, listHotProducts = {}", products.size(), listHotProducts.size());
		return "index";
	}

}
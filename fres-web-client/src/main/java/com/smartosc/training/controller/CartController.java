package com.smartosc.training.controller;

import javax.servlet.http.HttpServletRequest;

import com.smartosc.training.entity.CartLineInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.CartInfo;
import com.smartosc.training.service.ProductService;
import com.smartosc.training.utils.CartSupportUtils;

@Controller
@RequestMapping("/cart")
public class CartController {
	
	@Autowired
    private ProductService productService;

    @GetMapping
    public String view(HttpServletRequest request, Model model) {
        CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
        model.addAttribute("myCart", cartInfo);
        model.addAttribute("myCartProduct", cartInfo.getCartLines());
        model.addAttribute("sizeCart", cartInfo.getCartLines().size());
        return "cart";
    }

    @GetMapping("/buycart")
    @ResponseBody
    public Integer buyProduct(HttpServletRequest request,
                          @RequestParam(value = "id", defaultValue = "") Integer id,
                          @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {
        ProductDTO productDTO = null;
        if (id != null && id > 0) {
            productDTO = productService.findById(id);
        }

        if (productDTO != null) {
            CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
            ProductDTO product = new ProductDTO(productDTO);
            cartInfo.addProduct(product, quantity);
            return cartInfo.getCartLines().size();
        }
        else {
            return 0;
        }
    }

    @GetMapping("/removecart" )
    @ResponseBody
    public Integer removeProduct(HttpServletRequest request,
                                 @RequestParam(value = "id", defaultValue = "") Integer id) {
        ProductDTO productDTO = null;
        if (id != null && id > 0) {
            productDTO = productService.findById(id);
        }
        if (productDTO != null) {

            CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
            ProductDTO product = new ProductDTO(productDTO);
            cartInfo.removeProduct(product);
            return cartInfo.getCartLines().size();
        }
        else {
            return 0;
        }
    }
    @GetMapping("/updatecart" )
    @ResponseBody
    public Double updateProduct(HttpServletRequest request,
                                @RequestParam(value = "id", defaultValue = "") Integer id,
                                @RequestParam(value = "quantity", defaultValue = "1") Integer quantity) {
        ProductDTO productDTO = null;
        if (id != null && id > 0) {
            productDTO = productService.findById(id);
        }
        if (productDTO != null) {
            CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
            ProductDTO product = new ProductDTO(productDTO);
            cartInfo.addProduct2(product, quantity);
            CartLineInfo line = cartInfo.findLineByCode(productDTO.getProductId());
            return line.getAmount();
        }
        return 0.0;
    }
	
}

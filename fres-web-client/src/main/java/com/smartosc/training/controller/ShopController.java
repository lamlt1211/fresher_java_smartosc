package com.smartosc.training.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartosc.training.dto.CategoryDTO;
import com.smartosc.training.dto.PageMetaData;
import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.CartInfo;
import com.smartosc.training.service.CategoryService;
import com.smartosc.training.service.ProductService;
import com.smartosc.training.utils.CartSupportUtils;

@Controller
public class ShopController {
	
	@Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/shop")
    public String page(@RequestParam(name = "categoryId", required = true, defaultValue = "0") Integer categoryId,
                       @RequestParam(name = "page", required = false, defaultValue = "0") Integer page,
                       @RequestParam(name = "size", required = false, defaultValue = "16") Integer size,
                       ModelMap modelMap, HttpServletRequest request) {
        if (categoryId == 0){
            CategoryDTO categoryDTO = new CategoryDTO();
            categoryDTO.setCategoryId(0);
            modelMap.addAttribute("categoryDTO", categoryDTO);

            APIResponse<List<ProductDTO>> responseDTO = productService.getAllpageProduct(PageRequest.of(page, size));
            PageMetaData metaData = responseDTO.getPageMetadata();
            modelMap.addAttribute("listProduct", responseDTO.getData());
            if (metaData != null) {
                modelMap.addAttribute("pages", metaData);
            }
        }
        else {
            CategoryDTO categoryDTO = categoryService.getCategoryById(categoryId);
            modelMap.addAttribute("categoryDTO", categoryDTO);

            APIResponse<List<ProductDTO>> responseDTO = productService.getProductByCategoryId(categoryId, PageRequest.of(page, size));
            PageMetaData metaData = responseDTO.getPageMetadata();
            modelMap.addAttribute("listProduct", responseDTO.getData());
            if (metaData != null) {
                modelMap.addAttribute("pages", metaData);
            } else {
            	metaData = new PageMetaData();
            	metaData.setFirst(false);
            	metaData.setLast(false);
            	metaData.setNumber(0);
            	metaData.setPage(0);
            	metaData.setSize(0);
            	metaData.setTotalElements(0);
            	metaData.setTotalPages(0);
            	modelMap.addAttribute("pages", metaData);
            }
        }
        List<CategoryDTO> listCategory = categoryService.getAllCategory();
        modelMap.addAttribute("listCategory", listCategory);

        CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
        modelMap.addAttribute("sizeCart", cartInfo.getCartLines().size());
        return "shop";
    }

    @GetMapping("/shop/{name}")
    public String getProductByCategory(@PathVariable(name = "name") String categoryName, Model model){
        List<ProductDTO> listProduct = productService.getProductByCategoryName(categoryName);
        List<CategoryDTO> listCategory = categoryService.getAllCategory();

        model.addAttribute("listCategory", listCategory);
        model.addAttribute("listProduct", listProduct);
        return "shop";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable(name = "id") Integer id,
                         Model model, HttpServletRequest request) {
        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);

        List<ProductDTO> listHotProducts = productService.getHotProducts();
        model.addAttribute("listHotProducts", listHotProducts);

        CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
        model.addAttribute("sizeCart", cartInfo.getCartLines().size());
        return "product-detail";
    }
    @GetMapping("/{id}")
    public String category(@PathVariable(name = "id") Integer id,
                         Model model) {
        CategoryDTO category = categoryService.getCategoryById(id);

        model.addAttribute("category", category);
        return "shop";
    }
	
}

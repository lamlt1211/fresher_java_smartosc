package com.smartosc.training.controller;

import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.dto.PromotionDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.JWTUtil;
import com.smartosc.training.utils.RestPageImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/promotions")
public class PromotionController {

    @Value("${api.url}")
    private String url;
    @Value("${prefix.promotion}s")
    private String urlPrefix;
    @Autowired
    private RestService restService;
    @Autowired

    private JWTUtil jwtUtil;
    private static String promotionId ="/{id}";

    @PostMapping("/search")
    public String getListAllPromotionBySearchValue(@RequestParam("table_search") String searchValue) {
        return "redirect:/promotions?searchValue=" + searchValue;
    }

    @GetMapping
    public String getPromotionPage(Model model,
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer pageNo,
            @RequestParam(defaultValue = "10", required = false) Integer pageSize,
            @RequestParam(defaultValue = "promotionId", required = false) String sortBy) {
        RestPageImpl<PromotionDTO> result = null;
        Map<String, Object> values = new HashMap<>();
        values.put("searchValue", searchValue);
        values.put("pageNo", pageNo);
        values.put("pageSize", pageSize);
        values.put("sortBy", sortBy);
        APIResponse<RestPageImpl<PromotionDTO>> responseData = restService.execute(
                new StringBuilder(url).append(urlPrefix)
                        .append("?searchValue={searchValue}&page={pageNo}&size={pageSize}&sortBy={sortBy}")
                        .toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<RestPageImpl<PromotionDTO>>>() {},
                values
        );

        if(responseData.getStatus() == HttpStatus.OK.value()) {
            result = responseData.getData();
        }

        model.addAttribute("data", result);
        model.addAttribute("dataPageImpl", result);
        model.addAttribute("currentPage", pageNo);
        model.addAttribute("searchValue", searchValue);
        return "promotionManagement";
    }

    @GetMapping("/create")
    public String getUpdatePromotionPage(Model model,
                                         @RequestParam(name = "id", required = false) Integer id) {
        PromotionDTO promotion = null;
        List<ProductDTO> products = this.getProducts();
        if(id != null) {
            Map<String, Object> values = new HashMap<>();
            values.put("id", id);
            promotion = restService.execute(
                    new StringBuilder(url).append(urlPrefix).append(promotionId).toString(),
                    HttpMethod.GET,
                    null,
                    null,
                    new ParameterizedTypeReference<APIResponse<PromotionDTO>>() {
                    },
                    values
            ).getData();
            promotion.setProductDTOIds(promotion.getProductDTOs().stream()
                    .map(ProductDTO::getProductId)
                    .collect(Collectors.toList()));
        } else {
            promotion = new PromotionDTO();
            promotion.setStatus(1);
        }

        model.addAttribute("promotion", promotion);
        model.addAttribute("products", products);
        return "add-promotion";
    }

    @PostMapping
    public String updatePromotion(
            @ModelAttribute("promotion") @Valid PromotionDTO promotionDTO,
            BindingResult result, Model model) {
        PromotionDTO promotion = null;
        if(result.hasErrors()) {
            model.addAttribute("promotion", promotionDTO);
            model.addAttribute("products", this.getProducts());
            return "add-promotion";
        }

        if(promotionDTO.getPromotionId() != null) {
            promotion = this.editPromotion(promotionDTO, promotionDTO.getPromotionId());
        } else {
            promotion = this.createPromotion(promotionDTO);
        }

        return promotion != null ? "redirect:/promotions" : "redirect:/promotions/create";
    }

    @PostMapping("/delete")
    public String deletePromotion(
            @RequestParam(name = "searchValue", defaultValue = "", required = false) String searchValue,
            @RequestParam("id") Integer id) {
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(jwtUtil.getJwtTokenFromSecurityContext());
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        restService.execute(
                new StringBuilder(url).append(urlPrefix).append(promotionId).toString(),
                HttpMethod.DELETE,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<PromotionDTO>>() {},
                values
        );

        return "redirect:/promotions?searchValue=" + searchValue;
    }

    private PromotionDTO createPromotion(PromotionDTO promotionDTO) {
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(jwtUtil.getJwtTokenFromSecurityContext());
        APIResponse<PromotionDTO> responseData = restService.execute(
                new StringBuilder(url).append(urlPrefix).toString(),
                HttpMethod.POST,
                header,
                promotionDTO,
                new ParameterizedTypeReference<APIResponse<PromotionDTO>>() {},
                new HashMap<>()
        );

        if(responseData.getStatus() == HttpStatus.OK.value()) {
            return responseData.getData();
        } else {
            return null;
        }
    }

    private PromotionDTO editPromotion(PromotionDTO promotionDTO, Integer id) {
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(jwtUtil.getJwtTokenFromSecurityContext());
        Map<String, Object> values = new HashMap<>();
        values.put("id", id);
        APIResponse<PromotionDTO> responseData = restService.execute(
                new StringBuilder(url).append(urlPrefix).append(promotionId).toString(),
                HttpMethod.PUT,
                header,
                promotionDTO,
                new ParameterizedTypeReference<APIResponse<PromotionDTO>>() {},
                values
        );

        if(responseData.getStatus() == HttpStatus.OK.value()) {
            return responseData.getData();
        } else {
            return null;
        }
    }

    private List<ProductDTO> getProducts() {
        APIResponse<List<ProductDTO>> result = restService.execute(
                new StringBuilder(url).append("products/all").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {},
                new HashMap<>()
        );

        return result.getStatus() == HttpStatus.OK.value() ? result.getData() : null;
    }

}

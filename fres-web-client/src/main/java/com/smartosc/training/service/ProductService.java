package com.smartosc.training.service;


import com.smartosc.training.dto.ProductDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.impl.RestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {

    @Autowired
    private RestServiceImpl restTemplateService;

    @Value("${api.url}")
    private String url;

    @Value("${prefix.product}")
    private String preUrl;


    public ProductDTO findById(Integer productId) {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/" + productId).toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                },
                new HashMap<String, Object>()).getData();
    }

    public APIResponse<List<ProductDTO>> getAllpageProduct(Pageable pageable) {
        Map<String, Object> values = new HashMap<>();
        values.put("page", pageable.getPageNumber());
        values.put("size", pageable.getPageSize());
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/promotions/{page}/{size}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                },
                values);
    }

    public APIResponse<List<ProductDTO>> getAllpageProductHasPromotion(Pageable pageable) {
        Map<String, Object> values = new HashMap<>();
        values.put("page", pageable.getPageNumber());
        values.put("size", pageable.getPageSize());
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/{page}/{size}").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                },
                values);
    }

    public APIResponse<List<ProductDTO>> getProductByCategoryId(Integer categoryId, Pageable pageable) {
        return restTemplateService.exchangePaging(
                new StringBuilder(url).append(preUrl).append("s/" + categoryId).append("/").append(pageable.getPageNumber()).append("/").append(+pageable.getPageSize()).toString(),
                HttpMethod.GET,
                null,
                null);
    }


    public ProductDTO getProductById(int id) {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/" + id).toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<ProductDTO>>() {
                }).getData();
    }

    public List<ProductDTO> getProductByCategoryName(String categoryName) {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/category/name/").append(categoryName).toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                }).getData();
    }

    public List<ProductDTO> getTopNewProduct() {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/newest-all").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                }).getData();
    }

    public List<ProductDTO> getHotProducts() {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/hot").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                }).getData();
    }

    public List<ProductDTO> getTop8NewestPromotedProducts() {
        return restTemplateService.execute(
                new StringBuilder(url).append(preUrl).append("s/newest-promoted").toString(),
                HttpMethod.GET,
                null,
                null,
                new ParameterizedTypeReference<APIResponse<List<ProductDTO>>>() {
                }).getData();
    }
}

package com.smartosc.training.service.impl;

import com.smartosc.training.dto.AppCodeDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RestService;
import com.smartosc.training.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * fres-parent
 *
 * @author Hieupv
 * @created_at 21/04/2020 - 10:17 AM
 * @created_by Hieupv
 * @since 21/04/2020
 */
@Service
public class AppCodeServiceImpl {
    @Value("${api.url}")
    private String url;

    @Value("${prefix.appcode}")
    private String prefixUrl;

    @Autowired
    private RestService restService;

    @Autowired
    private JWTUtil jwtTokenUtil;

    //get all appcode
    public APIResponse<List<AppCodeDTO>> getAllAppCode(Pageable pageable) {
        return restService.execute(new StringBuilder(url).append(prefixUrl).append("/" + pageable.getPageNumber() + "/" + pageable.getPageSize()).toString(),
                HttpMethod.GET, getHeader(), null, new ParameterizedTypeReference<APIResponse<List<AppCodeDTO>>>() {
                });
    }


    //update appcode
    public AppCodeDTO updateAppCode(AppCodeDTO appCodeDTO) {
        return restService.execute(new StringBuilder(url).append(prefixUrl).toString(), HttpMethod.PUT, getHeader(), appCodeDTO, new ParameterizedTypeReference<APIResponse<AppCodeDTO>>() {
        }).getData();
    }

    //Search appcode by code or description
    public List<AppCodeDTO> searchAppCode(String searchValue) {
        return restService.execute(new StringBuilder(url).append(prefixUrl).append("/" + searchValue).toString(), HttpMethod.GET, getHeader(), null, new ParameterizedTypeReference<APIResponse<List<AppCodeDTO>>>() {
        }).getData();
    }

    //add new appcode
    public AppCodeDTO addNewAppCode(AppCodeDTO appCodeDTO) {
        return restService.execute(new StringBuilder(url).append(prefixUrl).append("/add").toString(), HttpMethod.POST, getHeader(), appCodeDTO, new ParameterizedTypeReference<APIResponse<AppCodeDTO>>() {
        }).getData();
    }

    //header sets token
    public HttpHeaders getHeader() {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        return header;
    }

    //find appcode by id
    public AppCodeDTO findAppCodeById(int id) {
        return restService.execute(new StringBuilder(url).append(prefixUrl).append("/find/" + id).toString(), HttpMethod.GET, getHeader(), null, new ParameterizedTypeReference<APIResponse<AppCodeDTO>>() {
        }).getData();
    }
}

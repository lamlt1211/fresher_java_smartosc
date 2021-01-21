package com.smartosc.training.service;

import com.smartosc.training.dto.OrdersDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.impl.RestServiceImpl;
import com.smartosc.training.utils.JWTUtils;
import org.hibernate.criterion.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderService {

    @Autowired
    private RestServiceImpl restService;

    @Autowired
    private JWTUtils jwtTokenUtil;

    @Value("${api.url}")
    private String url;

    @Value("${prefix.order}")
    private String preUrl;


    public List<OrdersDTO> getAllOrder(String username, Integer status) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        return restService.execute(
                new StringBuilder(url).append(preUrl).append("/member?username=").append(username).append("&status=").append(status).toString(),
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<List<OrdersDTO>>>() {
                }).getData();
    }

    public OrdersDTO viewOrder (Integer orderId) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        return restService.execute(
                new StringBuilder(url).append(preUrl).append("/view?orderId=").append(orderId).toString(),
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<OrdersDTO>>() {
                }).getData();
    }


    public Boolean cancelOrder(Integer orderId) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        return restService.execute(
                new StringBuilder(url).append(preUrl).append("/cancel?orderId=").append(orderId).toString(),
                HttpMethod.GET,
                header,
                null,
                new ParameterizedTypeReference<APIResponse<Boolean>>() {
                }).getData();
    }

    public boolean createOrder(OrdersDTO ordersDTO) {
        String authToken = jwtTokenUtil.getJwtTokenFromSecurityContext();
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(authToken);
        if (ordersDTO != null) {
            restService.execute(
                    new StringBuilder(url).append(preUrl).toString(),
                    HttpMethod.POST,
                    header,
                    ordersDTO,
                    new ParameterizedTypeReference<APIResponse<OrdersDTO>>() {
                    }).getData();
            return true;
        } else return false;
    }
}

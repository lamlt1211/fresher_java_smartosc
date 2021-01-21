package com.smartosc.training.service.impl;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.LoginRequest;
import com.smartosc.training.exception.RestTemplateException;
import com.smartosc.training.service.RestService;


@Service
public class RestServiceImpl implements RestService {
	
	private static final Logger logger = LoggerFactory.getLogger(RestServiceImpl.class);
	
	@Autowired
	private RestTemplate restTemplate;
	
	@Override
	public <T> APIResponse<T> execute(String url, HttpMethod method, HttpHeaders headers,
			Object body, ParameterizedTypeReference<APIResponse<T>> type,
			Map<String, Object> values){
		try {	
			HttpEntity<Object> entity = new HttpEntity<>(body, headers);
			ResponseEntity<APIResponse<T>> response = restTemplate.exchange(
					url,
					method,
					entity,
					type,
					values);
			if(response.getStatusCodeValue() >= HttpStatus.OK.value()
					&& response.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
				return response.getBody();
			}
			logger.info("Can't get data from API - {}", response.getBody().getMessage());
			throw new RestTemplateException(response.getBody().getMessage());
		} catch (Exception e) {
			logger.info("Some error has occur when call API - {}", e.getMessage());
			throw new RestTemplateException(e.getMessage(), e);
		}
	}
	
	@Override
	public <T> APIResponse<T> execute(String url, HttpMethod method, HttpHeaders headers,
			Object body, ParameterizedTypeReference<APIResponse<T>> type){
		try {	
			HttpEntity<Object> entity = new HttpEntity<>(body, headers);
			ResponseEntity<APIResponse<T>> response = restTemplate.exchange(
					url,
					method,
					entity,
					type);
			if(response.getStatusCodeValue() >= HttpStatus.OK.value()
					&& response.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
				return response.getBody();
			}
			logger.info("Can't get data from API - {}", response.getBody().getMessage());
			throw new RestTemplateException(response.getBody().getMessage());
		} catch (Exception e) {
			logger.info("Some error has occur when call API - {}", e.getMessage());
			throw new RestTemplateException(e.getMessage(), e);
		}
	}

	@Override
	public APIResponse<String> getToken(String url, HttpMethod method, LoginRequest loginRequest){
		try {
			HttpEntity<Object> entity = new HttpEntity<>(loginRequest);
			ResponseEntity<APIResponse<String>> response = restTemplate.exchange(
					url,
					method,
					entity,
					new ParameterizedTypeReference<APIResponse<String>>() {});
			if(response.getStatusCodeValue() >= HttpStatus.OK.value() && response.getStatusCodeValue() < HttpStatus.MULTIPLE_CHOICES.value()) {
				return response.getBody();
			}
			logger.info("Can't get data from API - {}", response.getBody().getMessage());
			throw new RestTemplateException(response.getBody().getMessage());
		} catch (Exception e) {
			logger.info("Some error has occur when call API - {}", e.getMessage());
			throw new RestTemplateException(e.getMessage(), e);
		}
	}
}
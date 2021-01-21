package com.smartosc.training.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartosc.training.dto.RoleDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.RoleService;

@RestController
@RequestMapping("/")
public class RoleController {

    @Autowired
    private RoleService roleService;
    
    @GetMapping("role/username/{username}")
    public ResponseEntity<APIResponse<List<RoleDTO>>> findRoleByUsername(
    		@PathVariable("username") String username) {
    	APIResponse<List<RoleDTO>> responseData = new APIResponse<>();
        List<RoleDTO> roleDTOs = roleService.getAllRoleByUserName(username);
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("MyResponseHeader", "acd");
        responseData.setMessage("Find by name successful");
        responseData.setData(roleDTOs);
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, responseHeaders, HttpStatus.OK);
    }
    
}

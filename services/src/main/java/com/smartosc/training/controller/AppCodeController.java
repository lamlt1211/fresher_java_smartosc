package com.smartosc.training.controller;

import com.smartosc.training.dto.AppCodeDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.AppCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

/**
 * fres-parent
 *
 * @author Namtt
 * @created_at 20/04/2020 - 5:58 PM
 * @created_by Namtt
 * @since 20/04/2020
 */

@RestController
@RequestMapping("/appCode")
public class AppCodeController {

    private final AppCodeService appCodeService;

    @Autowired
    public AppCodeController(AppCodeService appCodeService){
        this.appCodeService = appCodeService;
    }

    //List all app code with pageable
    @GetMapping("/{pageNo}/{pageSize}")
    public ResponseEntity<APIResponse<List<AppCodeDTO>>> getAllAppCode(@PathVariable("pageNo") int pageNo, @PathVariable("pageSize") int pageSize) {
        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<AppCodeDTO> dtos = appCodeService.getAllAppCode(pageable);
        return new ResponseEntity(appCodeService.getResponseData(dtos, pageNo, pageSize), HttpStatus.OK);
    }

    /**
     * @param appCodeDTO
     * @return
     * @author Namtt
     */

    //add new app code
    @PostMapping("/add")
    public ResponseEntity<Object> addAppCode(@RequestBody AppCodeDTO appCodeDTO) {
        appCodeService.addAppCode(appCodeDTO);
        APIResponse<AppCodeDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Add successful");
        responseData.setData(appCodeDTO);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    /*
     * author: Hieu
     * function: findByCodeOrDescription; updateAppCode; deleteAppCode
     * */
    //find appcode by id
    @GetMapping("/find/{id}")
    public ResponseEntity<Object> findAppCodeById(@PathVariable("id") int id) {
        APIResponse<AppCodeDTO> responseData = new APIResponse<>();
        responseData.setMessage("Find by code or description successfull");
        responseData.setData(appCodeService.findAppCodeById(id));
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, HttpStatus.OK);

    }

    //search appcode
    @GetMapping("/{searchvalue}")
    public ResponseEntity<Object> findByCodeOrDescription(@PathVariable("searchvalue") String searchValue) {
        APIResponse<List<AppCodeDTO>> responseData = new APIResponse<>();
        responseData.setMessage("Find by code or description successfull");
        responseData.setData(appCodeService.findByCodeOrDescription(searchValue));
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    //update appcode
    @PutMapping
    public ResponseEntity<Object> updateAppCode(@RequestBody AppCodeDTO appCodeDTO) {
        APIResponse<AppCodeDTO> responseData = new APIResponse<>();
        responseData.setMessage("Update successfully");
        responseData.setData(appCodeService.saveAppCode(appCodeDTO));
        responseData.setStatus(HttpStatus.OK.value());
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }


}

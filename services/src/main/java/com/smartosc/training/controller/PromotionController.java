package com.smartosc.training.controller;

import com.smartosc.training.dto.PromotionDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.PromotionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/promotions")
public class PromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<APIResponse<Page<PromotionDTO>>> findAllPromotion(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "promotionId", required = false) String sortBy) {
        Page<PromotionDTO> result = promotionService.findAllPromotion(
                searchValue, page, size, sortBy);
        APIResponse<Page<PromotionDTO>> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Find list promotion successfully!");
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

    @GetMapping("/{id}")
    public ResponseEntity<APIResponse<PromotionDTO>> findById(@PathVariable("id") Integer id) {
        PromotionDTO result = promotionService.findById(id);
        APIResponse<PromotionDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        if(result!=null) {
            responseData.setStatus(HttpStatus.OK.value());
            responseData.setMessage("Get promotion with ID = " + id + " successfully!");
        } else {
            responseData.setStatus(HttpStatus.NOT_FOUND.value());
            responseData.setMessage("Get promotion with ID = " + id + " failed!");
        }
        return ResponseEntity.ok(responseData);
    }

    @PostMapping
    public ResponseEntity<APIResponse<PromotionDTO>> createPromotion(
            @RequestBody PromotionDTO promotionDTO) {
        PromotionDTO result = promotionService.createAndUpdatePromotion(promotionDTO, promotionDTO.getPromotionId());
        APIResponse<PromotionDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Add new promotion successfully!");
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<PromotionDTO>> updatePromotion(
            @RequestBody PromotionDTO promotionDTO, @PathVariable("id") Integer id) {
        PromotionDTO result = promotionService.createAndUpdatePromotion(promotionDTO, id);
        APIResponse<PromotionDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Edit promotion successfully!");
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<APIResponse<PromotionDTO>> deletePromotion(
            @PathVariable("id") Integer id) {
        PromotionDTO result = promotionService.deletePromotion(id);
        APIResponse<PromotionDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        if(result != null) {
            responseData.setStatus(HttpStatus.OK.value());
            responseData.setMessage("Delete promotion with ID = " + id + " successfully!");
        } else {
            responseData.setStatus(HttpStatus.NOT_FOUND.value());
            responseData.setMessage("Delete promotion with ID = " + id + " failed!");
        }
        return ResponseEntity.ok(responseData);
    }

}

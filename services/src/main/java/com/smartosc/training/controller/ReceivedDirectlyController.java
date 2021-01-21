package com.smartosc.training.controller;

import com.smartosc.training.dto.ReceivedDirectlyBankDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.ReceivedDirectlyBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * fres-parent
 *
 * @author thanhttt
 * @created_at 21/04/2020 - 10:46 AM
 */
@Slf4j
@RestController
@RequestMapping("/banks")
public class ReceivedDirectlyController {

    @Autowired
    private ReceivedDirectlyBankService directlyBankService;

    private final String ADD_MESS = "Add successful";

    // Get all database from bank table with status = 1
    @GetMapping("/byStatus")
    public ResponseEntity<?> getBankByStatus() {

        APIResponse<List<ReceivedDirectlyBankDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(directlyBankService.getBankByStatus());
        responseData.setMessage("Successful: List All Bank With Status = 1");

        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Add type = "DIRECT" for bank and add data to bank_detail only when this bank' s status =1
    @PostMapping
    public ResponseEntity<Object> createReceivedDirectlyBankCatalog(@RequestBody ReceivedDirectlyBankDTO receivedDirectlyBankDTO) throws Exception {
        APIResponse<ReceivedDirectlyBankDTO> responseData = new APIResponse<>();
        directlyBankService.createReceivedDirectlyBankCatalog(receivedDirectlyBankDTO);
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage(ADD_MESS);
        responseData.setData(receivedDirectlyBankDTO);
        log.info("Successful: Add Received Directly Bank Successfully");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Show bank detail by ID
    @GetMapping("/{id}")
    public ResponseEntity<Object> findBankById(@PathVariable("id") Integer id) throws ResourceNotFoundException {
        APIResponse<ReceivedDirectlyBankDTO> responseData = new APIResponse<>();
        responseData.setMessage("Find received directly bank by Id successfully");
        responseData.setData(directlyBankService.findBankDetailById(id));
        responseData.setStatus(HttpStatus.OK.value());
        log.info("Successful: Find Bank Detail by id = " + id);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    // Convert status in Bank Detail (for Received directly bank) from 1-Active or 0-Inactive, and conversely
    @PutMapping("/{id}")
    public ResponseEntity<Object> updateReceivedDirectlyBankCatalog(@RequestBody ReceivedDirectlyBankDTO receivedDirectlyBankDTO, @PathVariable("id") Integer id) {
        APIResponse<ReceivedDirectlyBankDTO> responseData = new APIResponse<>();
        directlyBankService.updateReceivedDirectlyBankCatalog(receivedDirectlyBankDTO, id);
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Update received directly bank successfully");
        log.info("Update Direct Bank" + receivedDirectlyBankDTO.getCode() + "Successfully");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}

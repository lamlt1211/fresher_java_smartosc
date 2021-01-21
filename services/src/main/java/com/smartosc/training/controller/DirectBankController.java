package com.smartosc.training.controller;

import com.smartosc.training.dto.BankDTO;
import com.smartosc.training.dto.DirectBankDTO;
import com.smartosc.training.dto.IntermediaryBankDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * fres-parent
 *
 * @author Huupd
 * @created_at 23/04/2020 - 2:27 PM
 * @created_by Huupd
 */
@Slf4j
@Controller
@RequestMapping(value = "/direct-banks")
public class DirectBankController {
    private BankService bankService;

    @Autowired
    public DirectBankController(BankService bankService){
        this.bankService = bankService;
    }

//Get all directBank
    @GetMapping()
    public ResponseEntity<APIResponse<Page<DirectBankDTO>>> findAllDirectBank(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "5", required = false) Integer size,
            @RequestParam(defaultValue = "bankId", required = false) String sortBy) {

        log.info("Find All direct banks");
        log.trace(String.format("search key: %s; page: %d; size: %d, sort by: %s ", searchValue, page, size, sortBy));

        Page<DirectBankDTO> result = bankService.findAllDirectBank(searchValue, page, size, sortBy);
        APIResponse<Page<DirectBankDTO>> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Find list direct bank successfully!");
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

}

package com.smartosc.training.controller;

import com.smartosc.training.dto.BankDirectConfigDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.repositories.BankDetailRepository;
import com.smartosc.training.service.BankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/**
 * @author duongnch
 * @since 20/04/2020
 */
@Slf4j
@RestController
@RequestMapping("/bankDirectConfiguration")
public class BankConfigController {
    @Autowired
    BankDetailRepository bankDetailRepository;
    @Autowired
    BankService bankService;

    // API return list send bank configuration
    @GetMapping("/direct-bank-configuration")
    public ResponseEntity<APIResponse<Page<BankDirectConfigDTO>>> getAllDirectbankConfiguration(@RequestParam(defaultValue = "", required = false) String searchValue,
                                                                                                @RequestParam(defaultValue = "0", required = false) int default_page,
                                                                                                @RequestParam(defaultValue = "5", required = false) int page_size) {

            Pageable pageable = PageRequest.of(default_page, page_size);
            Page<BankDirectConfigDTO> bankDirectConfigDTOList = bankService.getAllDirectBankConfiguration(pageable,searchValue);
            APIResponse<Page<BankDirectConfigDTO>> responseData = new APIResponse<>();
            responseData.setStatus(HttpStatus.OK.value());
            responseData.setData(bankDirectConfigDTOList);
            responseData.setMessage("Get all DirectBankConfiguration successfully");
            return new ResponseEntity<>(responseData, HttpStatus.OK);

    }

}




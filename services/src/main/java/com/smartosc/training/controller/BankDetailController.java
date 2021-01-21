package com.smartosc.training.controller;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.request.UpdateInterBankReq;
import com.smartosc.training.service.BankDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */

//Handler ngân hàng trung gian
@Slf4j
@RestController
@RequestMapping("/bank")
public class BankDetailController {

    private final BankDetailService bankDetailService;

    @Autowired
    public BankDetailController(BankDetailService bankDetailService) {
        this.bankDetailService = bankDetailService;
    }
    /**
     * @param req
     * @param id
     * @return
     * @author anhdt2
     */

}

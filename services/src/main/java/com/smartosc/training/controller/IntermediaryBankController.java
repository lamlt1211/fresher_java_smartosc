package com.smartosc.training.controller;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.dto.IntermediaryBankDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.request.UpdateInterBankReq;
import com.smartosc.training.service.BankDetailService;
import com.smartosc.training.service.IntermediaryBankService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


/**
 * fres-parent
 *
 * @author andy98
 * @created_at 20/04/2020 - 13:18
 * @created_by andy98
 * @since 20/04/2020
 */
@Slf4j
@Controller
@RequestMapping(value = "/intermediary-banks")
public class IntermediaryBankController {

    private final IntermediaryBankService interBankService;

    private final BankDetailService bankDetailService;

    @Autowired
    public IntermediaryBankController(IntermediaryBankService interBankService, BankDetailService bankDetailService){
        this.interBankService = interBankService;
        this.bankDetailService = bankDetailService;
    }

    /**
     * POST /intermediary-banks : Add an intermediary bank
     * @param interBankDTO
     * @return the ResponseEntity with status 201 (Created) and with body the new ,
     *          or with status 400 (Bad Request) if the program has already an ID
     */
    @PostMapping
    public ResponseEntity<APIResponse<IntermediaryBankDTO>> addIntermediaryBank(
            @RequestBody IntermediaryBankDTO interBankDTO) {

        log.info("Connected to Add intermediary bank API");

        IntermediaryBankDTO result = interBankService.addIntermediaryBank(interBankDTO);
        APIResponse<IntermediaryBankDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Add new intermediary bank successfully!");
        responseData.setStatus(HttpStatus.CREATED.value());
        return ResponseEntity.ok(responseData);
    }

    @GetMapping
    public ResponseEntity<APIResponse<Page<IntermediaryBankDTO>>> findAllIntermediaryBank(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "10", required = false) Integer size,
            @RequestParam(defaultValue = "bankId", required = false) String sortBy) {

        log.info("Find All Intermediary banks");
        log.trace(String.format("search key: %s; page: %d; size: %d, sort by: %s ", searchValue, page, size, sortBy));

        Page<IntermediaryBankDTO> result = interBankService.findAllInterBank(searchValue, page, size, sortBy);
        APIResponse<Page<IntermediaryBankDTO>> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Find list intermediary bank successfully!");
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<APIResponse<IntermediaryBankDTO>> findInterBankById(@PathVariable("id") int id) {

        log.info("Find intermediary bank with id: {}", id);

        IntermediaryBankDTO lookupBankDTO = interBankService.findIntermediaryBankById(id);

        log.info("Init an API Response then return to client");
        APIResponse<IntermediaryBankDTO> responseData = new APIResponse<>();
        responseData.setData(lookupBankDTO);
        responseData.setMessage(String.format("Find intermediary bank with %d successfully!", id));
        responseData.setStatus(HttpStatus.OK.value());
        return ResponseEntity.ok(responseData);
    }

    /**
     *
     * @author anhdt
     * @param req
     * @param id
     * @return
     */


    @PutMapping("/{id}")
    public ResponseEntity<APIResponse<InterBankConfigDTO>> updateBankDetail(@RequestBody UpdateInterBankReq req,
                                                                            @PathVariable int id) {

        log.info(String.format("Start update intermediary bank with id %d", id));
        InterBankConfigDTO result = interBankService.updateInterBank(req, id);
        APIResponse<InterBankConfigDTO> responseData = new APIResponse<>();
        responseData.setData(result);
        responseData.setMessage("Update status successfully!!");
        responseData.setStatus(HttpStatus.OK.value());

        return ResponseEntity.ok(responseData);
    }


}

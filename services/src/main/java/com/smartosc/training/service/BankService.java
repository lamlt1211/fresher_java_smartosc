package com.smartosc.training.service;


import com.smartosc.training.dto.BankDirectConfigDTO;
import com.smartosc.training.dto.DirectBankDTO;
import com.smartosc.training.entity.Bank;
import com.smartosc.training.entity.BankDetail;
import com.smartosc.training.repositories.BankDetailRepository;

import com.smartosc.training.repositories.BankRepository;
import com.smartosc.training.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author duongnch
 * @since 20/04/2020
 */
@Slf4j
@Service
public class BankService {
    private static final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm a");
    private static final String BANK_TYPE = "DIRECT";
    private BankDetailRepository bankDetailRepository;

    private BankRepository bankRepository ;

    @Autowired
    public BankService(BankDetailRepository bankDetailRepository,BankRepository bankRepository) {
        this.bankDetailRepository = bankDetailRepository;
        this.bankRepository = bankRepository;
    }
    static String message;

    //Get List Bank Direct Deals
    public Page<BankDirectConfigDTO> getAllDirectBankConfiguration(Pageable pageable, String searchKey) {
        try {
            log.info("Retrieving Direct bank");
            List<Object[]> resultOj = bankDetailRepository.findAllBankDirectConfiguration(searchKey);
            List<BankDirectConfigDTO> directBankConfigDTOS = convert(resultOj);
            log.info("Convert into BankDirectConfigDto Success");
            Page<BankDirectConfigDTO> directbank = new PageImpl<BankDirectConfigDTO>(directBankConfigDTOS, pageable, directBankConfigDTOS.size());
            log.debug("Paging bank result: {}", directbank);
            return directbank;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new NullPointerException(e.getMessage());
        }
    }
    // Convert list object to list send bank configuration
    private List<BankDirectConfigDTO> convert(List<Object[]> objects) {
        List<BankDirectConfigDTO> result = new ArrayList<>();
        log.info("Convert object list into BankDirectConfig Success!!");
        for (Object[] obj : objects) {
            BankDirectConfigDTO bankSent = new BankDirectConfigDTO();
            bankSent.setCode(obj[0] != null ? obj[0].toString() : "");
            bankSent.setLegalName(obj[1] != null ? obj[1].toString() : "");
            bankSent.setStatus(Integer.parseInt(obj[2] != null ? obj[2].toString() : "0"));
            bankSent.setModifiedBy(obj[3] != null ? obj[3].toString() : "");
            bankSent.setModifiedDateTime(obj[4] != null ? formatter.format(obj[4]) : "");
            result.add(bankSent);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Page<DirectBankDTO> findAllDirectBank(String searchKey, Integer pageNo, Integer pageSize, String sortBy){

        log.info("Retrieving intermediary banks");
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Bank> result = bankRepository.finAllDirectBank(searchKey,BANK_TYPE,pageable);
        log.debug("Paging bank result: {}", result.getContent());
        return result.map(bank -> {

            BankDetail bankDetail = bankDetailRepository.findByBankIdAndType(bank.getBankId(), BANK_TYPE).get();
            return ConvertUtils.convertBankToDirectBankDTO(bank, bankDetail);


        });
    }
}
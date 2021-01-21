package com.smartosc.training.service;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.dto.IntermediaryBankDTO;
import com.smartosc.training.entity.Bank;
import com.smartosc.training.entity.BankDetail;
import com.smartosc.training.entity.Status;
import com.smartosc.training.exception.EntityNotFoundException;
import com.smartosc.training.exception.FieldDuplicateException;
import com.smartosc.training.exception.InternalServerException;
import com.smartosc.training.exception.NotFoundException;
import com.smartosc.training.mapper.BankDetailMapper;
import com.smartosc.training.repositories.BankRepository;
import com.smartosc.training.repositories.BankDetailRepository;
import com.smartosc.training.request.UpdateInterBankReq;
import com.smartosc.training.utils.ConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


/**
 * fres-parent
 *
 * @author andy98
 * @created_at 20/04/2020 - 15:35
 * @created_by andy98
 * @since 20/04/2020
 */
@Slf4j
@Service
public class IntermediaryBankService {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm a");

    String NOT_FOUND = "This Bank does not exist!";

    String UPDATE_SERVER_ERROR = "Database error. Can't update bank";


    private static final String BANK_TYPE_TRANSIT = "TRANSIT";

    private final BankDetailRepository bankDetailRepository;

    private final BankRepository bankRepository;

    @Autowired
    public IntermediaryBankService(BankDetailRepository bankDetailRepository, BankRepository bankRepository) {
        this.bankRepository = bankRepository;
        this.bankDetailRepository = bankDetailRepository;
    }


    public IntermediaryBankDTO addIntermediaryBank(IntermediaryBankDTO bankDTO) throws FieldDuplicateException {

        log.info("Add Intermediary bank");
        log.debug("Intermediary Bank data: {}", bankDTO);

        // Check if current bank is transit or not
        Optional<BankDetail> bankDetailOpt = bankDetailRepository.findByBankIdAndType(bankDTO.getBankId(), BANK_TYPE_TRANSIT);
        if (bankDetailOpt.isPresent()) {
            log.warn("Transit bank exists with id {}", bankDTO.getBankId());
            throw new FieldDuplicateException(String.format("Transit Bank with id %d exists", bankDTO.getBankId()));
        }

        // init a BankDetail object and assign value from DTO
        BankDetail newBankDetail = new BankDetail();
        newBankDetail.setBankId(bankDTO.getBankId());
        newBankDetail.setType(BANK_TYPE_TRANSIT);
//        bank.setValue(1); // default chuyen mach
        newBankDetail.setStatus(Status.ACTIVE.getValue()); // defaut status = 1

        // extract username from SecurityContext
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        newBankDetail.setCreatedBy(username);

        // set value for return object
        bankDTO.setStatus(newBankDetail.getStatus());

        bankDetailRepository.save(newBankDetail);

        return bankDTO;
    }

    @Transactional(readOnly = true)
    public Page<IntermediaryBankDTO> findAllInterBank(String searchKey, Integer pageNo, Integer pageSize, String sortBy) {

        log.info("Retrieving intermediary banks");

        // Find all intermediary bank and page them with above parameters
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(Sort.Direction.DESC, sortBy));
        Page<Bank> result = bankRepository.findAllIntermediaryBank(searchKey, BANK_TYPE_TRANSIT, pageable);
        log.debug("Paging bank result: {}", result.getContent());
        return result.map(bank -> {
            BankDetail transitBankDetail = bankDetailRepository.findByBankIdAndType(bank.getBankId(), BANK_TYPE_TRANSIT).get();
            return ConvertUtils.convertBankToInterBankDTO(bank, transitBankDetail);
        });

    }

    @Transactional(readOnly = true)
    public IntermediaryBankDTO findIntermediaryBankById(int id) throws EntityNotFoundException {

        log.info("Looking up for intermediary bank with id {}", id);
        Optional<Bank> lookupBank = bankRepository.findIntermediaryBankById(id, BANK_TYPE_TRANSIT);
        if (!lookupBank.isPresent()) {
            throw new EntityNotFoundException(String.format("Can not find bank with id: %d", id));
        }
        BankDetail transitBankDetail = bankDetailRepository.findByBankIdAndType(lookupBank.get().getBankId(), BANK_TYPE_TRANSIT).get();
        // mapping and return a DTO
        return ConvertUtils.convertBankToInterBankDTO(lookupBank.get(), transitBankDetail);
    }

    @Transactional
    public InterBankConfigDTO updateInterBank(UpdateInterBankReq updateInterBankReq, int id) {


        Optional<BankDetail> bankDetail = bankDetailRepository.findByIdAndType(id, updateInterBankReq.getTYPE());

        if (!bankDetail.isPresent()) {
            throw new NotFoundException(NOT_FOUND);
        }
        try {

            bankDetail.get().setModifiedBy(SecurityContextHolder.getContext().getAuthentication().getName());
            bankDetail.get().setStatus(updateInterBankReq.getStatus());
            bankDetailRepository.save(bankDetail.get());
            return BankDetailMapper.toInterBankConfigDTO(bankDetail.get());

        } catch (Exception e) {
            throw new InternalServerException(UPDATE_SERVER_ERROR);
        }
    }

}

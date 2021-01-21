package com.smartosc.training.service;

import com.smartosc.training.dto.ReceivedDirectlyBankDTO;
import com.smartosc.training.entity.Bank;
import com.smartosc.training.entity.BankDetail;
import com.smartosc.training.entity.Status;
import com.smartosc.training.repositories.BankDetailRepository;
import com.smartosc.training.repositories.BankRepository;
import com.smartosc.training.utils.exception.BadResourceException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * fres-parent
 *
 * @author thanhttt
 * @created_at 21/04/2020 - 09:30 AM
 */
@Slf4j
@Service
public class ReceivedDirectlyBankService {
    @Autowired
    private BankRepository bankRepository;

    @Autowired
    private BankDetailRepository directlyBankRepository;

    private static final String TYPE = "DIRECT";

    @Autowired
    private ModelMapper modelMapper;

    // Get all database from bank table with status = 1
    public List<ReceivedDirectlyBankDTO> getBankByStatus() {
        List<ReceivedDirectlyBankDTO> directlyBankDTOS = new ArrayList<>();
        List<Bank> bankList = bankRepository.getAllBanksByStatus();
        for (Bank bank : bankList) {
            ReceivedDirectlyBankDTO directlyBankDTO = modelMapper.map(bank, ReceivedDirectlyBankDTO.class);
            directlyBankDTOS.add(directlyBankDTO);
        }
        return directlyBankDTOS;
    }

    // Show bank detail by ID
    public ReceivedDirectlyBankDTO findBankDetailById(Integer id) throws ResourceNotFoundException {
        Optional<BankDetail> bankOptional = directlyBankRepository.findById(id);
        ReceivedDirectlyBankDTO receivedDirectlyBankDTO = new ReceivedDirectlyBankDTO();
        if (bankOptional.isPresent()) {
            BankDetail bankdetail = bankOptional.get();
            Bank bank = bankRepository.findById(bankdetail.getBankId()).get();
            receivedDirectlyBankDTO = modelMapper.map(bankdetail, ReceivedDirectlyBankDTO.class);
            receivedDirectlyBankDTO.setCode(bank.getCode());
            receivedDirectlyBankDTO.setLegalName(bank.getLegalName());
            receivedDirectlyBankDTO.setPrefixCard(bank.getPrefixCard());
            return receivedDirectlyBankDTO;
        } else {
            throw new ResourceNotFoundException("Failed: Not found bank detail with id = " + id);
        }
    }

    // Add type = "DIRECT" for bank and add data to bank_detail only when this bank' s status =1
    @Transactional
    public BankDetail createReceivedDirectlyBankCatalog(ReceivedDirectlyBankDTO receivedDirectlyBankDTO) throws BadResourceException {
        BankDetail bankDetail = new BankDetail();
        Integer id = receivedDirectlyBankDTO.getBankId();
        Optional<BankDetail> checkDirect = directlyBankRepository.findByBankIdAndType(id, TYPE);
        if (!checkDirect.isPresent()) {
            bankDetail.setBankId(receivedDirectlyBankDTO.getBankId());
            bankDetail.setCreatedBy(receivedDirectlyBankDTO.getCreatedBy());
            bankDetail.setCreatedDatetime(receivedDirectlyBankDTO.getCreatedDatetime());
            bankDetail.setType(TYPE);
            bankDetail.setStatus(Status.ACTIVE.getValue());
            return directlyBankRepository.save(bankDetail);
        } else {
            throw new BadResourceException("Failed create: duplicate DIRECT transaction type with bank : " + receivedDirectlyBankDTO.getCode());
        }
    }

    // Convert status in Bank Detail (for Received directly bank) from 1-Active or 0-Inactive, and conversely
    public void updateReceivedDirectlyBankCatalog(ReceivedDirectlyBankDTO receivedDirectlyBankDTO, Integer id) throws ResourceNotFoundException {
        BankDetail bank = directlyBankRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Failed: Bank not found for this id : " + id));
        bank.setBankId(bank.getBankId());
        bank.setModifiedBy(receivedDirectlyBankDTO.getModifiedBy());
        bank.setStatus(receivedDirectlyBankDTO.getStatus());
        directlyBankRepository.save(bank);
    }
}
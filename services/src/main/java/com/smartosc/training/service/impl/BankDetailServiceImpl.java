package com.smartosc.training.service.impl;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.dto.PageMetaData;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.entity.BankDetail;
import com.smartosc.training.exception.InternalServerException;
import com.smartosc.training.exception.NotFoundException;
import com.smartosc.training.mapper.BankDetailMapper;
import com.smartosc.training.repositories.BankDetailRepository;
import com.smartosc.training.request.UpdateInterBankReq;
import com.smartosc.training.service.BankDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */
@Component
public class BankDetailServiceImpl implements BankDetailService {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm a");

    @Autowired
    BankDetailRepository bankDetailRepository;

    /**
     * @param updateInterBankReq
     * @param id
     * @return
     * @author anhdt2
     */



    /**
     * @param searchKey
     * @return
     * @author anhdt2
     */
    //search inter bank
    @Override
    public List<InterBankConfigDTO> searchInterBankConfigByLegalNameOrCode(String searchKey) {

        List<Object[]> listResult = bankDetailRepository.findInterBankConfigByCodeOrByLegalName(searchKey);

        if (listResult.isEmpty()) {
            throw new NotFoundException(NOT_FOUND);
        }

        try {

            return convert(listResult);
        } catch (Exception e) {
            throw new InternalServerException(SERVER_ERROR);
        }
    }

    /*//Get list inter bank config
    @Override
    public List<InterBankConfigDTO> getAllInterBank() {

        List<Object[]> resultOj = bankDetailRepository.findAllInterBank();
        if (resultOj == null) {
            throw new NotFoundException("No record found");
        }

        return convert(resultOj);

    }*/

    //set response data for view

    /**
     * @param dtos
     * @param pageNo
     * @param pageSize
     * @return
     * @author anhtd2
     */
    @Override
    public APIResponse<List<InterBankConfigDTO>> getResponseData(Page<InterBankConfigDTO> dtos, int pageNo, int pageSize) {
        APIResponse<List<InterBankConfigDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("Fill all inter banks successful");
        responseData.setData(dtos.getContent());


        PageMetaData pageMetaData = new PageMetaData();
        pageMetaData.setNumber(dtos.getNumber());
        pageMetaData.setTotalElements(dtos.getTotalElements());
        pageMetaData.setTotalPages(dtos.getTotalPages());
        pageMetaData.setPage(pageNo);
        pageMetaData.setSize(pageSize);
        pageMetaData.setFirst(dtos.isFirst());
        pageMetaData.setLast(dtos.isLast());

        responseData.setPageMetadata(pageMetaData);
        return responseData;
    }

    /**
     * @param pageable
     * @return
     * @author anhtd2
     */
    //List all inter banks
    @Override
    public Page<InterBankConfigDTO> getListInterBanks(Pageable pageable) {

        try {
            List<Object[]> resultOj = bankDetailRepository.findAllInterBank();

            if (resultOj == null) {
                throw new NotFoundException("No record found");
            }

            List<InterBankConfigDTO> interBankConfigDTOList = convert(resultOj);

            return new PageImpl<InterBankConfigDTO>(interBankConfigDTOList, pageable, interBankConfigDTOList.size());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    // Convert list object to list intermediary bank configuration

    /**
     * @param objects
     * @return
     */
    private List<InterBankConfigDTO> convert(List<Object[]> objects) {
        List<InterBankConfigDTO> result = new ArrayList<>();
        for (Object[] obj : objects) {
            InterBankConfigDTO interBankConfigDTO = new InterBankConfigDTO();
            interBankConfigDTO.setId(Integer.parseInt(obj[0] != null ? obj[0].toString() : "0"));
            interBankConfigDTO.setCode(obj[1] != null ? obj[1].toString() : "");
            interBankConfigDTO.setLegalName(obj[2] != null ? obj[2].toString() : "");
            interBankConfigDTO.setStatus(Integer.parseInt(obj[3] != null ? obj[3].toString() : "0"));
            interBankConfigDTO.setModifiedBy(obj[4] != null ? obj[4].toString() : "");
            interBankConfigDTO.setModifiedDatetime(obj[5] != null ? formatter.format((TemporalAccessor) obj[5]) : "");
            result.add(interBankConfigDTO);
        }
        return result;
    }
}

package com.smartosc.training.service;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.request.UpdateInterBankReq;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */
@Service
public interface  BankDetailService {

    String NOT_FOUND = "This Bank does not exist!";

    String UPDATE_SERVER_ERROR = "Database error. Can't update post";

    String SERVER_ERROR = "Something was wrong!!!";
//
//    InterBankConfigDTO updateInterBank(UpdateInterBankReq updateInterBankReq, int id);

    List<InterBankConfigDTO> searchInterBankConfigByLegalNameOrCode(String searchKey);

//    List<InterBankConfigDTO> getAllInterBank();

    APIResponse<List<InterBankConfigDTO>> getResponseData(Page<InterBankConfigDTO> dtos, int pageNo, int pageSize);

    Page<InterBankConfigDTO> getListInterBanks(Pageable pageable);
}

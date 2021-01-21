package com.smartosc.training.mapper;

import com.smartosc.training.dto.InterBankConfigDTO;
import com.smartosc.training.entity.BankDetail;

import java.time.format.DateTimeFormatter;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 21/04/2020 - 3:26 PM
 * @created_by anhdt
 * @since 21/04/2020
 */
public class BankDetailMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm a");

    /**
     * @param bankDetail
     * @return
     */
    public static InterBankConfigDTO toInterBankConfigDTO(BankDetail bankDetail) {

        InterBankConfigDTO tmp = new InterBankConfigDTO();
        tmp.setId(bankDetail.getId());

        tmp.setModifiedBy(bankDetail.getModifiedBy());
        tmp.setModifiedDatetime(formatter.format(bankDetail.getModifiedDatetime()));
        tmp.setStatus(bankDetail.getStatus());

        return tmp;
    }
}

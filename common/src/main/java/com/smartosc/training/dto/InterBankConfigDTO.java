package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 23/04/2020 - 9:50 AM
 * @created_by anhdt
 * @since 23/04/2020
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//Intermediary Bank(NH trung gian)
public class InterBankConfigDTO {

    private int id;

    private String code;

    private String legalName;

    private Integer status;

    private String modifiedDatetime;

    private String modifiedBy;
}

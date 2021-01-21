package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 22/04/2020 - 3:21 PM
 * @created_by anhdt
 * @since 22/04/2020
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BankDTO {

    private int bankId;

    private String code;

    private String legalName;

    private String shortName;

    private Integer status;

    private String modifiedBy;

    private LocalDateTime modifiedDatetime;

    private LocalDateTime createdDatetime;

    private String createdBy;
}

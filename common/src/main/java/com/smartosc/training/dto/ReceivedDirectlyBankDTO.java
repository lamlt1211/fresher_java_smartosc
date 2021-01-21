package com.smartosc.training.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * fres-parent
 *
 * @author thanhttt
 * @created_at 21/04/2020 - 09:30 AM
 */
@Getter
@Setter
public class ReceivedDirectlyBankDTO {
    private Integer id;

    private Integer bankId;

    private String code;

    private String legalName;

    private String prefixCard;

    private String type;

    private Integer status;

    private String createdBy;

    private String modifiedBy;

    private LocalDateTime createdDatetime;

    private LocalDateTime modifiedDatetime;
}

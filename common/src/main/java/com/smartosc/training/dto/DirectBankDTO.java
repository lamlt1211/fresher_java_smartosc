package com.smartosc.training.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * fres-parent
 *
 * @author Huupd
 * @created_at 22/04/2020 - 6:10 PM
 * @created_by Huupd
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DirectBankDTO{

    private  int id;
    private int bankId;
    private String code;
    @JsonIgnore
    private String shortName;
    private String legalName;
    private String prefixCard;
    private String modifiedBy;
    private LocalDateTime modifiedDatetime;
    private int status;
}


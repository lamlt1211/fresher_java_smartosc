package com.smartosc.training.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * fres-parent
 *
 * @author andy98
 * @created_at 20/04/2020 - 10:47
 * @created_by andy98
 * @since 20/04/2020
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntermediaryBankDTO {
    private int id;
    private int bankId;
    private String code;
    @JsonIgnore
    private String shortName;
    private String legalName;
    private String modifiedBy;
    private LocalDateTime modifiedDatetime;
    private int status;
    private String createdBy;
}

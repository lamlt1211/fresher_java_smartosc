package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author anhdt
 * @project fres-parent
 * @created_at 20/04/2020 - 6:11 PM
 * @created_by anhdt
 * @since 20/04/2020
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * Response structure bank data
 */
public class BankDetailDTO {

    private Integer id;

    private Integer bankId;

    private Integer status;

    private String createdBy;

    private LocalDateTime createdDatetime;

    private String modifiedBy;

    private LocalDateTime modifiedDatetime;

}

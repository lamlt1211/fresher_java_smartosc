package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * fres-parent
 *
 * @author duongnch
 * @created_at 22/04/2020 - 10:36 AM
 * @created_by duongnch
 * @since 22/04/2020
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BankDirectConfigDTO{
    String code;
    String legalName;
    int status;
    String modifiedBy;
    String modifiedDateTime;
}

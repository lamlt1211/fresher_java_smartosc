package com.smartosc.training.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class UpdateInterBankReq {

    private Integer status;

    private final String TYPE = "TRANSIT";

    private Integer id;


}

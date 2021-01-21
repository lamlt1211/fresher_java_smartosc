package com.smartosc.training.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * Created by DucTD on 17/4/2020
 */

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AppCodeDTO {

    private Integer appCodeId;
    private String code;
    private String description;
    private Integer status;
    private String createdBy;
    private String modifiedBy;
    private LocalDateTime createdDatetime;
    private LocalDateTime modifiedDatetime;
}

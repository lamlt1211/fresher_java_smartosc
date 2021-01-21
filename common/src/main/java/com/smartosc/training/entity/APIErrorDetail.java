package com.smartosc.training.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class APIErrorDetail {

    private String object;
    private String field;
    private Object rejectedValue;
    private String message;

}

package com.smartosc.training.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Getter
@Setter
public class PromotionDTO extends AbstractDTO {

    private Integer promotionId;

    @NotNull
    @NotEmpty
    private String name;

    @Min(value = 0)
    @Max(value = 100)
    private double percent;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date startDate;

    @DateTimeFormat(pattern = "dd/MM/yyyy")
    private Date endDate;

    private List<Integer> productDTOIds;
    private List<ProductDTO> productDTOs;

}

package com.smartosc.training.dto;

import com.smartosc.training.dto.AbstractDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class OrderdetailDTO extends AbstractDTO {

	private int deltailId;
    private ProductDTO productDTO;
    private int quantity;
    private double price;
    private double amount;

    public OrderdetailDTO() {
        this.quantity = 0;
    }

    public double getAmount() {
        return this.productDTO.getPrice() * this.quantity;
    }
    
}

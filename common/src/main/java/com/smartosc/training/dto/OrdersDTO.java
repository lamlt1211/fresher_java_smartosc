package com.smartosc.training.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrdersDTO extends AbstractDTO{
	private Date orderDate;
    private int orderNum;
    private int ordersId;
    private double totalPrice;
    private UserDTO userDTO;
    private double amount;

    private List<OrderdetailDTO> orderDetailEntities;
}

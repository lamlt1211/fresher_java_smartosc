package com.smartosc.training.controller;

import com.smartosc.training.dto.*;
import com.smartosc.training.entity.CartInfo;
import com.smartosc.training.entity.CartLineInfo;
import com.smartosc.training.service.OrderService;
import com.smartosc.training.utils.CartSupportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String getOrderMember(ModelMap modelMap,
                                 @RequestParam("username") String username,
                                 @RequestParam(value = "status", defaultValue = "0",required = false) Integer status) {
        List<OrdersDTO> ordersDTOs = orderService.getAllOrder(username, status);
        modelMap.addAttribute("orders", ordersDTOs);
        return "order-member";
    }

    @GetMapping("/view")
    public String viewOrder(@RequestParam("orderid") Integer orderid, ModelMap model){
        OrdersDTO ordersDTO = orderService.viewOrder(orderid);
        model.addAttribute("order", ordersDTO);
        return "order-view";
    }

    @GetMapping("/cancel")
    @ResponseBody
    public boolean cancelOrderMember(@RequestParam("orderid") Integer orderid) {
        return orderService.cancelOrder(orderid);
    }

    @ResponseBody
    @GetMapping("/create")
    public Boolean getProductByCategory(
            @RequestParam("username") String username,
            @RequestParam("fullname") String fullname,
            @RequestParam("phone") String phone,
            @RequestParam("address") String address,
            @RequestParam("subtotal") double subtotal,
            @RequestParam("amount") double amount,
            @RequestParam("subquantity") double subquantity,
            HttpServletRequest request) {

        if (username != null) {
            OrdersDTO ordersDTO = new OrdersDTO();
            ordersDTO.setAmount(amount);
            ordersDTO.setTotalPrice(subtotal);

            UserDTO userDTO = new UserDTO();
            userDTO.setFullName(fullname);
            userDTO.setUserName(username);
            ordersDTO.setUserDTO(userDTO);

            CartInfo cartInfo = CartSupportUtils.getCartInSession(request);
            List<OrderdetailDTO> list = new ArrayList<>();
            List<CartLineInfo> lineInfos = cartInfo.getCartLines();
            lineInfos.forEach(l -> {
                OrderdetailDTO orderdetailDTO = new OrderdetailDTO();
                orderdetailDTO.setQuantity(l.getQuantity());
                orderdetailDTO.setPrice(l.getAmount());
                orderdetailDTO.setAmount(l.getAmount());

                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(l.getProductDTO().getProductId());
                productDTO.setProductName(l.getProductDTO().getProductName());
                productDTO.setImage(l.getProductDTO().getImage());
                productDTO.setDescription(l.getProductDTO().getDescription());
                productDTO.setPrice(l.getProductDTO().getPrice());

                orderdetailDTO.setProductDTO(productDTO);
                list.add(orderdetailDTO);
            });
            ordersDTO.setOrderDetailEntities(list);
            boolean check = orderService.createOrder(ordersDTO);
            if (check) {
                CartSupportUtils.removeCartInSession(request);
            }
            return true;
        } else {
            return false;
        }
    }
}

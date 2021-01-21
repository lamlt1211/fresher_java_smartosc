package com.smartosc.training.controller;

import com.smartosc.training.dto.OrdersDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    private String messageSucces = "get successful";

    @GetMapping
    public ResponseEntity<APIResponse<Page<OrdersDTO>>> getAllOrder(
            @RequestParam(defaultValue = "", required = false) String searchValue,
            @RequestParam(defaultValue = "0", required = false) Integer page,
            @RequestParam(defaultValue = "5", required = false) Integer size,
            @RequestParam(defaultValue = "orderId", required = false) String sortBy) {

        Page<OrdersDTO> orders = orderService.getAllOrder(searchValue, page, size, sortBy);
        APIResponse<Page<OrdersDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orders);
        responseData.setMessage(messageSucces);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("/member")
    public ResponseEntity<APIResponse<List<OrdersDTO>>> getAllOrderMember(
            @RequestParam("username") String member,
            @RequestParam("status") Integer status) {
        List<OrdersDTO> orders = orderService.getAllOrderMember(member, status);
        APIResponse<List<OrdersDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orders);
        responseData.setMessage("get successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("/view")
    public ResponseEntity<APIResponse<OrdersDTO>> viewOrder(
            @RequestParam("orderId") Integer orderid) {
        OrdersDTO orders = orderService.findOrderById(orderid);
        APIResponse<OrdersDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orders);
        responseData.setMessage("get by id successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }


    @GetMapping("/cancel")
    public ResponseEntity<APIResponse<Boolean>> cancelOrder(
            @RequestParam("orderId") Integer orderid) {
        APIResponse<Boolean> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orderService.cancelOrderStatus(orderid));
        responseData.setMessage("update successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<APIResponse<List<OrdersDTO>>> getAllOrder() {
        List<OrdersDTO> orders = orderService.getAllOrderExport();
        APIResponse<List<OrdersDTO>> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orders);
        responseData.setMessage(messageSucces);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Object> createOrder(@RequestBody OrdersDTO ordersDTO) {
        orderService.createOrder(ordersDTO);
        APIResponse<Boolean> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage(messageSucces);
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PostMapping("/import")
    public ResponseEntity<Object> importOrder(@RequestBody List<OrdersDTO> ordersDTOs) {
        orderService.createListOrder(ordersDTOs);
        APIResponse<Boolean> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("get successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<APIResponse<OrdersDTO>> changeStatus(@RequestBody OrdersDTO ordersDTO) {
        orderService.updateOrderStatus(ordersDTO);
        APIResponse<OrdersDTO> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setMessage("update successful");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }

    @GetMapping("/count")
    public ResponseEntity<APIResponse<Long>> getNumberOfOrderOnHold() {
        Long orderNum = orderService.countOrderOnHold();
        APIResponse<Long> responseData = new APIResponse<>();
        responseData.setStatus(HttpStatus.OK.value());
        responseData.setData(orderNum);
        responseData.setMessage("get number of order on hold successfull");
        return new ResponseEntity<>(responseData, HttpStatus.OK);
    }
}

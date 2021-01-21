package com.smartosc.training.service;

import com.smartosc.training.dto.OrderdetailDTO;
import com.smartosc.training.dto.OrdersDTO;
import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.OrderDetail;
import com.smartosc.training.entity.Orders;
import com.smartosc.training.entity.Product;
import com.smartosc.training.entity.Users;
import com.smartosc.training.repositories.OrderDetailRepository;
import com.smartosc.training.repositories.OrderRepository;
import com.smartosc.training.repositories.ProductRepository;
import com.smartosc.training.repositories.UserRepository;
import com.smartosc.training.utils.ConvertUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    public OrdersDTO findOrderById(Integer orderId){
        Optional<Orders> orders = orderRepository.findById(orderId);
        if(orders.isPresent()){
            return ConvertUtils.convertOrderToOrderDTO(orders.get());
        }
        return null;
    }

    public Page<OrdersDTO> getAllOrder(String searchValue, Integer pageNo, Integer sizeNo, String sortBy) {
        Pageable pageable = PageRequest.of(pageNo, sizeNo, Sort.by(sortBy));
        Page<Orders> pageResult = orderRepository.findByUsers_UserName(searchValue, pageable);
        return pageResult.map(ConvertUtils::convertOrderToOrderDTO);
    }

    public List<OrdersDTO> getAllOrderMember(String username, Integer status) {
        List<Orders> listResult = orderRepository.findByUsers_UserName(username, status);
        return listResult.stream().map(ConvertUtils::convertOrderToOrderDTO).collect(Collectors.toList());
    }

    public List<OrdersDTO> getAllOrderExport() {
        List<Orders> listOrders = orderRepository.findAll();
        List<OrdersDTO> listOrdersDTO = new ArrayList<>();
        listOrders.forEach(o -> {
            OrdersDTO ordersDTO = ConvertUtils.convertOrderToOrderDTO(o);
            listOrdersDTO.add(ordersDTO);
        });
        return listOrdersDTO;
    }

    public boolean createOrder(OrdersDTO ordersDTO) {
        if (ordersDTO != null) {
            Orders orders = new Orders();
            orders.setTotalPrice(ordersDTO.getTotalPrice());
            orders.setStatus(0);

            UserDTO userDTO = ordersDTO.getUserDTO();
            Users users = userRepository.findByUserName(userDTO.getUserName());
            orders.setUsers(users);
            orderRepository.save(orders);

            List<OrderdetailDTO> orderdetailDTOS = ordersDTO.getOrderDetailEntities();
            orderdetailDTOS.forEach(o -> {
                OrderDetail orderDetail = new OrderDetail();
                orderDetail.setQuantity(o.getQuantity());
                orderDetail.setStatus(0);
                orderDetail.setOrders(orders);

                Optional<Product> product = productRepository.findById(o.getProductDTO().getProductId());
                orderDetail.setProduct(product.get());
                orderDetail.setPrice(o.getPrice());

                orderDetailRepository.save(orderDetail);
            });
            return true;
        } else {
            return false;
        }
    }

    public void createListOrder(List<OrdersDTO> ordersDTOs) {
        ordersDTOs.forEach(o -> {
            try {
                Orders orders = new Orders();
                orders.setTotalPrice(o.getTotalPrice());
                orders.setStatus(o.getStatus());

                UserDTO userDTO = o.getUserDTO();
                Users users = userRepository.findByUserName(userDTO.getUserName());
                orders.setUsers(users);
                orderRepository.save(orders);

                List<OrderdetailDTO> orderdetailDTOS = o.getOrderDetailEntities();
                orderdetailDTOS.forEach(od -> {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setPrice(od.getPrice());
                    orderDetail.setQuantity(od.getQuantity());
                    orderDetail.setStatus(0);
                    Product product = productRepository.findBy1NameProduct(od.getProductDTO().getProductName());
                    orderDetail.setProduct(product);
                    orderDetail.setOrders(orders);

                    orderDetailRepository.save(orderDetail);
                });
            } catch (Exception e) {
                e.getMessage();
            }
        });
    }

    public boolean updateOrderStatus(OrdersDTO ordersDTO) {
        Optional<Orders> orders = orderRepository.findById(ordersDTO.getOrdersId());
        if (orders.isPresent()) {
            switch (ordersDTO.getStatus()) {
                case 0:
                    orders.get().setStatus(1);
                    break;
                case 1:
                    orders.get().setStatus(2);
                    break;
                default:
                    orders.get().setStatus(3);
                    break;
            }
            if (orders.get() != null) {
                orderRepository.save(orders.get());
                return true;
            }
        }
        return false;
    }

    public boolean cancelOrderStatus(Integer orderId) {
        Optional<Orders> orders = orderRepository.findById(orderId);
        if (orders.isPresent()) {
            if (orders.get().getStatus() == 0) {
                orders.get().setStatus(3);
                orders.get().setUpdatedAt(new Date());
            } else if (orders.get().getStatus() == 3) {
                orders.get().setStatus(0);
                orders.get().setUpdatedAt(new Date());
            }
            if (orders.get() != null) {
                orderRepository.save(orders.get());
                return true;
            }
        }
        return false;
    }

    public Long countOrderOnHold() {
        return orderRepository.countByStatus(0);
    }

}

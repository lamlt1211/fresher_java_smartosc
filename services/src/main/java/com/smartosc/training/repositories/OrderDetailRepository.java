package com.smartosc.training.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartosc.training.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
}

package com.smartosc.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartosc.training.entity.Orders;

import java.util.List;

public interface OrderRepository extends JpaRepository<Orders, Integer> {

	@Query("SELECT o FROM Orders o INNER JOIN o.users u"
			+ " WHERE u.userName LIKE %:searchValue%")
	Page<Orders> findByUsers_UserName(@Param("searchValue") String searchValue, Pageable pageable);

	@Query("SELECT o FROM Orders o INNER JOIN o.users u WHERE u.userName = :userName AND o.status = :status")
	List<Orders> findByUsers_UserName(@Param("userName") String userName, @Param("status") Integer status);

	Long countByStatus(Integer status);
	
}

package com.smartosc.training.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartosc.training.entity.Users;

import java.util.List;

public interface UserRepository extends JpaRepository<Users, Integer> {

    @Query("from Users u where u.userId = ?1")
    Users findByUserId(Integer idUsers);
    
    Users findByEmail(String email);
    
    Users findByUserName(String username);
    
    @Query("SELECT e FROM Users e INNER JOIN e.roles r"
    		+ " WHERE (e.userName LIKE %:searchValue% OR e.fullName LIKE %:searchValue% OR e.email LIKE %:searchValue%)"
    		+ " AND r.name LIKE :roleName")
	Page<Users> findBySearchValueAndRoles_Name(@Param("searchValue") String searchValue, @Param("roleName") String roleName, Pageable pageable);

	Long countByStatus(Integer status);

    List<Users> findByRoles_Name(String role_user);
}

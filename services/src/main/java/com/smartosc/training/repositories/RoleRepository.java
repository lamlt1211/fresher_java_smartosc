package com.smartosc.training.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartosc.training.entity.Role;

public interface RoleRepository extends JpaRepository<Role, Integer> {
	
	List<Role> findByUsers_UserName(String username);

	Role findByName(String string);
	
}

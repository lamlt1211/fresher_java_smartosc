package com.smartosc.training.service;

import java.util.ArrayList;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartosc.training.dto.RoleDTO;
import com.smartosc.training.entity.Role;
import com.smartosc.training.repositories.RoleRepository;

@Service
@Transactional
public class RoleService {

	@Autowired
	private RoleRepository roleRepository;

	@Autowired
	private ModelMapper modelMapper;

	public List<RoleDTO> getAllRoleByUserName(String username) {
		List<Role> roles = roleRepository.findByUsers_UserName(username);
		List<RoleDTO> roleDTOs = new ArrayList<>();
		roles.forEach(p -> {
			RoleDTO roleDTO = modelMapper.map(p, RoleDTO.class);
			roleDTOs.add(roleDTO);
		});
		return roleDTOs;
	}

}

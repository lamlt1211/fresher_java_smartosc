package com.smartosc.training.repositories;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartosc.training.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Integer> {
	
	PasswordResetToken findByToken(String verifyToken);
	
	void deleteByExpiryDateLessThan(Date now);
	
}

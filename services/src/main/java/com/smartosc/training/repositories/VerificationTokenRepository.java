package com.smartosc.training.repositories;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartosc.training.entity.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer>  {

	VerificationToken findByToken(String verifyToken);
	
	void deleteByExpiryDateLessThan(Date now);

}

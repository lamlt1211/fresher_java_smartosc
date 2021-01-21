package com.smartosc.training.task;

import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartosc.training.repositories.PasswordResetTokenRepository;
import com.smartosc.training.repositories.VerificationTokenRepository;

@Service
@Transactional
public class RemoveExpiryDateTokenTask {
	
	@Autowired
	private VerificationTokenRepository verifycationTokenRepository;
	
	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;
	
	@Scheduled(cron = "0 0 11 * * ?")
	public void removeExpiryDateToken() {
		Date now = Date.from(Instant.now());
		verifycationTokenRepository.deleteByExpiryDateLessThan(now);
		passwordResetTokenRepository.deleteByExpiryDateLessThan(now);
	}
	
}

package com.smartosc.training.event.listener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import com.smartosc.training.dto.UserDTO;
import com.smartosc.training.entity.APIResponse;
import com.smartosc.training.event.OnRegistrationCompleteEvent;
import com.smartosc.training.service.RestService;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

	private static final Logger LOGGER = LoggerFactory.getLogger(RegistrationListener.class);

	@Value("${api.url}")
	private String url;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${prefix.user}")
	private String prefixUrl;

	@Autowired
	private RestService restService;

	@Qualifier("messageSource")
	@Autowired
	private MessageSource messages;

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private SpringTemplateEngine templateEngine;

	@Autowired
	private Environment env;


	@Override
	public void onApplicationEvent(OnRegistrationCompleteEvent event) {
		this.confirmRegistration(event);
	}

	private void confirmRegistration(OnRegistrationCompleteEvent event) {
		String token = null;
		APIResponse<String> response = restService.execute(
				new StringBuilder(url).append("/").append(prefixUrl).append("/generate-verify-token").toString(),
				HttpMethod.POST,
				null,
				event.getUser(),
				new ParameterizedTypeReference<APIResponse<String>>() {},
				new HashMap<String, Object>());
		if (response.getStatus() == 200) {
			token = response.getData();
		}

		final MimeMessage email = constructEmailMessage(event, token);
		mailSender.send(email);

	}

	private final MimeMessage constructEmailMessage(final OnRegistrationCompleteEvent event,
			final String token) {
		final UserDTO user = event.getUser();
		final String recipientAddress = user.getEmail();
		final String subject = "Registration Confirm";
		String confirmationUrl = baseUrl + "registrationConfirm?token=" + token;
		final String message = messages.getMessage("message.regSucc", null,
				"If you can see it, that's mean you has received mail confirmation!", event.getLocale());
		final MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper helper = null;
		try {
			helper = new MimeMessageHelper(
					mimeMessage,
					MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
					StandardCharsets.UTF_8.name());
			
			Context context = new Context();
			context.setVariable("name", user.getFullName());
			context.setVariable("signature", baseUrl);
			context.setVariable("confirm_message", message);
			context.setVariable("confirm_link", confirmationUrl);
			String html = templateEngine.process("mail/mail-template", context);
			
			helper.setTo(recipientAddress);
			helper.setSubject(subject);
			helper.setText(html, true);
			helper.setFrom(env.getProperty("support.email"));
		} catch (MessagingException e) {
			LOGGER.error("Error when sent email occur: {}", e.getMessage());
		}
		return mimeMessage;
	}

}

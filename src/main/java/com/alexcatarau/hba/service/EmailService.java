package com.alexcatarau.hba.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private JavaMailSender emailSender;

    @Autowired
    public EmailService(JavaMailSender emailSender) {
        this.emailSender = emailSender;
    }


    public void sendEmailNewAccountLink(String firstName, String email, String confirmationToken) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(System.getenv("EMAIL_USERNAME"));
        message.setSubject("Holiday booking account");
        message.setText(createEmailMessage(firstName, confirmationToken));
        emailSender.send(message);
    }

    private String createEmailMessage(String firstName, String confirmationToken) {
        return "Hi " + firstName+",\n\n" +
                "Welcome to your new holiday booking account.\n\n" +
                "Please follow the following link to finish registration: \n" +
                System.getenv("CONFIRM_REGISTRATION_PATH") + confirmationToken;
    }
}

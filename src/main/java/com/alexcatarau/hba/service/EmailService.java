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


    public void sendEmailNewAccountLink(String firstName, String email, String registrationToken) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(System.getenv("EMAIL_USERNAME"));
        message.setSubject("Holiday booking account");
        message.setText(createEmailMessage(firstName, registrationToken));
        emailSender.send(message);
    }

    private String createEmailMessage(String firstName, String registrationToken) {
        return "Hi " + firstName+",\n\n" +
                "Welcome to your new holiday booking account.\n\n" +
                "Please click the following link to finish registration: \n" +
                System.getenv("CONFIRM_REGISTRATION_PATH") + registrationToken;
    }

    public void sendResetPasswordEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(System.getenv("EMAIL_USERNAME"));
        message.setSubject("HBA - reset password");
        message.setText(createResetPasswordEmailMessage(token));
        emailSender.send(message);
    }

    private String createResetPasswordEmailMessage(String token) {
        return "Hi, \n\n" +
                "Please click the following link to reset the password: \n" +
                System.getenv("RESET_PASSWORD_PATH") + token;

    }
}

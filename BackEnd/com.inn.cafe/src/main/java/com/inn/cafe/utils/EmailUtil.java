package com.inn.cafe.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class EmailUtil {
    @Autowired
    private JavaMailSender emailSender;

    public void SendSimpleMessage(String to , String subject , String text , List<String> list){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("projectteamemail99@gmail.com");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        if(list != null && list.size() > 0)
            message.setCc(getCcArray(list));
        emailSender.send(message);
    }

    public String[] getCcArray(List<String> cclist){
        String[] cc = new String[cclist.size()];
        for (int i = 0 ; i < cclist.size(); i++){
            cc[i] = cclist.get(i);
        }
        return cc;
    }
    public void  forgetMail(String to , String subject, String password) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("projectteamemail99@gmail.com");
        helper.setTo(to);
        helper.setSubject(subject);
        String htmlMSG = "<p><b>Your Login details for Cafe Management System</b></p><b>Email:</b>"+ to + "<br><b>Password: </b>" + password + "<br><a href=\"http://localhost:4200/\">Click here to login</a></p>";
        message.setContent(htmlMSG , "text/html");
        emailSender.send(message);
    }

    /**
     * Sends the 6-digit signup verification OTP as an HTML email. Used by the two-step
     * signup flow (POST /user/signup -> POST /user/verifySignupOtp) so a real, deliverable
     * mailbox is proven before an account is created.
     */
    public void sendOtpMail(String to, String otp, int validMinutes) throws MessagingException {
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom("projectteamemail99@gmail.com");
        helper.setTo(to);
        helper.setSubject("Verify your email - Cafe Management System");
        String htmlMSG = "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto\">"
                + "<h2 style=\"color:#e23744\">Verify your email address</h2>"
                + "<p>Use the One-Time Password (OTP) below to complete your registration on Cafe Management System:</p>"
                + "<p style=\"font-size:32px;font-weight:bold;letter-spacing:8px;color:#111;background:#f5f5f5;"
                + "padding:16px 24px;border-radius:8px;display:inline-block\">" + otp + "</p>"
                + "<p>This OTP is valid for <b>" + validMinutes + " minutes</b>. If you didn't request this, you can safely ignore this email.</p>"
                + "</div>";
        message.setContent(htmlMSG, "text/html");
        emailSender.send(message);
    }

}

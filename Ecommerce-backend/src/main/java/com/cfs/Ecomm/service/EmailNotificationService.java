package com.cfs.Ecomm.service;

import com.cfs.Ecomm.model.Orders;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    public EmailNotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Orders order) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(order.getUser().getEmail());
        message.setSubject(
                "Shoplane Order #" + order.getId() + " Confirmed"
        );

        message.setText(
                "Hi " + order.getUser().getName() + ",\n\n" +
                        "Your payment was successful and your order has been confirmed.\n\n" +
                        "Order ID: " + order.getId() + "\n" +
                        "Amount: ₹" + order.getTotalAmount() + "\n" +
                        "Status: " + order.getStatus() + "\n\n" +
                        "Thank you for shopping with Shoplane!"
        );

        mailSender.send(message);
    }
}
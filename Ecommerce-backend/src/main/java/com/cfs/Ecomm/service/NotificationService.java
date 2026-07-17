package com.cfs.Ecomm.service;

import com.cfs.Ecomm.dto.OrderPlacedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import static com.cfs.Ecomm.config.KafkaProducerConfig.ORDER_EVENTS_TOPIC;

@Service
public class NotificationService {

    private final JavaMailSender mailSender;

    public NotificationService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @KafkaListener(
            topics = ORDER_EVENTS_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:ecomm-notification-service}"
    )
    public void handleOrderPlacedEvent(OrderPlacedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(event.getUserEmail());
        message.setSubject("Shoplane Order #" + event.getOrderId() + " Confirmed");
        message.setText(
                "Hi " + event.getUserName() + ",\n\n" +
                        "Your payment was successful and your order has been confirmed.\n\n" +
                        "Order ID: " + event.getOrderId() + "\n" +
                        "Amount: \u20B9" + event.getTotalAmount() + "\n" +
                        "Status: " + event.getStatus() + "\n\n" +
                        "Thank you for shopping with Shoplane!"
        );

        mailSender.send(message);
    }
}

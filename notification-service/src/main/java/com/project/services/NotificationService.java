package com.project.services;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction_completed";

    private final SimpleMailMessage simpleMailMessage;
    private final JavaMailSender javaMailSender;

    public NotificationService(SimpleMailMessage simpleMailMessage, JavaMailSender javaMailSender) {
        this.simpleMailMessage = simpleMailMessage;
        this.javaMailSender = javaMailSender;
    }

    @KafkaListener(topics = {TRANSACTION_COMPLETED_TOPIC}, groupId = "group1")
    public void notify(String msg) throws ParseException {
        JSONObject obj = (JSONObject) new JSONParser().parse(msg);
        String transactionStatus = (String) obj.get("transactionStatus");
        String transactionId = (String) obj.get("transactionId");
        Long amount = (Long) obj.get("amount") / 100; //converting to rupees
        String senderEmail = (String) obj.get("senderEmail");
        String receiverEmail = (String) obj.get("receiverEmail");
        String senderName = (String) obj.get("senderName");
        String receiverName = (String) obj.get("receiverName");

        String senderMsg = getSenderMessage(transactionStatus, amount, transactionId, senderName, receiverName);
        String receiverMsg = getReceiverMessage(transactionStatus, amount, senderEmail, receiverName, senderName);

        if (!senderMsg.isEmpty()) {
            simpleMailMessage.setTo(senderEmail);
            simpleMailMessage.setSubject("E-Wallet Transaction Updates");
            simpleMailMessage.setFrom("notificationtest87@gmail.com");
            simpleMailMessage.setText(senderMsg);
            javaMailSender.send(simpleMailMessage);
        }

        if (!receiverMsg.isEmpty()) {
            simpleMailMessage.setTo(receiverEmail);
            simpleMailMessage.setSubject("E-Wallet Transaction Updates");
            simpleMailMessage.setFrom("notificationtest87@gmail.com");
            simpleMailMessage.setText(receiverMsg);
            javaMailSender.send(simpleMailMessage);
        }
    }

    private String getSenderMessage(String transactionStatus, Long amount, String transactionId, String senderName, String receiverName) {
        StringBuilder msg = new StringBuilder("Dear ");
        msg.append(senderName)
                .append(",\n\n");

        if (transactionStatus.equals("FAILED")) {
            msg.append("We're sorry, your transaction for Rupees ")
                    .append(amount.toString()) // Ensure amount is formatted as a currency
                    .append(" with transaction id: ")
                    .append(transactionId)
                    .append(" to ")
                    .append(receiverName)
                    .append(" has failed.");
        } else {
            msg.append("Your recent transaction has been completed successfully. Please find the details for the transaction below\n")
                    .append("Transaction Id: ")
                    .append(transactionId)
                    .append("\nAmount: Rupees ")
                    .append(amount)
                    .append("\nRecipient: ")
                    .append(receiverName);
        }
        return msg.toString();
    }

    private String getReceiverMessage(String transactionStatus, Long amount, String senderEmail, String receiverName, String senderName) {
        String msg;
        StringBuilder builder = new StringBuilder();

        if (transactionStatus.equals("SUCCESSFUL")) {
            builder.append("Dear ")
                    .append(receiverName)
                    .append(",\n\n")
                    .append("Your account has been credited with Rupees ")
                    .append(amount)
                    .append(" for the transaction done by user ")
                    .append(senderName)
                    .append(" (")
                    .append(senderEmail)
                    .append(").");
        }
        msg = builder.toString();
        return msg;
    }
}
package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.dto.CreateTransactionRequest;
import com.project.models.Transaction;
import com.project.models.TransactionStatus;
import com.project.repositories.TransactionRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Service
public class TransactionService {

    private static final String WALLET_UPDATED_TOPIC = "wallet_updated";
    private static final String TRANSACTION_CREATED_TOPIC = "transaction_created";
    private static final String TRANSACTION_COMPLETED_TOPIC = "transaction_completed";
    private static final String WALLET_UPDATE_SUCCESS_STATUS = "SUCCESS";
    private static final String WALLET_UPDATE_FAILED_STATUS = "FAILED";
    private final TransactionRepository transactionRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    public TransactionService(TransactionRepository transactionRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.transactionRepository = transactionRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public String transact(CreateTransactionRequest createTransactionRequest) throws JsonProcessingException {

        Transaction transaction = Transaction.builder()
                .senderId(createTransactionRequest.getSender())
                .receiverId(createTransactionRequest.getReceiver())
                .externalId(UUID.randomUUID().toString())
                .reason(createTransactionRequest.getReason())
                .amount(createTransactionRequest.getAmount())
                .build();

        transactionRepository.save(transaction);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("senderId", transaction.getSenderId());
        jsonObject.put("receiverId", transaction.getReceiverId());
        jsonObject.put("amount", transaction.getAmount());
        jsonObject.put("transactionId", transaction.getExternalId());

        kafkaTemplate.send(TRANSACTION_CREATED_TOPIC, objectMapper.writeValueAsString(jsonObject));

        return transaction.getExternalId();
    }

    @KafkaListener(topics = {WALLET_UPDATED_TOPIC}, groupId = "group1")
    public void updateTransaction(String message) throws ParseException, JsonProcessingException {

        JSONObject jsonObj = (JSONObject) new JSONParser().parse(message);
        String externalTransactionId = (String) jsonObj.get("transactionId");
        String receiverPhoneNumber = (String) jsonObj.get("receiverWalletId");
        String senderPhoneNumber = (String) jsonObj.get("senderWalletId");
        String walletUpdateStatus = (String) jsonObj.get("status");
        Long amount = (Long) jsonObj.get("amount");

        TransactionStatus transactionStatus;

        if (walletUpdateStatus.equals(WALLET_UPDATE_FAILED_STATUS)) {
            transactionStatus = TransactionStatus.FAILED;
            transactionRepository.update(externalTransactionId, transactionStatus);
        } else {
            transactionStatus = TransactionStatus.SUCCESSFUL;
            transactionRepository.update(externalTransactionId, transactionStatus);
        }

        JSONObject senderObj = this.restTemplate.getForObject("http://localhost:9000/user/phone/" + senderPhoneNumber, JSONObject.class);
        JSONObject receiverObj = this.restTemplate.getForObject("http://localhost:9000/user/phone/" + receiverPhoneNumber, JSONObject.class);

        String senderEmail = senderObj == null ? null : (String) senderObj.get("email");
        String receiverEmail = receiverObj == null ? null : (String) receiverObj.get("email");

        String senderName = senderObj == null ? null : (String) senderObj.get("name");
        String receiverName = receiverObj == null ? null : (String) receiverObj.get("name");

        jsonObj = new JSONObject();
        jsonObj.put("transactionId", externalTransactionId);
        jsonObj.put("transactionStatus", transactionStatus.toString());
        jsonObj.put("amount", amount);
        jsonObj.put("senderEmail", senderEmail);
        jsonObj.put("receiverEmail", receiverEmail);
        jsonObj.put("senderName", senderName);
        jsonObj.put("receiverName", receiverName);
        jsonObj.put("senderPhone", senderPhoneNumber);
        jsonObj.put("receiverPhone", receiverPhoneNumber);


        kafkaTemplate.send(TRANSACTION_COMPLETED_TOPIC, objectMapper.writeValueAsString(jsonObj));
    }
}

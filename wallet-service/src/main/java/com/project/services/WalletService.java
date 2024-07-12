package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.models.Wallet;
import com.project.repositories.WalletRepository;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class WalletService {

    private static final String USER_CREATED_TOPIC = "user_created";
    private static final String TRANSACTION_CREATED_TOPIC = "transaction_created";
    private static final String WALLET_UPDATED_TOPIC = "wallet_updated";

    private final WalletRepository walletRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${wallet.initial.balance}")
    Long initialBalance;

    public WalletService(WalletRepository walletRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.walletRepository = walletRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // User Onboarding Flow

    @KafkaListener(topics = {USER_CREATED_TOPIC}, groupId = "group1")
    public void createWallet(String message) throws ParseException {
        JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);

        String walletId = (String) jsonObject.get("phoneNumber");

        Wallet wallet = Wallet.builder()
                .walletId(walletId)
                .currency("INR")
                .balance(initialBalance)
                .build();

        walletRepository.save(wallet);

        // TODO: Publish an event of wallet_created

        // Terminal log when Kafka consumes message:
        /*
        Processing [GenericMessage [payload={"phone":"+91123456789","email":"example@xyz.com"},
        headers={kafka_offset=5, kafka_consumer=org.apache.kafka.clients.consumer.KafkaConsumer@2d4535f0,
        kafka_timestampType=CREATE_TIME, kafka_receivedPartitionId=0, kafka_receivedTopic=user_created,
        kafka_receivedTimestamp=1688281886670, kafka_groupId=group1}]]
        */
    }

    // User Transaction Flow

    @KafkaListener(topics = {TRANSACTION_CREATED_TOPIC}, groupId = "group1")
    public void updateWallets(String message) throws ParseException, JsonProcessingException {

        JSONObject jsonObject = (JSONObject) new JSONParser().parse(message);

        String receiverWalletId = (String) jsonObject.get("receiverId");
        String senderWalletId = (String) jsonObject.get("senderId");
        Long amount = (Long) jsonObject.get("amount");
        String transactionId = (String) jsonObject.get("transactionId");

        try {

            Wallet senderWallet = walletRepository.findByWalletId(senderWalletId);
            Wallet receiverWallet = walletRepository.findByWalletId(receiverWalletId);

            if (senderWallet == null || receiverWallet == null || senderWallet.getBalance() < amount) {

                jsonObject = this.init(receiverWalletId, senderWalletId, amount, transactionId, "FAILED");
                jsonObject.put("senderWalletBalance", senderWallet == null ? 0 : senderWallet.getBalance());

                kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(jsonObject));
            }
            walletRepository.updateWallet(senderWalletId, -amount);
            walletRepository.updateWallet(receiverWalletId, amount);

            // walletRepository.decrementWallet(senderWalletId, amount);
            // walletRepository.incrementWallet(receiverWalletId, amount);
            jsonObject = this.init(receiverWalletId, senderWalletId, amount, transactionId, "SUCCESS");
            kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(jsonObject));
        } catch (Exception e) {
            jsonObject = this.init(receiverWalletId, senderWalletId, amount, transactionId, "FAILED");
            jsonObject.put("errorMsg", e.getMessage());

            kafkaTemplate.send(WALLET_UPDATED_TOPIC, objectMapper.writeValueAsString(jsonObject));
        }
    }

    private JSONObject init(String receiverId, String senderId, Long amount, String transactionId, String status) {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("transactionId", transactionId);
        jsonObject.put("senderWalletId", senderId);
        jsonObject.put("receiverWalletId", receiverId);
        jsonObject.put("amount", amount);
        jsonObject.put("status", status);
        return jsonObject;
    }
}

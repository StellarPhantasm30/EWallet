package com.project.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.models.User;
import com.project.repositories.UserCacheRepository;
import com.project.repositories.UserRepository;
import org.json.simple.JSONObject;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final String USER_CREATE_TOPIC = "user_created";
    private final UserRepository userRepository;
    private final UserCacheRepository userCacheRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserService(UserRepository userRepository, UserCacheRepository userCacheRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.userRepository = userRepository;
        this.userCacheRepository = userCacheRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public void create(User user) throws JsonProcessingException {
        userRepository.save(user);

        JSONObject userObj = new JSONObject();
        userObj.put("phoneNumber", user.getPhoneNumber());
        userObj.put("email", user.getEmail());
        userObj.put("age", user.getAge());

        kafkaTemplate.send(USER_CREATE_TOPIC, this.objectMapper.writeValueAsString(userObj));
    }

    public User get(int userId) throws Exception {
        User user = userCacheRepository.get(userId);
        if (user != null) {
            return user;
        }

        user = userRepository.findById(userId).orElseThrow(() -> new Exception());
        userCacheRepository.set(user);
        return user;
    }

    public User getByPhoneNumber(String phoneNumber) throws Exception {

        //TODO: Implement Caching
        return userRepository.findByPhoneNumber(phoneNumber);
    }
}

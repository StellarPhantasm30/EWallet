package com.project.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.project.Utils;
import com.project.dto.CreateUserRequest;
import com.project.dto.GetUserResponse;
import com.project.models.User;
import com.project.services.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/user/{userId}")
    public GetUserResponse getUser(@PathVariable("userId") int userId) throws Exception {
        User user = userService.get(userId);
        return Utils.convertToGetUserResponse(user);
    }

    @PostMapping("/user")
    public void createUser(@RequestBody @Valid CreateUserRequest createUserRequest) throws JsonProcessingException {
        userService.create(Utils.convertUserCreateRequest(createUserRequest));
    }

    // For Notification Service
    @GetMapping("/user/phone/{phone}")
    public GetUserResponse getUserByPhoneNumber(@PathVariable("phone") String phoneNumber) throws Exception {
        User user = userService.getByPhoneNumber(phoneNumber);
        return Utils.convertToGetUserResponse(user);
    }

}
package com.project;

import com.project.dto.CreateUserRequest;
import com.project.dto.GetUserResponse;
import com.project.models.User;

public class Utils {

    public static User convertUserCreateRequest(CreateUserRequest createUserRequest) {
        return User.builder()
                .name(createUserRequest.getName())
                .phoneNumber(createUserRequest.getPhoneNumber())
                .email(createUserRequest.getEmail())
                .age(createUserRequest.getAge())
                .build();
    }

    public static GetUserResponse convertToGetUserResponse(User user) {
        return GetUserResponse.builder()
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .email(user.getEmail())
                .age(user.getAge())
                .createdOn(user.getCreatedOn())
                .updatedOn(user.getUpdatedOn())
                .build();
    }
}

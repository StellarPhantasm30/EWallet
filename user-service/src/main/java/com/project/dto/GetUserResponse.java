package com.project.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetUserResponse {

    private String name;

    private String phoneNumber;

    private String email;

    private int age;

    private Date createdOn;

    private Date updatedOn;
}

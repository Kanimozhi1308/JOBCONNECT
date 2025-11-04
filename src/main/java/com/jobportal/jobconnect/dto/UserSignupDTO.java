package com.jobportal.jobconnect.dto;

import com.jobportal.jobconnect.model.Role;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDTO {

    private String name;
    private String email;
    private String password;
    private String phone;
    private Role role;

}

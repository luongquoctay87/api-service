package com.example.sample.api.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class UserForm {
    Long id;
    @NotNull(message = "Email can not null")
    @Email
    String email;
    @NotNull(message = "Username can not null")
    @NotBlank(message = "Username can not blank")
    @Length(min = 3, max = 255, message = "User must have 3~255 characters")
    @Pattern(regexp = "^[a-zA-Z0-9_]*$", message = "Username should have alphabets, numbers, underscores")
    String username;
    String password;
}

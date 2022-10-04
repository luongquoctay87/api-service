package com.example.sample.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
@Builder
public class TokenResponse implements Serializable {
    private String username;
    private String roles;
    private String access_token;
    private String refresh_token;
}

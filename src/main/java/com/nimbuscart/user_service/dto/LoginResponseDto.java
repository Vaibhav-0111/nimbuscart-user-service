package com.nimbuscart.user_service.dto;
import lombok.Getter;
@Getter
public class LoginResponseDto {
    private final String token;
    public LoginResponseDto(String token) {
        this.token = token;
    }
}
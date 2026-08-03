package com.nimbuscart.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import java.util.TimeZone;

@SpringBootApplication
@EnableMethodSecurity
public class UserServiceApplication {

	static {
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

	@Bean
	public CommandLineRunner initUsers(UserService userService) {
		return args -> {
			if (userService.findByUsername("admin").isEmpty()) {
				userService.saveUser(User.builder().username("admin").password("admin123").role("ADMIN").build());
			}
			if (userService.findByUsername("dealer").isEmpty()) {
				userService.saveUser(User.builder().username("dealer").password("dealer123").role("DEALER").build());
			}
			if (userService.findByUsername("employee").isEmpty()) {
				userService.saveUser(User.builder().username("employee").password("emp123").role("EMPLOYEE").build());
			}
		};
	}
}

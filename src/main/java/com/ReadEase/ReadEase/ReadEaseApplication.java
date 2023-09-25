package com.ReadEase.ReadEase;

import com.ReadEase.ReadEase.Model.Role;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;

@SpringBootApplication
public class ReadEaseApplication {


	public static void main(String[] args) {
		SpringApplication.run(ReadEaseApplication.class, args);
	}


//	@Bean
//	CommandLineRunner initDatabase (DocumentRepo docRepo){
//		return new CommandLineRunner() {
//			@Override
//			public void run(String... args) throws Exception {
//				for (int i = 1; i <= 10; i++) {
//
//				}
//
//			}
//		};
//	}

}

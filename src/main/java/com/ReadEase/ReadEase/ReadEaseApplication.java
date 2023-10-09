package com.ReadEase.ReadEase;

// import com.ReadEase.ReadEase.Model.Role;
// import com.ReadEase.ReadEase.Model.Token;
// import com.ReadEase.ReadEase.Model.TokenType;
// import com.ReadEase.ReadEase.Model.User;
// import com.ReadEase.ReadEase.Repo.RoleRepo;
// import com.ReadEase.ReadEase.Repo.TokenRepo;
// import com.ReadEase.ReadEase.Repo.UserRepo;
// import com.ReadEase.ReadEase.Service.DriveService;
// import com.google.api.client.auth.oauth2.TokenResponse;
// import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.context.annotation.Bean;
// import org.springframework.security.crypto.password.PasswordEncoder;

// import java.util.Date;


@SpringBootApplication
public class ReadEaseApplication {


	public static void main(String[] args) {
		SpringApplication.run(ReadEaseApplication.class, args);
	}


//	@Bean
//	CommandLineRunner initDatabase (TokenRepo tokenRepo, DriveService driveService, UserRepo userRepo, RoleRepo roleRepo, PasswordEncoder passwordEncoder){
//		return new CommandLineRunner() {
//			@Override
//			public void run(String... args) throws Exception {
//
//				Role role = new Role(2, "admin1");
//				User user = userRepo.findUserByEmail("nlnktpm@gmail.com").orElseThrow();
//				tokenRepo.deleteTokenByUserID(user.getID());
//				TokenResponse token = driveService.getToken();
//
//				tokenRepo.save(Token.builder()
//								.token(token.getAccessToken())
//								.expriedAt(new Date((new Date()).getTime() + token.getExpiresInSeconds() *1000))
//								.type(TokenType.GG_DRIVE)
//								.user(user)
//						.build());
//
//			}
//		};
//	}

}

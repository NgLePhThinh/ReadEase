package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Config.GeneratePassword;
import com.ReadEase.ReadEase.Controller.User.Request.LogoutRequest;
import com.ReadEase.ReadEase.Controller.User.Request.SignUpRequest;
import com.ReadEase.ReadEase.Controller.User.Response.LoginResponse;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.RoleRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private  final PasswordEncoder passwordEncoder;

    @GetMapping("/")
    public  ResponseEntity<?> getAllUser(){
        GeneratePassword pwdGenerator = new GeneratePassword();

        return new ResponseEntity<> (pwdGenerator.generateStrongPassword(8), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest res){
        if(userRepo.findUserByEmail(res.getEmail()) != null)
            return new ResponseEntity<>("Email already exists!!!", HttpStatus.BAD_REQUEST);

        User newUser =  roleRepo.findById(1).map(role ->{
            User user = new User(res.getEmail(), passwordEncoder.encode(res.getPassword()), role);
            return userRepo.save(user);
        }).orElseThrow();

        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    @PostMapping("/login/step1")
    public ResponseEntity <?> loginStep1(@RequestBody String email){
        if(userRepo.findUserByEmail(email) == null)
            return new ResponseEntity<>("invalid credentials", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>("Email is valid",HttpStatus.OK);
    }
    @PostMapping("/login/step2")
    public ResponseEntity <?> loginStep2(@RequestBody User user){
        var userLogin = userRepo.findUserByEmail(user.getEmail())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid credentials")
                );

        if(!passwordEncoder.matches(user.getPassword(), userLogin.getPassword()))
            return new ResponseEntity<>("Password is not valid",HttpStatus.OK);

        LoginResponse res = LoginResponse.builder()
                .userID(userLogin.getID())
                .email(userLogin.getEmail())
                .avatar(userLogin.getAvatar())
                .token("Phát triển sau")
                .currentDocumentReading(userLogin.getLastReadingDocument())
                .collections(userLogin.getCollections())
                .documents(userLogin.getDocumentsSortedByCreatedAtDesc())
                .build();

        return new ResponseEntity<>(res,HttpStatus.OK);
    }
    @PutMapping("/logout")
    public ResponseEntity <?> logout(@RequestBody LogoutRequest res) throws ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        Date loginTime = sdf.parse(res.getLoginTime());
        long totalTime = Duration.between(loginTime.toInstant(), new Date().toInstant()).getSeconds();

        userRepo.updateLastAccessByEmail(res.getEmail(),new Date(), totalTime);
        return new ResponseEntity<>("Log out successfully",HttpStatus.OK);
    }




}

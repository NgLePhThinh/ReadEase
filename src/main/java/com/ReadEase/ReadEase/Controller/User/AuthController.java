package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Config.GeneratePassword;
import com.ReadEase.ReadEase.Config.JwtService;
import com.ReadEase.ReadEase.Controller.User.Request.LogoutRequest;
import com.ReadEase.ReadEase.Controller.User.Request.SignUpRequest;
import com.ReadEase.ReadEase.Controller.User.Response.AuthResponse;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.RoleRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
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
    private final EmailService emailService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private  final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @GetMapping("/")
    public  ResponseEntity<?> getAllUser(){
        GeneratePassword pwdGenerator = new GeneratePassword();
        return new ResponseEntity<> (pwdGenerator.generateStrongPassword(8), HttpStatus.OK);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest req){
        if(userRepo.countUserByEmail(req.getEmail()) == 1)
            return new ResponseEntity<>("Email already exists!!!", HttpStatus.BAD_REQUEST);

        User newUser =  roleRepo.findById(1).map(role ->{
            User user = new User(req.getEmail(), passwordEncoder.encode(req.getPassword()), role);
            return userRepo.save(user);
        }).orElseThrow();
        System.out.println(newUser);

        AuthResponse res = AuthResponse.builder()
                .userID(newUser.getID())
                .email(newUser.getEmail())
                .avatar(newUser.getAvatar())
                .token("")
                .build();

        return new ResponseEntity<>(res, HttpStatus.CREATED);
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

        String jwtToken = jwtService.generateToken(userLogin);
        String refreshToken = jwtService.generateRefreshToken(userLogin);

        AuthResponse res = AuthResponse.builder()
                .userID(userLogin.getID())
                .email(userLogin.getEmail())
                .avatar(userLogin.getAvatar())
                .token(jwtToken)
                .refreshToken(refreshToken)
                .currentDocumentReading(userLogin.getLastReadingDocument())
                .collections(userLogin.getCollections())
                .documents(userLogin.getDocumentsSortedByLastReadDesc())
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

    @PostMapping("/forgot-password-step1")
    public ResponseEntity<?> forgotPasswordStep1(@RequestBody User req){
        User _user = userRepo.findUserByEmail(req.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email invalid!!!!")
        );
        GeneratePassword generatePassword = new GeneratePassword();
        String resetPasswordToken = jwtService.generateResetPasswordToken(_user);
        emailService.sendHTMLEmail(_user.getEmail(), resetPasswordToken);
        return  new ResponseEntity<>(generatePassword.generateStrongPassword(8), HttpStatus.OK);
    }

    @GetMapping("/forgot-password-step2")
    public ResponseEntity<?> forgotPasswordStep2(@RequestParam("token") String resetPasswordToken ){
        if(resetPasswordToken == null || jwtService.isTokenExpried(resetPasswordToken))
            new ResponseEntity<>("Token invalid!!!",HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(HttpStatus.OK);
    }
    @PostMapping("/forgot-password-step3")
    public ResponseEntity<?> forgotPasswordStep2(@RequestParam("token") String resetPasswordToken,@RequestBody  User req ){
        if(resetPasswordToken == null || req.getPassword() == null || jwtService.isTokenExpried(resetPasswordToken))
            return new ResponseEntity<>("Request invalid!!!",HttpStatus.BAD_REQUEST);
        String email = jwtService.extractUserEmail(resetPasswordToken);

        userRepo.updatePasswordByEmail(email,passwordEncoder.encode(req.getPassword()));

        User user = userRepo.findUserByEmail(email).orElseThrow();

        return new ResponseEntity<>(passwordEncoder.matches(req.getPassword(), user.getPassword()),HttpStatus.OK);
    }



}

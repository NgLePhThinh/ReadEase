package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Config.GeneratePassword;
import com.ReadEase.ReadEase.Config.JwtService;
import com.ReadEase.ReadEase.Controller.User.Request.SignUpRequest;
import com.ReadEase.ReadEase.Controller.User.Response.AuthResponse;
import com.ReadEase.ReadEase.Model.Token;
import com.ReadEase.ReadEase.Model.TokenType;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.RoleRepo;
import com.ReadEase.ReadEase.Repo.TokenRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.EmailService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.Date;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    @Value("${application.cross-origin}")
    private String domain;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private int maxAgeCookie;
    private final EmailService emailService;
    private final UserRepo userRepo;
    private final RoleRepo roleRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenRepo tokenRepo;


    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest req) {
        if (userRepo.countUserByEmail(req.getEmail()) == 1)
            return new ResponseEntity<>("Email already exists!!!", HttpStatus.BAD_REQUEST);

        User newUser = roleRepo.findById(1).map(role -> {
            User user = new User(req.getEmail(), passwordEncoder.encode(req.getPassword()), role);
            return userRepo.save(user);
        }).orElseThrow();

        AuthResponse res = AuthResponse.builder()
                .userID(newUser.getID())
                .email(newUser.getEmail())
                .avatar(newUser.getAvatar())
                .build();

        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<?> loginWithGoogle(@Nonnull HttpServletResponse response,@RequestBody User req){
        User user = userRepo.findUserByEmail(req.getEmail()).orElse(null);
        boolean isFirstLogin = false;
        if(user == null){
            GeneratePassword generatePassword = new GeneratePassword();
            String pwd = generatePassword.generateStrongPassword(8);
            user = roleRepo.findById(1).map(role -> {
                User _user = new User(req.getEmail(), passwordEncoder.encode(pwd), role);
                _user.setAvatar(req.getAvatar());
                return userRepo.save(_user);
            }).orElseThrow();

            isFirstLogin = true;
        }
        String jwtToken = jwtService.generateToken(user);

        //Update token into database
        if ( checkExistAndSaveToken(user, jwtToken, TokenType.ACCESS))
            return new ResponseEntity<>("Can not sign in now!!!",HttpStatus.FORBIDDEN);

        user.setLastAccess(new Date());
        userRepo.save(user);

        Cookie cookie = new Cookie("refreshToken", jwtService.generateRefreshToken(user));
        cookie.setMaxAge(maxAgeCookie/1000);
        cookie.setHttpOnly(true);
//        cookie.setDomain("http://localhost:3000");
        response.addCookie(cookie);


        AuthResponse res = AuthResponse.builder()
                .userID(user.getID())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .token(jwtToken)
                .build();
        if(!isFirstLogin){
            res.setCurrentDocumentReading(user.getLastReadingDocument());
            res.setCollections(user.getCollections());
            res.setDocuments(user.getDocumentCustom(0,10));
        }

        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    @PostMapping("/login/step1")
    public ResponseEntity<?> loginStep1(@RequestBody User req) {
        if (userRepo.countUserByEmail(req.getEmail()) == 0)
            return new ResponseEntity<>("Email is not valid!!!", HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>("Email is valid", HttpStatus.OK);
    }
    @PostMapping("/login/step2")
    public ResponseEntity<?> loginStep2(@Nonnull HttpServletResponse response,@RequestBody User req) {
        if (userRepo.countUserByEmail(req.getEmail()) == 0)
            return new ResponseEntity<>("Email is not valid!!!", HttpStatus.BAD_REQUEST);

        User user = userRepo.findUserByEmail(req.getEmail()).orElseThrow();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword()))
            return new ResponseEntity<>("Password is not valid", HttpStatus.BAD_REQUEST);

        String jwtToken = jwtService.generateToken(user);

        //Update token into database
        if ( checkExistAndSaveToken(user, jwtToken, TokenType.ACCESS))
            return new ResponseEntity<>("Can not sign in now!!!",HttpStatus.FORBIDDEN);

        user.setLastAccess(new Date());
        userRepo.save(user);

        Cookie cookie = new Cookie("refreshToken", jwtService.generateRefreshToken(user));
        cookie.setMaxAge(maxAgeCookie/1000);
        cookie.setHttpOnly(true);
//        cookie.setDomain("http://localhost:3000");
        response.addCookie(cookie);


        AuthResponse res = AuthResponse.builder()
                .userID(user.getID())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .token(jwtToken)
                .currentDocumentReading(user.getLastReadingDocument())
                .collections(user.getCollections())
                .documents(user.getDocumentCustom(0,10))
                .build();

        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PutMapping("/logout")
    public ResponseEntity<?> logout(@Nonnull HttpServletResponse response,@RequestBody User req) {

        User user = userRepo.findUserByEmail(req.getEmail()).orElse(null);

        if(user == null) return  new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);

        long totalTime = Duration.between(user.getLastAccess().toInstant(), new Date().toInstant()).getSeconds();

        user.setLastAccess(new Date());
        user.setTotalAccessTime(user.getTotalAccessTime() + totalTime);
        userRepo.save(user);
        tokenRepo.deleteTokenByUserID(user.getID());

        return new ResponseEntity<>("Log out successfully", HttpStatus.OK);
    }

    @PostMapping("/forgot-password/step1")
    public ResponseEntity<?> forgotPasswordStep1(@RequestBody User req) {
        User user = userRepo.findUserByEmail(req.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email invalid!!!!")
        );
        GeneratePassword generatePassword = new GeneratePassword();
        String resetPasswordToken = jwtService.generateResetPasswordToken(user);

        saveUserToken(user, resetPasswordToken, TokenType.RESET_PASSWORD);

        emailService.sendHTMLEmail(user.getEmail(), resetPasswordToken);
        return new ResponseEntity<>(generatePassword.generateStrongPassword(8), HttpStatus.OK);
    }

    @GetMapping("/forgot-password/step2")
    public ResponseEntity<?> forgotPasswordStep2(@RequestParam("token") String resetPasswordToken) {
        var token = tokenRepo.findTokenByToken(resetPasswordToken)
                .orElse(null);

        if (resetPasswordToken == null || jwtService.isTokenExpried(resetPasswordToken) || token == null)
            return new ResponseEntity<>("Token invalid!!!", HttpStatus.BAD_REQUEST);

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/forgot-password/step3")
    public ResponseEntity<?> forgotPasswordStep2(@RequestParam("token") String resetPasswordToken, @RequestBody User req) {
        var token = tokenRepo.findTokenByToken(resetPasswordToken)
                .orElse(null);

        if (resetPasswordToken == null || req.getPassword() == null || token == null
                || jwtService.isTokenExpried(resetPasswordToken))
            return new ResponseEntity<>("Request invalid!!!", HttpStatus.BAD_REQUEST);

        String email = jwtService.extractUserEmail(resetPasswordToken);

        userRepo.updatePasswordByEmail(email, passwordEncoder.encode(req.getPassword()));

        tokenRepo.delete(token);

        return new ResponseEntity<>(HttpStatus.OK);
    }


    private void saveUserToken(User user, String jwt, TokenType tokenType) {
        tokenRepo.save(Token.builder()
                .user(user)
                .expriedAt(new Date(new Date().getTime() + getExpiration(tokenType)))
                .token(jwt)
                .type(tokenType)
                .build());
    }

    private boolean checkExistAndSaveToken(User user, String jwt, TokenType tokenType) {

        Token token = tokenRepo.findTokenByUserIDAndType(user.getID(), tokenType.toString())
                .orElse(null);
        if (token != null)
            return true;
        saveUserToken(user, jwt, tokenType);
        return false;
    }

    private long getExpiration(TokenType tokenType) {
        JwtService jwtService = new JwtService();
        return tokenType == TokenType.ACCESS ? jwtService.getAccessTokenExpiration()
                : tokenType == TokenType.REFRESH ? jwtService.getRefreshTokenExpiration()
                : jwtService.getResetPasswordExpiration();
    }


}

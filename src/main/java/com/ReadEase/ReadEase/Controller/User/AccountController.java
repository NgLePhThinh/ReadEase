package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Controller.User.Request.ChangePasswordRequest;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;

import java.util.HashMap;



@RestController
@RequestMapping("/api/user/account")
@AllArgsConstructor
public class AccountController {
    private final UserRepo userRepo;
    private  final PasswordEncoder passwordEncoder;

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req){
        var user = userRepo.findById(req.getUserID())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"invalid credentials")
                );
        if(!passwordEncoder.matches(req.getOldPwd(),user.getPassword()))
            return new ResponseEntity<>("Password is not valid",HttpStatus.OK);
        user.setPassword(passwordEncoder.encode(req.getNewPwd()));
        userRepo.save(user);
        return new ResponseEntity<>("Change Password Successfully!!", HttpStatus.OK);
    }
    @GetMapping("")
    public ResponseEntity<?> getAccountInfor(@RequestBody  String req){

        Gson gson = new Gson();
        User user = gson.fromJson(req, User.class);
        User resUser = userRepo.findById(user.getID())
                .orElseThrow();

        return new ResponseEntity<>(new HashMap<String, String> (){
            {
                put("email", resUser.getEmail());
                put("avatar", resUser.getAvatar());
                put("totalAccessTime", String.valueOf(resUser.getTotalAccessTime()));
            }
        }, HttpStatus.OK);
    }
    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@RequestBody String req){
        Gson gson = new Gson();
        User user = gson.fromJson(req, User.class);
        User resUser = userRepo.findById(user.getID()).orElseThrow();
        resUser.setAvatar(user.getAvatar());
        userRepo.save(resUser);
        return new ResponseEntity<>("Avatar image update successfully!!!",HttpStatus.OK);
    }

}

package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Controller.User.Request.ChangePasswordRequest;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.google.gson.Gson;

import java.util.HashMap;



@RestController
@RequestMapping("/api/user/account")
@RequiredArgsConstructor
public class AccountController {
    private final UserRepo userRepo;
    private  final PasswordEncoder passwordEncoder;

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest req){
        User resUser = userRepo.findById(req.getUserID()).orElse(null);
        if ( resUser == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        if(!passwordEncoder.matches(req.getOldPwd(), resUser.getPassword()))
            return new ResponseEntity<>("Password is not valid",HttpStatus.BAD_REQUEST);
        resUser.setPassword(passwordEncoder.encode(req.getNewPwd()));
        userRepo.save(resUser);
        return new ResponseEntity<>("Change Password Successfully!!", HttpStatus.OK);
    }
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccountInfor(@PathVariable("id") String userID){

        User resUser = userRepo.findById(userID).orElse(null);
        if ( resUser == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(new HashMap<String, Object> (){
            {
                put("email", resUser.getEmail());
                put("avatar", resUser.getAvatar());
                put("totalAccessTime", resUser.getTotalAccessTime());
                put("totalCapacity", resUser.getTotalCapacity());
            }
        }, HttpStatus.OK);
    }
    @PutMapping("/update-information")
    public ResponseEntity<?> updateInformation(@RequestBody User req){
        User resUser = userRepo.findUserByEmail(req.getEmail()).orElse(null);
        if ( resUser == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        resUser.setAvatar(req.getAvatar());
        resUser.setTargetLanguage(req.getTargetLanguage());

        userRepo.save(resUser);

        return new ResponseEntity<>("Information update successfully!!!",HttpStatus.OK);
    }

}

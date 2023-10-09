package com.ReadEase.ReadEase.Controller.User;

import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user/get-all-user")
@RequiredArgsConstructor
public class GetAllUser {
    private final UserRepo userRepo;
    @GetMapping("")
    public ResponseEntity<?> getAllUsers(){
        return new ResponseEntity<>(userRepo.findAll(), HttpStatus.OK);
    }
}

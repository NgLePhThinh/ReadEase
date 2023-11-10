package com.ReadEase.ReadEase.Controller;

import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test/")
@RequiredArgsConstructor
public class test {
    private final UserRepo userRepo;

    @GetMapping()
    public ResponseEntity<?> test(@RequestBody User user){
        User _user = userRepo.findUserByEmail(user.getEmail()).orElse(null);
        return new ResponseEntity<>(_user.getCollections(), HttpStatus.OK);
    }
}

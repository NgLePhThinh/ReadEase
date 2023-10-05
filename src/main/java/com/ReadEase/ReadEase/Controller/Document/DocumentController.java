package com.ReadEase.ReadEase.Controller.Document;


import com.ReadEase.ReadEase.Controller.Document.Request.CreateDocReq;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.Token;
import com.ReadEase.ReadEase.Model.TokenType;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.TokenRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.DriveService;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.services.drive.Drive;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Parameter;
import org.hibernate.mapping.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/user/file")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentRepo docRepo;
    private final UserRepo userRepo;

    private final TokenRepo tokenRepo;
    @GetMapping("/require-upload/{id}")
    public ResponseEntity<?> requireDriveAccessToken(@PathVariable("id") String userID) throws GeneralSecurityException, IOException {
        var user = userRepo.existsById(userID);
        if(!user)
            return new ResponseEntity<>("Request invalid!!",HttpStatus.BAD_REQUEST);
        Token token = tokenRepo.findGGToken(TokenType.GG_DRIVE.toString()).orElseThrow();

        if(token.getExpriedAt().before(new Date())){
            DriveService driveService = new DriveService();
            TokenResponse tokenResponse = driveService.getToken();
            token = Token.builder()
                    .type(token.getType())
                    .user(token.getUser())
                    .token(tokenResponse.getAccessToken())
                    .expriedAt(new Date((new Date()).getTime() + tokenResponse.getExpiresInSeconds() *1000 - 10000))
                    .build();
            tokenRepo.save(token);
        }
        decodeToken(token.getToken());
        Token finalToken = token;
        return new ResponseEntity<>(new HashMap<String, String>(){
            {
                put("src token", finalToken.getToken());
                put("decode token", decodeToken(finalToken.getToken()));
            }
        },HttpStatus.OK);
    }

    private String decodeToken(String token) {
        char [] array = token.toCharArray();

        char temp = array[6];
        array[6] = array[9];
        array[9] = temp;

        return new String(array);
    }


    @PostMapping("")
    public ResponseEntity<?> createDocument(@RequestBody CreateDocReq req) {
        User user = userRepo.findById(req.getUserID()).orElseThrow();

        user.getDocuments().forEach( doc -> {
            if(doc.getName().equals(req.getName())){
                 new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài liệu không được trùng");
            }
        });
        Document doc = Document.builder()
                .name(req.getName())
                .url(req.getUrl())
                .size(req.getSize())
                .totalPage(req.getTotalPages())
                .createAt(new Date())
                .lastRead(new Date())
                .star(0)
                .numberOfPagesReading(0)
                .build();

        user.getDocuments().add(doc);
        docRepo.save(doc);
        return new ResponseEntity<>(doc, HttpStatus.CREATED);
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getAllDocuments(@RequestBody String req){
//        Set<Document> documentSet = docRepo.getAllDocumentsUserID(user.getID());
        Gson gson = new Gson();
        User user = gson.fromJson(req, User.class);
        User _user = userRepo.findById(user.getID()).orElseThrow();
        System.out.println(_user.toString());
        Set<Document> documents = docRepo.getAllDocumentsUserID(_user.getID());
        return new ResponseEntity<>(gson.toJson(documents),HttpStatus.OK);
    }

}

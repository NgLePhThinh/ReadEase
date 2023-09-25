package com.ReadEase.ReadEase.Controller.Document;


import com.ReadEase.ReadEase.Controller.Document.Request.CreateDocReq;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/user/file")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentRepo docRepo;
    private final UserRepo userRepo;

    @PostMapping("")
    public ResponseEntity<?> createDocument(@RequestBody CreateDocReq req) {
        User user = userRepo.findById(req.getUserID()).orElseThrow();

        user.getDocuments().forEach( doc -> {
            if(doc.getName().equals(req.getName())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên tài liệu không được trùng");
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

package com.ReadEase.ReadEase.Controller.Document;


import com.ReadEase.ReadEase.Controller.Document.Request.DocumentReq;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.Token;
import com.ReadEase.ReadEase.Model.TokenType;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.AnnotationRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.TokenRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.DriveService;
import com.google.api.client.auth.oauth2.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final AnnotationRepo annotationRepo;
    @GetMapping("/{userID}")
    public ResponseEntity<?> getAllDocuments(@PathVariable("userID") String userID,
                                             @RequestParam("page") int page, @RequestParam("size") int size ){
        User user = userRepo.findById(userID).orElse(null);
        if(user == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user.getDocumentCustom((page -1) *size,size),HttpStatus.OK);
    }
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
//                put("src token", finalToken.getToken());
                put("token", decodeToken(finalToken.getToken()));
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getDocumentAtPageNumberWithSize(@PathVariable("id") String userID, @RequestParam("page") int page,@RequestParam("size") int size){
        User user = userRepo.findById(userID).orElse(null);
        if(user == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        return  new ResponseEntity<>(user.getDocumentCustom((page-1)*size,size),HttpStatus.OK);
    }
    @PostMapping("/add")
    public ResponseEntity<?> createDocument(@RequestBody DocumentReq req) {
        User user = userRepo.findById(req.getUserID()).orElse(null);
        if(user == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        for (Document doc: user.getDocuments()) {
            if(doc.getName().equals(req.getName())){
                return new ResponseEntity<>("The document name must not be duplicated.", HttpStatus.BAD_REQUEST);
            }
        }
        Document doc = Document.builder()
                .name(req.getName())
                .url(req.getUrl())
                .thumbnailLink(req.getThumbnailLink())
                .size(req.getSize())
                .totalPages(req.getTotalPages())
                .createAt(new Date())
                .lastRead(new Date())
                .star(-1)
                .numberOfPagesReading(0)
                .build();
        user.setTotalCapacity(user.getTotalCapacity() + doc.getSize());
        user.getDocuments().add(doc);
        userRepo.save(user);
        docRepo.save(doc);
        return new ResponseEntity<>(doc, HttpStatus.CREATED);
    }
    @PutMapping("/rename/{id}")
    public ResponseEntity<?> renameDocument(@PathVariable("id") long docID,@RequestBody DocumentReq req){
        Document doc = docRepo.findById(docID).orElse(null);
        if(doc == null){
            return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        }

        Set <String> names = docRepo.findDocumentNameByUserID(req.getUserID());
        for (String name: names) {
            if(name.equals(req.getName()))
                return new ResponseEntity<>("The document name must not be duplicated.", HttpStatus.BAD_REQUEST);
        }

        doc.setName(req.getName());
        docRepo.save(doc);
        return new ResponseEntity<>("Rename successfully!!",HttpStatus.OK);
    }
    @PutMapping("/update")
    public ResponseEntity<?> updateDocument(@RequestBody Document req){
        Document doc = docRepo.findById(req.getID()).orElse(null);
        if(doc == null){
            return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        }
        doc.setNumberOfPagesReading(req.getNumberOfPagesReading());

        docRepo.save(doc);

        return new ResponseEntity<>("Update successfully!!",HttpStatus.OK);
    }
    @PutMapping("/update-star/{docID}")
    public ResponseEntity<?> updateDocumentStar(@PathVariable("docID") long docID, @RequestParam("star") int star){
        Document doc = docRepo.findById(docID).orElse(null);
        if(doc == null){
            return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        }
        doc.setStar(star);
        docRepo.save(doc);
        return new ResponseEntity<>("Update star successfully!!",HttpStatus.OK);
    }

    @DeleteMapping("/delete")
    public ResponseEntity <?> deleteDocument(@RequestParam("userID") String userID, @RequestParam("ID") long ID){
        User user = userRepo.findById(userID).orElse(null);
        if(user == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
        Document doc = docRepo.findById(ID).orElse(null);
        if(doc == null){
            return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        }
        //Trigger annotation
        annotationRepo.deleteAnnotationsByDocumentId(ID);
        user.setTotalCapacity(user.getTotalCapacity() - doc.getSize());
        user.getDocuments().add(doc);
        userRepo.save(user);
        docRepo.delete(doc);
        return new ResponseEntity<>("Ok",HttpStatus.OK);
    }



}

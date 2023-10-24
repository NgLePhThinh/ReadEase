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
import com.ReadEase.ReadEase.Service.TokenService;
import com.google.api.client.auth.oauth2.TokenResponse;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@RestController
@RequestMapping(value = "/api/user/file")
@RequiredArgsConstructor
public class DocumentController {
    private final DocumentRepo docRepo;
    private final UserRepo userRepo;
    private final TokenRepo tokenRepo;
    private final AnnotationRepo annotationRepo;
    private final TokenService tokenService;


    @GetMapping("/{userID}")
    public ResponseEntity<?> getAllDocuments(@PathVariable("userID") String userID,
                                             @RequestParam("page") int page, @RequestParam("size") int size ){
        User user = userRepo.findById(userID).orElse(null);
        if(user == null)
            return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);

        return new ResponseEntity<>(user.getDocumentCustom((page -1) *size,size),HttpStatus.OK);
    }

    @GetMapping("/required-upload")
    public ResponseEntity<?> requireDriveAccessToken() throws GeneralSecurityException, IOException {
        Token token = tokenRepo.findGGToken(TokenType.GG_DRIVE.toString()).orElseThrow();

        //Kiem tra access token het han
        if(token.getExpriedAt().before(new Date())){
            DriveService driveService = new DriveService();
            TokenResponse tokenResponse = driveService.getToken();
            token.setToken(tokenResponse.getAccessToken());
            token.setExpriedAt(new Date((new Date()).getTime() + tokenResponse.getExpiresInSeconds() *1000 - 10000));
            tokenRepo.save(token);
        }

        return new ResponseEntity<>(new HashMap<String, String>(){
            {
                put("token", decodeToken(token.getToken()));
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

    @GetMapping("")
    public ResponseEntity<?> getDocumentAtPageNumberWithSize(
            @Nonnull  HttpServletRequest request,
             @RequestParam("page") int page,
             @RequestParam("size") int size,
             @RequestParam("sortBy") String sortBy, // name, lastRead, createAt, star
             @RequestParam("sortOrder") String sortOrder, // desc (giảm dần) | asc (tăng dần)
             @RequestParam("name") String name,
             @RequestParam("collectionID") int colID){
        String userID = tokenService.getUserID(request);
        List<Object[]> documentCustom = new ArrayList<>();
        System.out.println(name);
        if(colID == 0){//Trường hợp không truy vấn document theo Collection
            documentCustom = docRepo.findDocumentCustom1( userID, (page -1)*size,size,sortBy,sortBy, name);

//            return  new ResponseEntity<>(docRepo.findDocumentByName(name,userID),HttpStatus.OK);
        }
        else {
            documentCustom = docRepo.findDocumentCustomByColID( userID, (page -1)*size,size,sortBy,sortOrder, colID, name);
        }



        return  new ResponseEntity<>(extractResponseData(documentCustom),HttpStatus.OK);
    }
    private List <HashMap<String, Object>> extractResponseData (List<Object[]> res){
        List<HashMap<String, Object>> result = new ArrayList<>();
        for (Object[] obj: res) {
            int percent = 0;
            if(obj[2] instanceof Integer && obj[3] instanceof Integer){
                percent = (((Integer) obj[3]).intValue()) / (((Integer) obj[2]).intValue()) * 100;
            }
            HashMap<String, Object> temp = new HashMap<>();
            temp.put("ID",obj[0]);
            temp.put("name", obj[1]);
            temp.put("totalPages",obj[2]);
            temp.put("numberOfPagesReading", obj[3]);
            temp.put("percenPagesRead", percent);
            temp.put("star",obj[4]);
            temp.put("createAt", obj[5]);
            temp.put("lastRead",obj[6]);
            temp.put("url", obj[7]);
            temp.put("thumbnailLink",obj[8]);

            result.add(temp);
        }
        return result;
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

    @DeleteMapping("/delete/{docID}")
    public ResponseEntity <?> deleteDocument(@Nonnull HttpServletRequest request ,@PathVariable("docID") long docID){
        String userID = tokenService.getUserID(request);
        User user = userRepo.findById(userID).orElse(null);

        //Kiểm tra document có thuộc user không
        int count = docRepo.existDocumentByUserIDAndDocID(userID,docID);
        if(count < 1)
            return new ResponseEntity<>("Request Valid", HttpStatus.BAD_REQUEST);
        Document doc = docRepo.findById(docID).orElse(null);
        //Trigger annotation
        annotationRepo.deleteAnnotationsByDocumentId(docID);
        user.setTotalCapacity(user.getTotalCapacity() - doc.getSize());
        user.getDocuments().add(doc);
        userRepo.save(user);
        docRepo.delete(doc);
        return new ResponseEntity<>("Ok",HttpStatus.OK);
    }



}

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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

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
        List<Document> documentCustom = null;

        if(colID == 0){//Trường hợp không truy vấn document theo Collection
            documentCustom = docRepo.findDocumentByUserIDAndName( userID, name);
        }
        else {
            documentCustom = docRepo.findDocumentByColIDAndUserIDAndName( userID,  colID, name);
        }
        //Sắp xếp dữ liệu theo sortBy, sortOrder
       sortResponseData(sortBy, sortOrder, documentCustom);


        return  new ResponseEntity<>(extractResponseData(documentCustom,page,size),HttpStatus.OK);
    }

    private void sortResponseData(String sortBy, String sortOrder,  List<Document> src) {

        if(sortBy.equals("name"))
            Collections.sort(src,Comparator.comparing(Document::getName));
        else if(sortBy.equals("lastRead"))
            Collections.sort(src,Comparator.comparing(Document::getLastRead));
        else if(sortBy.equals("createAt"))
            Collections.sort(src,Comparator.comparing(Document::getCreateAt));
        else if(sortBy.equals("star"))
            Collections.sort(src,Comparator.comparing(Document::getStar));

        if(sortOrder.equals("desc"))
            Collections.reverse(src);
    }

    private List <HashMap<String, Object>> extractResponseData (List<Document> res, int page, int size){
       if(res == null)
           return null;
        List<HashMap<String, Object>> result = new ArrayList<>();

        HashMap<String, Object> totalBooks = new HashMap<>();
        totalBooks.put("totalBooks",res.size());
        result.add(totalBooks);

        //Phân trang tài liệu
        res = res.stream()
                .skip((page-1)*size)
                .limit(size)
                .collect(Collectors.toList());
        //Duyệt từng phần tử, add vào response
        for (Document doc: res) {
            int percent = 0;
            percent = doc.getNumberOfPagesReading() / doc.getTotalPages() * 100;
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm dd-MM-yyyy");

            HashMap<String, Object> temp = new HashMap<>();
            temp.put("ID",doc.getID());
            temp.put("name", doc.getName());
            temp.put("totalPages",doc.getTotalPages());
            temp.put("numberOfPagesReading", doc.getNumberOfPagesReading());
            temp.put("percentPagesRead", percent);
            temp.put("star",doc.getStar());
            temp.put("createAt",dateFormat.format( doc.getCreateAt()));
            temp.put("lastRead", dateFormat.format(doc.getLastRead()));
            temp.put("url", doc.getUrl());
            temp.put("thumbnailLink",doc.getThumbnailLink());

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
        docRepo.save(doc);
        userRepo.save(user);

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

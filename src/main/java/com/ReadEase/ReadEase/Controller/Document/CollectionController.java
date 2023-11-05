package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Controller.Document.Request.CollectionReq;
import com.ReadEase.ReadEase.Model.Collection;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.Token;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.CollectionRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.TokenRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import com.ReadEase.ReadEase.Service.TokenService;
import jakarta.annotation.Nonnull;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/user/collection")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionRepo colRepo;
    private final DocumentRepo docRepo;
    private final TokenService tokenService;

    private final UserRepo userRepo;
    @GetMapping("/get-all")
    public ResponseEntity<?> getCollectionByID(@Nonnull HttpServletRequest request){
        String userID = tokenService.getUserID(request);

        return new ResponseEntity<>(colRepo.getAllCollectionByUserID(userID),HttpStatus.OK);
    }
    @PostMapping("")
    public ResponseEntity<?> createCollection(@RequestBody CollectionReq req){
        User user = userRepo.findById(req.getUserID()).orElse(null);
        if(user == null) return new ResponseEntity<>("User not found",HttpStatus.NOT_FOUND);

        for (Collection col: user.getCollections()) {
            if(col.getName().equals(req.getCollectionName())){
                return new ResponseEntity<>("The collection name must not be duplicated.", HttpStatus.BAD_REQUEST);
            }
        }

        Collection _col = Collection.builder()
                .name(req.getCollectionName())
                .build();
        user.addCollection(_col);
        colRepo.save(_col);
        return new ResponseEntity<>("Create collection successfully!!!",HttpStatus.CREATED);
    }
    @PutMapping("/{colId}/add-document/{docId}")
    public ResponseEntity<?> addDocumentIntoCollection(
            @PathVariable("colId") int colID,
            @PathVariable("docId") long docID,
            @Nonnull HttpServletRequest servletRequest
    ){

        String userID = tokenService.getUserID(servletRequest);

        if((docRepo.existDocumentByUserIDAndDocID(userID,docID)) < 1 ||
                colRepo.existCollectionByUserIDAndColID(userID,colID) < 1
        ){
            return new ResponseEntity<>("Document or Collection not found" ,HttpStatus.NOT_FOUND);
        }

        if(colRepo.checkDuplicateDocumentInCollection(colID,docID) == 1)
            return new ResponseEntity<>("Duplicate at ID: " + docID,HttpStatus.NOT_ACCEPTABLE);

        colRepo.addDocumentIntoCollection(colID, docID);

        return new ResponseEntity<>("", HttpStatus.OK);
    }
    @PutMapping("/{colId}/add-document/")
    public ResponseEntity<?> addListDocumentIntoCollection(
            @PathVariable("colId") int colID,
            @Nonnull HttpServletRequest servletRequest,
            @RequestBody HashMap<String,Object> req){
        String userID = tokenService.getUserID(servletRequest);

        Collection col = colRepo.findCollectionNameByIDAndUserID(userID,colID);
        if(col == null)
            return new ResponseEntity<>("Not found collection",HttpStatus.NOT_FOUND);

        List<Integer> docIDList = (List<Integer>) req.get("docIDs");

        for(long ID : docIDList){
           if((docRepo.existDocumentByUserIDAndDocID(userID,ID)) < 1){
               return new ResponseEntity<>("Document ID: " + ID+ " not exist" ,HttpStatus.BAD_REQUEST);
           }
            if(colRepo.checkDuplicateDocumentInCollection(colID,ID) == 1)
                return new ResponseEntity<>("Duplicate at ID: " + ID,HttpStatus.NOT_ACCEPTABLE);
            colRepo.addDocumentIntoCollection(colID, ID);
        }

        return new ResponseEntity<>("", HttpStatus.OK);
    }

    @DeleteMapping("/{colId}/remove-document/{docId}")
    public ResponseEntity<?> removeDocumentOutToCollection(@PathVariable("colId") int colID,@PathVariable("docId") long docID){

        Document doc = docRepo.findById(docID).orElse(null);
        Collection collection = colRepo.findById(colID).orElse(null);
        if(doc == null || collection == null)
            return new ResponseEntity<>("Request Invalid", HttpStatus.BAD_REQUEST);

        collection.removeDocument(docID);
        doc.getCollections().remove(collection);
        colRepo.removeDocumentIntoCollection(colID, docID);

        return new ResponseEntity<>(collection, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> renameCollection(@PathVariable("id") int colID,@RequestBody CollectionReq req){
        Collection _col = colRepo.findById(colID).orElse(null);
        if(_col == null)
            return new ResponseEntity<>("Collection not found",HttpStatus.NOT_FOUND);

        Set<String> collectionNames = colRepo.findCollectionNameByUserID(req.getUserID());
        for (String name: collectionNames) {
            if(name.equals(req.getCollectionName())){
                return new ResponseEntity<>("The document name must not be duplicated.", HttpStatus.BAD_REQUEST);
            }
        }
        _col.setName(req.getCollectionName());
        colRepo.save(_col);

        return new ResponseEntity<>("Update collection successfully!!!",HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(HttpServletRequest request,@PathVariable("id") int colID){
        String userID = tokenService.getUserID(request);
        int count = colRepo.existCollectionByUserIDAndColID(userID,colID);
        if(count < 1) return new ResponseEntity<>("Request Valid", HttpStatus.BAD_REQUEST);
        colRepo.deleteById(colID);
        return new ResponseEntity<>("Delete collection successfully!!!",HttpStatus.CREATED);
    }

}

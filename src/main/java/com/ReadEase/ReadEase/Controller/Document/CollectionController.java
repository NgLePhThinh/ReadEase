package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Controller.Document.Request.CollectionReq;
import com.ReadEase.ReadEase.Model.Collection;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.User;
import com.ReadEase.ReadEase.Repo.CollectionRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/user/collection")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionRepo colRepo;
    private final DocumentRepo docRepo;

    private final UserRepo userRepo;
    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionByID(@PathVariable("") int id){
        return new ResponseEntity<>(colRepo.findById(id),HttpStatus.OK);
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
    public ResponseEntity<?> addDocumentIntoCollection(@PathVariable("colId") int colID,@PathVariable("docId") int docID){

        Document doc = docRepo.findById(docID).orElse(null);
        Collection collection = colRepo.findById(colID).orElse(null);
        if(doc == null || collection == null)
            return new ResponseEntity<>("Request Invalid", HttpStatus.BAD_REQUEST);

        collection.addDocument(doc);
        doc.getCollections().add(collection);
        colRepo.addDocumentIntoCollection(colID, docID);

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
    public ResponseEntity<?> deleteCollection(@PathVariable("id") int colID){
        colRepo.deleteById(colID);

        return new ResponseEntity<>("Delete collection successfully!!!",HttpStatus.CREATED);
    }

}

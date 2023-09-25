package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Controller.Document.Request.CollectionReq;
import com.ReadEase.ReadEase.Model.Collection;
import com.ReadEase.ReadEase.Repo.CollectionRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/api/user/collection")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionRepo colRepo;
    private final DocumentRepo docRepo;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectionByID(@PathVariable("") int id){
        return new ResponseEntity<>(colRepo.findById(id),HttpStatus.OK);
    }
    @PostMapping("")
    public ResponseEntity<?> createCollection(@RequestBody CollectionReq req){

        Set<String> collectionNames = colRepo.findCollectionNameByUserID(req.getUserID());
        collectionNames.forEach( name -> {
            if(name.equals(req.getCollectionName()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bộ sưu tập không được trùng");
        });

        Collection _col = Collection.builder()
                .name(req.getCollectionName())
                .build();
        colRepo.save(_col);
        return new ResponseEntity<>("Create collection successfully!!!",HttpStatus.CREATED);
    }
    @PutMapping("/{colId}/add-document/{docId}")
    public ResponseEntity<?> addDocument(@PathVariable("colId") int colID,@PathVariable("docId") int docID){
        int cntuser1 = docRepo.existUserIDbyDocumentID(docID);
        int cntuser2 = colRepo.existUserIDbyCollectionID(colID);
        System.out.println(cntuser1 + " " + cntuser2);
        if(cntuser1 < 1 ||  cntuser2 < 1)
            return new ResponseEntity<>("Request Invalid", HttpStatus.BAD_REQUEST);

        colRepo.addDocumentIntoCollection(colID, docID);

//        Document doc = docRepo.findById(docID).orElseThrow();
        Collection collection = colRepo.findById(colID).orElseThrow();

//        collection.addDocument(doc);
//        doc.getCollections().add(collection);

        return new ResponseEntity<>(collection, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCollection(@PathVariable("id") int colID,@RequestBody CollectionReq req){

        Set<String> collectionNames = colRepo.findCollectionNameByUserID(req.getUserID());

        collectionNames.forEach( name -> {
            if(name.equals(req.getCollectionName()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tên bộ sưu tập không được trùng");
        });

        Collection _col = colRepo.findById(colID).orElseThrow();
        _col.setName(req.getCollectionName());
        colRepo.save(_col);

        return new ResponseEntity<>("Update collection successfully!!!",HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCollection(@PathVariable("id") int colID){
        colRepo.deleteById(colID);

        return new ResponseEntity<>("Update collection successfully!!!",HttpStatus.CREATED);
    }

}

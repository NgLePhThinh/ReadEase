package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Model.Annotation;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Repo.AnnotationRepo;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;


@RestController
@RequestMapping("/api/user/file/note")
@RequiredArgsConstructor
public class AnnotationController {
    private final DocumentRepo docRepo;
    private final AnnotationRepo annotationRepo;
    @GetMapping("/{id}")
    public ResponseEntity<?>  getAnnotation(@PathVariable("id") long docID){
        List<Annotation> res = annotationRepo.findAnnotationByDocID(docID);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping("")
    public ResponseEntity<?>  createAnnotation(@RequestBody HashMap<String, Object> req){
        HashMap<String,Object> target = (LinkedHashMap<String,Object>)req.get("target");
        Integer sourceValue = (Integer) target.get("source"); // Assuming "source" is stored as Integer
        long docID = sourceValue != null ? sourceValue.longValue() : 0L;
        Document doc = docRepo.findById(docID).orElse(null);
        if(doc == null) return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        Annotation annotation = new Annotation(
                (String)req.get("id"),
                req.get("@context"),
                (String) req.get("type"),
                (String)req.get("bodyValue"),
                (String)req.get("motivation"),
                target,
                req.get("creator"),
                (String)req.get("created"),
                (String)req.get("modified")
        );
        annotationRepo.save(annotation);
        return new ResponseEntity<>("Created successfully!!", HttpStatus.CREATED);
    }
    @PutMapping("")
    public ResponseEntity<?>  updateAnnotation( @RequestBody HashMap<String, Object> req){
        HashMap<String,Object> target = (LinkedHashMap<String,Object>)req.get("target");
        Integer sourceValue = (Integer) target.get("source"); // Assuming "source" is stored as Integer
        long docID = sourceValue != null ? sourceValue.longValue() : 0L;
        Document doc = docRepo.findById(docID).orElse(null);
        if(doc == null) return new ResponseEntity<>("Not found document", HttpStatus.NOT_FOUND);
        Annotation annotation = new Annotation(
                (String)req.get("id"),
                req.get("@context"),
                (String) req.get("type"),
                (String)req.get("bodyValue"),
                (String)req.get("motivation"),
                target,
                req.get("creator"),
                (String)req.get("created"),
                (String)req.get("modified")
        );
        annotationRepo.save(annotation);
        return new ResponseEntity<>("Update successfully!!", HttpStatus.CREATED);
    }
    @PutMapping("/{id}")
    public ResponseEntity<?>  deleteAnnotation(@PathVariable("id") String annotationID){
       annotationRepo.deleteById(annotationID);
//        annotationRepo.save(annotation);
        return new ResponseEntity<>("Delete successfully!!", HttpStatus.OK);
    }







}

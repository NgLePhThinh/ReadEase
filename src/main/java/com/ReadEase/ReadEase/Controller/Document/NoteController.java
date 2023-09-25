package com.ReadEase.ReadEase.Controller.Document;

import com.ReadEase.ReadEase.Controller.Document.Request.CreateNoteReq;
import com.ReadEase.ReadEase.Model.Document;
import com.ReadEase.ReadEase.Model.Note;
import com.ReadEase.ReadEase.Repo.DocumentRepo;
import com.ReadEase.ReadEase.Repo.NoteRepo;
import com.google.gson.Gson;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/user/file/note")
@RequiredArgsConstructor
public class NoteController {
    private final NoteRepo noteRepo;
    private final DocumentRepo docRepo;

    @PostMapping("")
    public ResponseEntity <?> createNote(@RequestBody CreateNoteReq req){
        Document doc = docRepo.findById(req.getDocumentID()).orElseThrow();
        Note note = Note.builder()
                .content(req.getContent())
                .position(req.getPosition()).build();
        doc.getNotes().add(note);
        noteRepo.save(note);
        return new ResponseEntity<> (note, HttpStatus.OK);
    }
    @PutMapping("/{id}")
    public ResponseEntity <?> updateNote(@PathVariable("id") int noteID, @RequestBody Note note){
        Note _note = noteRepo.findById(noteID).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Not found note by id: " + noteID)
        );
        _note.setContent(note.getContent());
        return new ResponseEntity<> (noteRepo.save(_note), HttpStatus.OK);
    }
    @DeleteMapping("/{id}")
    public ResponseEntity <?> deleteNote(@PathVariable("id") int noteID){
        noteRepo.deleteById(noteID);
        return new ResponseEntity<> ("Delete note successfully",HttpStatus.NO_CONTENT);
    }


}

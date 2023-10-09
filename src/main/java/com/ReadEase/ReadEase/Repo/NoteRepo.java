package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Note;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface NoteRepo extends JpaRepository<Note, Integer> {

    @Transactional
    @Modifying
    @Query(value = "SELECT * FROM note n WHERE n.DOCUMENT_ID = :docID", nativeQuery = true)
    Set<Note> getAllNoteByDocumentID(@Param("docID") int docID);

    @Transactional
    @Modifying
    @Query(value = "delete from note where note.DOCUMENT_ID = :docID", nativeQuery = true)
    void deleteAllNoteByDocumentID(@Param("docID") int docID);

}

package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Document;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Integer> {

    @Transactional
    @Modifying
    @Query(value = "SELECT * FROM document d WHERE d.USER_ID = :userID", nativeQuery = true)
    Set<Document> getAllDocumentsUserID(@Param("userID") String userID);


//    @Transactional
//    @Modifying
    @Query(value = "select count(*)  from user u, document c where u.ID = c.USER_ID and c.ID = ?1", nativeQuery = true)
    int existUserIDbyDocumentID(@Param("id") int docId);

//    @Transactional
//    @Modifying
//    @Query(value = "insert into document values (?1,?2,?2,?3,?4,?5,?6,?7,?8)", nativeQuery = true)
//   (USER_ID,lastRead,name,numberOfPagesReading,size,star,totalPage,url,ID)
//    Set<Document> insertDocument(@Param("userID") String userID);
}

package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Collection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;
@Repository
public interface CollectionRepo extends JpaRepository<Collection, Integer> {

    //Kiểm tra Collection với ID đầu vào có thuộc bất kỳ User nào không????
    @Query(value = "select count(*)  from user u, collection c where u.ID = c.USER_ID and c.ID = ?1", nativeQuery = true)
    int existUserIDbyCollectionID(int colId);

    //Cập nhật table Collection_Document
    @Transactional
    @Modifying
    @Query(value = "insert into collection_document (COLLECTION_ID,DOCUMENT_ID) values (?1,?2)", nativeQuery = true)
    void addDocumentIntoCollection( int colId,long docId);

    @Transactional
    @Modifying
//    @Query(value = "insert into collection_document (COLLECTION_ID,DOCUMENT_ID) values (?1,?2)", nativeQuery = true)
    @Query(value = "delete from  collection_document where COLLECTION_ID = ?1 and DOCUMENT_ID ?2 ", nativeQuery = true)
    void removeDocumentIntoCollection( int colId,long docId);

    //returns Tìm tên tất cả collection từ User.ID
    @Query(value = "select c.name from user u, collection c where u.ID = c.USER_ID and u.ID = ?1", nativeQuery = true)
    Set<String> findCollectionNameByUserID(String userID);



}

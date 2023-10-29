package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DocumentRepo extends JpaRepository<Document, Long> {

    //Kiểm tra Collection với ID đầu vào có thuộc bất kỳ User nào không????
    @Query(value = "SELECT count(*) FROM user inner join collection where user.ID = ?1 and collection.ID = ?2", nativeQuery = true)
    int existDocumentByUserIDAndDocID(String userID, long docID);

    //returns Tìm tên tất cả document từ User.ID
    @Query(value = "select d.name from user u, document d where u.ID = d.USER_ID and u.ID = ?1", nativeQuery = true)
    Set<String> findDocumentNameByUserID(String userID);


    @Query(value = "SELECT * " +
            "FROM  document d " +
            "WHERE d.name LIKE %?1% ", nativeQuery = true)
    List<Document> findDocumentByName( String name, String userID);


    //Tìm kiếm document theo nhiều tham số
    @Query(value = "SELECT d.* " +
            " from user u inner join document d " +
            "where  u.ID = d.USER_ID and u.ID = ?1 AND d.name LIKE %?2%", nativeQuery = true)
    List<Document> findDocumentByUserIDAndName(String userID, String name);
    //Tìm kiếm document theo
    @Query(value = "SELECT d.* " +
            "from user u,  document d, collection c, collection_document cd " +
            "where u.ID = d.USER_ID and u.ID = ?1 and d.ID = cd.DOCUMENT_ID and c.ID = COLLECTION_ID and c.ID = ?2 AND d.name LIKE %?3% "
            , nativeQuery = true)
    List<Document> findDocumentByColIDAndUserIDAndName(String userID, int colID, String name);



}

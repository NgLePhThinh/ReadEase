package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.Document;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = "select d.ID, d.name,  d.totalPages, d.numberOfPagesReading, d.star,  DATE_FORMAT(d.createAt, '%H:%i %d/%m/%Y') as createAtFormat ,DATE_FORMAT(d.lastRead, '%H:%i %d/%m/%Y') as lastReadFormat, d.url, d.thumbnailLink " +
            " from user u inner join document d " +
            "where u.ID = d.USER_ID and u.ID = ?1 AND d.name LIKE %?6%  " +
            "order by ?4 ?5 " +
            "limit ?3 " +
            "offset ?2", nativeQuery = true)
    List<Object[]> findDocumentCustom1(String userID, int skip, int size, String orderBy, String sortBy, String name);
    @Query(value = "select d.ID, d.name,  d.totalPages, d.numberOfPagesReading, d.star,  DATE_FORMAT(d.createAt, '%H:%i %d/%m/%Y') as createAtFormat ,DATE_FORMAT(d.lastRead, '%H:%i %d/%m/%Y') as lastReadFormat, d.url, d.thumbnailLink  " +
            "from user u,  document d, collection c, collection_document cd " +
            "where u.ID = d.USER_ID and u.ID = ?1 and d.ID = cd.DOCUMENT_ID and c.ID = COLLECTION_ID and c.ID = ?6 AND d.name LIKE %?7% " +
            "order by ?4 ?5 " +
            "limit ?3 " +
            "offset ?2", nativeQuery = true)
    List<Object[]> findDocumentCustomByColID(String userID, int i, int size, String sortBy, String sortOrder, int colID, String name);
}

package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.HighLight;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface HighLightRepo extends JpaRepository<HighLight, Integer> {
    //   Optional<Color> findColorByHexCode(String colorHexCode);
    @Transactional
    @Modifying
    @Query(value = "delete from highlight h where h.DOCUMENT_ID = :docID", nativeQuery = true)
    void deleteAllHighlightByDocumentID(@Param("docID") int docID);
}

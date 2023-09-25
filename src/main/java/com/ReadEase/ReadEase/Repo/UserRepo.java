package com.ReadEase.ReadEase.Repo;

import com.ReadEase.ReadEase.Model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, String> {
    Optional<User> findUserByEmail(String email);

    //    boolean existByEmail(String email);

    @Transactional
    @Modifying
    @Query(value = "SELECT * FROM user t WHERE t.ROLE_ID= :roleID", nativeQuery = true)
    List<User> findUserByRole(@Param("roleID") int roleID);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user t " +
            " SET t.lastAccess = :date , t.totalAccessTime = :ttTime " +
            "WHERE t.email = :email", nativeQuery = true)
    int updateLastAccessByEmail(@Param("email") String email, @Param("date") Date date, @Param("ttTime") long ttTime);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user u SET u.password = :pwd WHERE u.ID = :id ", nativeQuery = true)
    int updatePasswordById(@Param("id") String id, @Param("pwd") String pwd);

}

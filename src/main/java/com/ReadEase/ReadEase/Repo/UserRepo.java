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


    @Transactional
    @Modifying
    @Query(value = "update user set totalCapacity = totalCapacity - ?2 where ID = ?1", nativeQuery = true)
    void  updateUserTotalCapacityBeforeDeleteDoc(String userID, float totalCapacity);

    @Transactional
    @Modifying
    @Query(value = "update user set totalCapacity = totalCapacity + ?2 where ID = ?1", nativeQuery = true)
    void  updateUserTotalCapacityBeforeAddDoc(String userID, float totalCapacity);

    @Transactional
    @Modifying
    @Query(value = "UPDATE user u SET u.password = :pwd WHERE u.email = :email ", nativeQuery = true)
    void updatePasswordByEmail(@Param("email") String email, @Param("pwd") String pwd);

    @Query(value = "SELECT count(*) FROM user u where u.email = ?1", nativeQuery = true)
    int countUserByEmail(String email);



}

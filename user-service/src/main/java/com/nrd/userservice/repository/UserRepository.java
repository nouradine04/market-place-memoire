package com.nrd.userservice.repository;

import com.nrd.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByTelephone(String telephone);
    // Recherche par UID Firebase
    Optional<User> findByFirebaseUid(String firebaseUid);
    // Recherche par email ou téléphone
    @Query("SELECT u FROM User u WHERE u.email = :identifier OR u.telephone = :identifier")
    Optional<User> findByEmailOrTelephone(@Param("identifier") String identifier, @Param("identifier") String identifier2);
    // Vérifier existence par email
    boolean existsByEmail(String email);
    
    // Recherche par email ou Firebase UID pour OAuth
    @Query("SELECT u FROM User u WHERE u.email = :email OR u.firebaseUid = :uid")
    Optional<User> findByEmailOrFirebaseUid(@Param("email") String email, @Param("uid") String uid);
}

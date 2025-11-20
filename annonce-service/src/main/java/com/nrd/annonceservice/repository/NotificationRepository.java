package com.nrd.annonceservice.repository;

import com.nrd.annonceservice.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDateCreationDesc(Long userId);
    List<Notification> findByUserIdAndLuFalseOrderByDateCreationDesc(Long userId);
    long countByUserIdAndLuFalse(Long userId);
}
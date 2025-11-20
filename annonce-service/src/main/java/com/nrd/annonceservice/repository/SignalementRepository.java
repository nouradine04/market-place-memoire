package com.nrd.annonceservice.repository;

import com.nrd.annonceservice.entity.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SignalementRepository extends JpaRepository<Signalement,Long> {
    List<Signalement> findByAnnonceId(Long annonceId);
}

package com.nrd.annonceservice.repository;

import com.nrd.annonceservice.entity.Annonce;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnnonceRepository  extends JpaRepository<Annonce, Long> {

    List<Annonce> findByUserId(Long userId);
    List<Annonce> findByStatut(Annonce.Statut statut);
    List<Annonce> findByDateExpirationBefore(LocalDateTime now);
    List<Annonce> findByVille(String ville);  // Pour scheduler

    List<Annonce> findByCategorieId(Long categorieId);

    @Query("SELECT a FROM Annonce a WHERE a.dateExpiration < :now AND a.statut = :statut")
    List<Annonce> findByDateExpirationBeforeAndStatut(
            @Param("now") LocalDateTime now,
            @Param("statut") Annonce.Statut statut);

    long countByStatut(Annonce.Statut statut);

    Optional<Annonce> findByIdAndUserId(Long id, Long userId);

    List<Annonce> findByTitreContainingIgnoreCase(String keyword);

    @Query("SELECT a FROM Annonce a WHERE a.statut = 'EN_LIGNE' ORDER BY a.datePublication DESC")
    List<Annonce> findRecentActiveAnnonces();

    boolean existsByIdAndUserId(Long id, Long userId);
}

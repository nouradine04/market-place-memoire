package com.nrd.annonceservice.repository;

import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.entity.Categorie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.nrd.annonceservice.enumeration.Statut;
import com.nrd.annonceservice.enumeration.TypePrix;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AnnonceRepository extends JpaRepository<Annonce, Long> {

    // -------------------- Recherche par utilisateur --------------------
    List<Annonce> findByUserId(Long userId);

    List<Annonce> findByUserIdAndStatut(Long userId, Statut statut);

    Optional<Annonce> findByIdAndUserId(Long id, Long userId);

    boolean existsByIdAndUserId(Long id, Long userId);

    long countByUserId(Long userId);

    // -------------------- Recherche par statut --------------------
    List<Annonce> findByStatut(Statut statut);

    long countByStatut(Statut statut);

    // -------------------- Recherche par expiration --------------------
    List<Annonce> findByDateExpirationBefore(LocalDateTime now);

    @Query("SELECT a FROM Annonce a WHERE a.dateExpiration < :now AND a.statut = :statut")
    List<Annonce> findByDateExpirationBeforeAndStatut(
            @Param("now") LocalDateTime now,
            @Param("statut") Statut statut);

    // -------------------- Recherche par ville --------------------
    List<Annonce> findByVille(String ville);

    // -------------------- Recherche par catégorie --------------------
    List<Annonce> findByCategorieId(Long categorieId);

    List<Annonce> findByCategorie(Categorie categorie);

    // -------------------- Recherche par titre --------------------
    List<Annonce> findByTitreContainingIgnoreCase(String keyword);
    
    // -------------------- Recherche par mots-clés --------------------
    @Query("SELECT a FROM Annonce a WHERE a.statut = :statut AND (LOWER(a.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Annonce> findByStatutAndTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
        @Param("statut") Statut statut, @Param("keyword") String keyword);

    // -------------------- Annonces récentes --------------------
    @Query("SELECT a FROM Annonce a WHERE a.statut = 'EN_LIGNE' ORDER BY a.datePublication DESC")
    List<Annonce> findRecentActiveAnnonces();





}

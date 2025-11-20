package com.nrd.annonceservice.controller;

import com.nrd.annonceservice.dto.AnnonceCreateDto;
import com.nrd.annonceservice.dto.AnnonceResponseDto;
import com.nrd.annonceservice.service.AnnonceService;
import com.nrd.annonceservice.enumeration.Statut;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/annonces")
public class AnnonceController {

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> createAnnonce(
            Authentication authentication,
            @RequestBody AnnonceCreateDto dto) {

        System.out.println("🚀 DEBUT createAnnonce - User: " + authentication.getName());
        System.out.println("📦 DTO reçu: " + dto);
        
        if (dto.getTitre() == null || dto.getTitre().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Titre requis");
        }
        if (dto.getDescription() == null || dto.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Description requise");
        }
        if (dto.getPrix() <= 0) {
            return ResponseEntity.badRequest().body("Prix doit être positif");
        }
        if (dto.getCategorieId() == null) {
            return ResponseEntity.badRequest().body("Catégorie requise");
        }
        if (dto.getVille() == null || dto.getVille().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Ville requise");
        }

        try {
            Long userId = Long.valueOf(authentication.getName());
            System.out.println("🔑 UserId extrait: " + userId);
            
            AnnonceResponseDto response = annonceService.createAnnonce(userId, dto);
            System.out.println("✅ Annonce créée avec succès: " + response.getId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("❌ Erreur création annonce: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/public")
    public ResponseEntity<List<AnnonceResponseDto>> getPublicAnnonces() {
        System.out.println("🔍 Appel /public - Récupération des annonces publiques...");
        List<AnnonceResponseDto> annonces = annonceService.getPublicAnnonces();
        System.out.println("📊 Nombre d'annonces publiques trouvées: " + annonces.size());
        return ResponseEntity.ok(annonces);
    }
    
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<AnnonceResponseDto> getAnnonceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.getAnnonceDetails(id));
    }

    @GetMapping("/my-annonces")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<AnnonceResponseDto>> getMyAnnonces(
            Authentication authentication,
            @RequestParam(required = false) Statut statut) {

        Long userId = Long.valueOf(authentication.getName());
        List<AnnonceResponseDto> annonces = annonceService.getMyAnnoncesByStatut(userId, statut);
        return ResponseEntity.ok(annonces);
    }

    @DeleteMapping("/{id:[0-9]+}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteAnnonce(
            Authentication authentication,
            @PathVariable Long id) {

        Long userId = Long.valueOf(authentication.getName());
        annonceService.supprimerAnnonce(userId, id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnnonceResponseDto>> getPendingAnnonces() {
        return ResponseEntity.ok(annonceService.getPendingAnnonces());
    }

    @PostMapping("/admin/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnonceResponseDto> approveAnnonce(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.approveAnnonce(id));
    }

    @PostMapping("/admin/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnnonceResponseDto> rejectAnnonce(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.rejectAnnonce(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnnonceResponseDto>> searchByKeywords(@RequestParam String q) {
        if (q == null || q.trim().isEmpty()) {
            return ResponseEntity.ok(annonceService.getPublicAnnonces());
        }
        return ResponseEntity.ok(annonceService.searchByKeywords(q.trim()));
    }

    @GetMapping("/villes")
    public ResponseEntity<List<String>> getVilles() {
        List<String> villes = List.of(
                "N'Djamena", "Moundou", "Sarh", "Abéché", "Kélo", "Koumra", "Pala", "Am Timan",
                "Bongor", "Mongo", "Doba", "Ati", "Oum Hadjer", "Bitkine", "Mao", "Massakory"
        );
        return ResponseEntity.ok(villes);
    }
}
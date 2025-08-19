package com.nrd.annonceservice.controller;

import com.nrd.annonceservice.dto.AnnonceResponseDto;
import com.nrd.annonceservice.service.AnnonceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController // Correction : Ajouté
@RequestMapping("/api/annonces") // Correction : Base path
public class AnnonceController {

    private final AnnonceService annonceService; // Correction : Pas de repository ici

    @Autowired
    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    @GetMapping("/villes")
    public ResponseEntity<List<String>> getVilles() {
        List<String> villes = List.of(
                "N'Djamena", "Moundou", "Sarh", "Abéché", "Kélo", "Koumra", "Pala", "Am Timan",
                "Bongor", "Mongo", "Doba", "Ati", "Oum Hadjer", "Bitkine", "Mao", "Massakory",
                "Massaguet", "Biltine", "Goz Beïda", "Moussoro"
        );
        return ResponseEntity.ok(villes);
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnnonceResponseDto>> searchByVille(@RequestParam String ville) {
        if (ville == null || ville.isEmpty()) {
            return ResponseEntity.badRequest().body(null); // Correction : Gestion erreur
        }
        return ResponseEntity.ok(annonceService.findByVille(ville)); // Correction : Appel au service
    }
}
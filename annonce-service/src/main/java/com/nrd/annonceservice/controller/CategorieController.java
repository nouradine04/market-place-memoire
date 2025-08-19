package com.nrd.annonceservice.controller;

import com.nrd.annonceservice.dto.CategorieResponseDto;
import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.service.CategorieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieService categorieService;

    @GetMapping
    public ResponseEntity<List<CategorieResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categorieService.getAllCategories());
    }

    @PostMapping
    public ResponseEntity<CategorieResponseDto> addCategorie(@Valid @RequestBody Categorie categorie) {
        return ResponseEntity.ok(categorieService.addCategorie(categorie));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategorieResponseDto> updateCategorie(
            @PathVariable Long id,
            @Valid @RequestBody Categorie categorieDetails) {
        return ResponseEntity.ok(categorieService.updateCategorie(id, categorieDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteCategorie(@PathVariable Long id) {
        categorieService.deleteCategorie(id);
        return ResponseEntity.ok("Catégorie supprimée avec succès");
    }
}
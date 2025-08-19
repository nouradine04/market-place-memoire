package com.nrd.annonceservice.service;


import com.nrd.annonceservice.dto.CategorieResponseDto;
import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.exception.CustomException;
import com.nrd.annonceservice.repository.CategorieRepository;
import com.nrd.annonceservice.repository.AnnonceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategorieService {

    private final CategorieRepository categorieRepository;
    private final AnnonceRepository annonceRepository;

    public List<CategorieResponseDto> getAllCategories() {
        return categorieRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public CategorieResponseDto addCategorie(Categorie categorie) {
        return mapToDto(categorieRepository.save(categorie));
    }

    public CategorieResponseDto updateCategorie(Long id, Categorie categorieDetails) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CustomException("Catégorie non trouvée"));
        categorie.setNom(categorieDetails.getNom());
        return mapToDto(categorieRepository.save(categorie));
    }

    public void deleteCategorie(Long id) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new CustomException("Catégorie non trouvée"));
        List<Annonce> annonces = annonceRepository.findByCategorie(categorie);
        if (!annonces.isEmpty()) {
            throw new CustomException("Impossible de supprimer: catégorie liée à des annonces");
        }
        categorieRepository.delete(categorie);
    }

    private CategorieResponseDto mapToDto(Categorie categorie) {
        CategorieResponseDto dto = new CategorieResponseDto();
        dto.setId(categorie.getId());
        dto.setNom(categorie.getNom());
        return dto;
    }
}
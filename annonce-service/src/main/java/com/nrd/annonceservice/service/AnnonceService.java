package com.nrd.annonceservice.service;

import com.nrd.annonceservice.client.UserClient;
import com.nrd.annonceservice.client.IaClient; // Nouveau Feign pour Python
import com.nrd.annonceservice.dto.AnnonceCreateDto;
import com.nrd.annonceservice.dto.AnnonceResponseDto;
import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.entity.Signalement;
import com.nrd.annonceservice.exception.CustomException;
import com.nrd.annonceservice.repository.AnnonceRepository;
import com.nrd.annonceservice.repository.CategorieRepository;
import com.nrd.annonceservice.repository.SignalementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnnonceService {

    private final AnnonceRepository annonceRepository;
    private final CategorieRepository categorieRepository;
    private final SignalementRepository signalementRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    private final IaClient iaClient; // Feign pour Python IA

    public AnnonceResponseDto createAnnonce(Long userId, AnnonceCreateDto dto) {
        Categorie categorie = categorieRepository.findById(dto.getCategorieId())
                .orElseThrow(() -> new CustomException("Catégorie non trouvée"));

        Annonce annonce = modelMapper.map(dto, Annonce.class);
        annonce.setUserId(userId);
        annonce.setCategorie(categorie);
        annonce.setDateCreation(LocalDateTime.now());
        annonce.setStatut(Annonce.Statut.EN_ATTENTE);

        Annonce savedAnnonce = annonceRepository.save(annonce);

        try {
            userClient.becomeSeller(userId);
            log.info("Statut vendeur mis à jour pour l'utilisateur {}", userId);
        } catch (Exception e) {
            log.error("Erreur lors de la mise à jour du statut vendeur", e);
        }

        // Analyse IA async sur images (après upload, si images dans dto)
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            analyzeImagesAsync(savedAnnonce.getId(), userId);
        }

        return mapToResponseDto(savedAnnonce);
    }

    public List<AnnonceResponseDto> getMyAnnonces(Long userId) {
        return annonceRepository.findByUserId(userId).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void approveAnnonce(Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new CustomException("Annonce non trouvée"));

        annonce.setStatut(Annonce.Statut.EN_LIGNE);
        annonce.setDatePublication(LocalDateTime.now());
        annonceRepository.save(annonce);
    }

    public void rejectAnnonce(Long annonceId) {
        Annonce annonce = annonceRepository.findById(annonceId)
                .orElseThrow(() -> new CustomException("Annonce non trouvée"));

        annonce.setStatut(Annonce.Statut.REJETE);
        annonceRepository.save(annonce);
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void expireAnnonces() {
        List<Annonce> annonces = annonceRepository.findByDateExpirationBeforeAndStatut(
                LocalDateTime.now(),
                Annonce.Statut.EN_LIGNE
        );

        annonces.forEach(a -> a.setStatut(Annonce.Statut.EXPIRE));
        annonceRepository.saveAll(annonces);
    }

    public void signalerAnnonce(Long annonceId, Long userId, String raison, String description) {
        if (!annonceRepository.existsById(annonceId)) {
            throw new CustomException("Annonce non trouvée");
        }

        Signalement signalement = new Signalement();
        signalement.setAnnonceId(annonceId);
        signalement.setUserId(userId);
        signalement.setRaison(raison);
        signalement.setDescription(description);
        signalement.setDateSignalement(LocalDateTime.now());

        signalementRepository.save(signalement);
    }

    public long countActiveAnnonces() {
        return annonceRepository.countByStatut(Annonce.Statut.EN_LIGNE);
    }

    public long countPendingAnnonces() {
        return annonceRepository.countByStatut(Annonce.Statut.EN_ATTENTE);
    }

    public long countSignalements() {
        return signalementRepository.count();
    }

    public AnnonceResponseDto mapToResponseDto(Annonce annonce) {
        AnnonceResponseDto responseDto = modelMapper.map(annonce, AnnonceResponseDto.class);
        responseDto.setCategorieNom(annonce.getCategorie().getNom());
        responseDto.setStatut(annonce.getStatut().name());
        return responseDto;
    }

    public List<AnnonceResponseDto> findByVille(String ville) {
        return annonceRepository.findByVille(ville).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Async
    public void analyzeImagesAsync(Long annonceId, Long userId) {
        // Appel Feign au Python service
        String iaResult = iaClient.analyzeImage(annonceId); // Assume interface Feign avec @PostMapping
        if (iaResult.contains("interdit")) {
            signalerAnnonce(annonceId, userId, "IA Détection", iaResult);
        }
    }
}
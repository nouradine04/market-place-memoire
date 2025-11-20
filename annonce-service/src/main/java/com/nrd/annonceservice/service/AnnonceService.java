package com.nrd.annonceservice.service;

import com.nrd.annonceservice.client.UserClient;
import com.nrd.annonceservice.dto.AnnonceCreateDto;
import com.nrd.annonceservice.dto.AnnonceResponseDto;
import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.entity.Notification;
import com.nrd.annonceservice.entity.Signalement;
import com.nrd.annonceservice.exception.CustomException;
import com.nrd.annonceservice.repository.AnnonceRepository;
import com.nrd.annonceservice.repository.CategorieRepository;
import com.nrd.annonceservice.repository.NotificationRepository;
import com.nrd.annonceservice.repository.SignalementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.nrd.annonceservice.enumeration.Statut;
import org.modelmapper.ModelMapper;
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
    private final NotificationRepository notificationRepository;
    private final ModelMapper modelMapper;
    private final UserClient userClient;

    public AnnonceResponseDto createAnnonce(Long userId, AnnonceCreateDto dto) {
        System.out.println("🔍 Recherche catégorie ID: " + dto.getCategorieId());
        
        Categorie categorie = categorieRepository.findById(dto.getCategorieId())
                .orElseThrow(() -> new CustomException("Catégorie non trouvée"));

        System.out.println("✅ Catégorie trouvée: " + categorie.getNom());

        if (annonceRepository.countByUserId(userId) == 0) {
            try {
                String response = userClient.becomeSeller();
                log.info("Promotion vendeur réussie pour user {} : {}", userId, response);
            } catch (Exception e) {
                log.warn("Erreur promotion vendeur pour user {} : {} - Continue quand même", userId, e.getMessage());
            }
        }

        Annonce annonce = new Annonce();
        annonce.setTitre(dto.getTitre());
        annonce.setDescription(dto.getDescription());
        annonce.setPrix(dto.getPrix());
        annonce.setVille(dto.getVille());
        annonce.setUserId(userId);
        annonce.setCategorie(categorie);
        annonce.setStatut(Statut.EN_ATTENTE);

        System.out.println("💾 Sauvegarde de l'annonce...");
        Annonce savedAnnonce = annonceRepository.save(annonce);
        System.out.println("✅ Annonce sauvée avec ID: " + savedAnnonce.getId());
        
        return mapToResponseDto(savedAnnonce);
    }

    public List<AnnonceResponseDto> getMyAnnoncesByStatut(Long userId, Statut statut) {
        if (statut != null) {
            return annonceRepository.findByUserIdAndStatut(userId, statut).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        } else {
            return annonceRepository.findByUserId(userId).stream()
                    .map(this::mapToResponseDto)
                    .collect(Collectors.toList());
        }
    }

    private AnnonceResponseDto mapToResponseDto(Annonce annonce) {
        AnnonceResponseDto dto = modelMapper.map(annonce, AnnonceResponseDto.class);
        dto.setCategorieNom(annonce.getCategorie().getNom());
        dto.setStatut(annonce.getStatut().name());
        dto.setNombreSignalements(annonce.getSignalements() != null ? annonce.getSignalements().size() : 0);
        return dto;
    }

    public List<AnnonceResponseDto> getPublicAnnonces() {
        return annonceRepository.findByStatut(Statut.EN_LIGNE).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public List<AnnonceResponseDto> getPendingAnnonces() {
        return annonceRepository.findByStatut(Statut.EN_ATTENTE).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public AnnonceResponseDto approveAnnonce(Long annonceId) {
        Annonce annonce = findAnnonceById(annonceId);
        annonce.setStatut(Statut.EN_LIGNE);
        annonce.setDatePublication(LocalDateTime.now());
        return mapToResponseDto(annonceRepository.save(annonce));
    }

    public AnnonceResponseDto rejectAnnonce(Long annonceId) {
        Annonce annonce = findAnnonceById(annonceId);
        annonce.setStatut(Statut.REJETE);
        return mapToResponseDto(annonceRepository.save(annonce));
    }

    public AnnonceResponseDto getAnnonceDetails(Long annonceId) {
        Annonce annonce = findAnnonceById(annonceId);
        
        if (annonce.getStatut() != Statut.EN_LIGNE) {
            throw new CustomException("Annonce non disponible");
        }
        
        annonce.setVues(annonce.getVues() + 1);
        annonceRepository.save(annonce);
        
        return mapToResponseDto(annonce);
    }

    public void supprimerAnnonce(Long userId, Long annonceId) {
        Annonce annonce = findAnnonceById(annonceId);
        checkOwnership(userId, annonce);
        annonceRepository.delete(annonce);
    }

    public List<AnnonceResponseDto> searchByKeywords(String keywords) {
        return annonceRepository.findByStatutAndTitreContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                Statut.EN_LIGNE, keywords).stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public void signalerAnnonce(Long userId, Long annonceId, String raison, String description) {
        Annonce annonce = findAnnonceById(annonceId);
        
        if (annonce.getUserId().equals(userId)) {
            throw new CustomException("Vous ne pouvez pas signaler votre propre annonce");
        }
        
        Signalement signalement = Signalement.builder()
                .annonceId(annonceId)
                .userId(userId)
                .raison(raison)
                .description(description != null ? description : "")
                .build();
        signalementRepository.save(signalement);
    }

    public AnnonceResponseDto modifyAnnonce(Long userId, Long annonceId, AnnonceCreateDto dto) {
        Annonce annonce = findAnnonceById(annonceId);
        checkOwnership(userId, annonce);
        
        if (annonce.getStatut() == Statut.EN_LIGNE) {
            annonce.setStatut(Statut.EN_ATTENTE);
        }
        
        modelMapper.map(dto, annonce);
        return mapToResponseDto(annonceRepository.save(annonce));
    }

    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    public Long getAnnonceOwnerId(Long annonceId) {
        Annonce annonce = findAnnonceById(annonceId);
        return annonce.getUserId();
    }

    public Object getUserStats(Long userId) {
        List<Annonce> userAnnonces = annonceRepository.findByUserId(userId);
        
        long totalAnnonces = userAnnonces.size();
        long annoncesEnLigne = userAnnonces.stream().filter(a -> a.getStatut() == Statut.EN_LIGNE).count();
        long annoncesEnAttente = userAnnonces.stream().filter(a -> a.getStatut() == Statut.EN_ATTENTE).count();
        int vuesTotal = userAnnonces.stream().mapToInt(Annonce::getVues).sum();
        
        return java.util.Map.of(
            "totalAnnonces", totalAnnonces,
            "annoncesEnLigne", annoncesEnLigne,
            "annoncesEnAttente", annoncesEnAttente,
            "vuesTotal", vuesTotal
        );
    }

    private Annonce findAnnonceById(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new CustomException("Annonce non trouvée"));
    }

    private void checkOwnership(Long userId, Annonce annonce) {
        if (!annonce.getUserId().equals(userId))
            throw new CustomException("Action non autorisée pour cet utilisateur.");
    }
}
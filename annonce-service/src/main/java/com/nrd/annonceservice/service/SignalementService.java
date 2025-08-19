package com.nrd.annonceservice.service;


import com.nrd.annonceservice.dto.SignalementResponseDto;
import com.nrd.annonceservice.entity.Signalement;
import com.nrd.annonceservice.repository.SignalementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SignalementService {

    private final SignalementRepository signalementRepository;

    public List<SignalementResponseDto> getAllSignalements() {
        return signalementRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<SignalementResponseDto> getSignalementsByAnnonce(Long annonceId) {
        return signalementRepository.findByAnnonceId(annonceId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private SignalementResponseDto mapToDto(Signalement s) {
        SignalementResponseDto dto = new SignalementResponseDto();
        dto.setId(s.getId());
        dto.setAnnonceId(s.getAnnonceId());
        dto.setUserId(s.getUserId());
        dto.setRaison(s.getRaison());
        dto.setDescription(s.getDescription());
        dto.setDateSignalement(s.getDateSignalement());
        return dto;
    }
}

package com.nrd.annonceservice.controller;

import com.nrd.annonceservice.dto.SignalementResponseDto;
import com.nrd.annonceservice.service.SignalementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signalements")
@RequiredArgsConstructor

public class SignalementController {

    private final SignalementService signalementService;

    @GetMapping
    public ResponseEntity<List<SignalementResponseDto>> getAllSignalements() {
        return ResponseEntity.ok(signalementService.getAllSignalements());
    }

    @GetMapping("/annonce/{annonceId}")
    public ResponseEntity<List<SignalementResponseDto>> getSignalementsByAnnonce(@PathVariable Long annonceId) {
        return ResponseEntity.ok(signalementService.getSignalementsByAnnonce(annonceId));
    }
}
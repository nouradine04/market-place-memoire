package com.nrd.annonceservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "annonces")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Annonce {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    private double prix;
    private String devise = "FCFA";

    private Long userId;

    @ManyToOne
    private Categorie categorie;

    @ElementCollection
    private List<String> images; // URLs

    private String ville;

    private LocalDateTime dateCreation = LocalDateTime.now();
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;

    @Enumerated(EnumType.STRING)
    private Statut statut = Statut.EN_ATTENTE; // valeur par défaut

    private int vues = 0;
    private int messages = 0;

    public enum TypePrix {
        NEGOCIABLE,FIXE
    }

    public enum Statut {
        EN_ATTENTE, EN_LIGNE, EXPIRE, REJETE
    }

    @PrePersist
    public void prePersist() {
        dateExpiration = dateCreation.plusDays(30);
    }
}

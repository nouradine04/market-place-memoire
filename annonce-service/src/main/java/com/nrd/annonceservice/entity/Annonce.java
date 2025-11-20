package com.nrd.annonceservice.entity;

import com.nrd.annonceservice.enumeration.Statut;
import com.nrd.annonceservice.enumeration.TypePrix;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
2
    private String ville;
    private LocalDateTime dateCreation = LocalDateTime.now();
    private LocalDateTime datePublication;
    private LocalDateTime dateExpiration;
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private  Statut statut = Statut.EN_ATTENTE;    // valeur par défaut
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private TypePrix typePrix = TypePrix.FIXE;
    private int vues = 0;
    private int messages = 0;
    @ElementCollection
    @Builder.Default
    private Set<Long> signalements = new HashSet<>(); // IDs des utilisateurs signalant


    @PrePersist
    public void prePersist() {
        dateExpiration = dateCreation.plusDays(30);
    }
}

package com.nrd.annonceservice.config;

import com.nrd.annonceservice.entity.Annonce;
import com.nrd.annonceservice.entity.Categorie;
import com.nrd.annonceservice.enumeration.Statut;
import com.nrd.annonceservice.enumeration.TypePrix;
import com.nrd.annonceservice.repository.AnnonceRepository;
import com.nrd.annonceservice.repository.CategorieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AnnonceRepository annonceRepository;
    private final CategorieRepository categorieRepository;
    private final Random random = new Random();

    // Villes du Tchad
    private final List<String> villes = Arrays.asList(
        "N'Djamena", "Moundou", "Sarh", "Abéché", "Kélo", "Koumra", "Pala", "Am Timan",
        "Bongor", "Mongo", "Doba", "Ati", "Oum Hadjer", "Bitkine", "Mao", "Massakory"
    );

    // Images réalistes par catégorie
    private final List<String> carImages = Arrays.asList(
        "https://images.unsplash.com/photo-1549317661-bd32c8ce0db2?w=400", // Toyota Hilux
        "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=400", // Voiture 4x4
        "https://images.unsplash.com/photo-1583121274602-3e2820c69888?w=400"  // Moto
    );
    
    private final List<String> houseImages = Arrays.asList(
        "https://images.unsplash.com/photo-1560518883-ce09059eeffa?w=400", // Maison moderne
        "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?w=400", // Appartement
        "https://images.unsplash.com/photo-1582268611958-ebfd161ef9cf?w=400"  // Villa
    );
    
    private final List<String> phoneImages = Arrays.asList(
        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400", // iPhone
        "https://images.unsplash.com/photo-1592750475338-74b7b21085ab?w=400", // Samsung
        "https://images.unsplash.com/photo-1574944985070-8f3ebc6b79d2?w=400"  // Téléphone
    );
    
    private final List<String> foodImages = Arrays.asList(
        "https://images.unsplash.com/photo-1586201375761-83865001e31c?w=400", // Céréales
        "https://images.unsplash.com/photo-1594736797933-d0401ba2fe65?w=400", // Dattes
        "https://images.unsplash.com/photo-1548550023-2bdb3c5beed7?w=400"  // Poulet
    );
    
    private final List<String> serviceImages = Arrays.asList(
        "https://images.unsplash.com/photo-1571019613454-1cb2f99b2d8b?w=400", // Cours
        "https://images.unsplash.com/photo-1581092918056-0c4c3acd3789?w=400", // Réparation
        "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"  // Service
    );

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n\n=== DEBUT SEEDER COMPLET ===");
        try {
            System.out.println("🌱 Nettoyage des anciennes données...");
            annonceRepository.deleteAll();
            categorieRepository.deleteAll();
            System.out.println("🗑️ Toutes les annonces et catégories supprimées");

            System.out.println("🌱 Création des catégories...");
            seedCategories();
            
            System.out.println("🌱 Création des annonces tchadiennes...");
            seedAnnonces();
            System.out.println("✅ Seeding terminé !");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du seeding: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("=== FIN SEEDER COMPLET ===\n\n");
    }

    private void seedAnnonces() {
        log.info("📋 Récupération des catégories...");
        // Récupérer les catégories existantes
        List<Categorie> categories = categorieRepository.findAll();
        log.info("📊 Nombre de catégories trouvées: {}", categories.size());
        
        if (categories.isEmpty()) {
            System.out.println("⚠️ Aucune catégorie trouvée - Erreur dans le seeding");
            return;
        }

        // Données d'annonces tchadiennes
        String[][] annonceData = {
            {"Vente Toyota Hilux 2018", "Véhicule en excellent état, climatisé, 4x4. Idéal pour les routes tchadiennes.", "15000000", "FCFA"},
            {"Appartement 3 pièces Chagoua", "Bel appartement meublé dans le quartier Chagoua, proche des commodités.", "180000", "FCFA"},
            {"Cours particuliers Mathématiques", "Professeur expérimenté donne cours de maths niveau lycée et université.", "15000", "FCFA"},
            {"Vente moutons Tabaski", "Beaux moutons pour la fête de Tabaski, différentes tailles disponibles.", "150000", "FCFA"},
            {"Réparation climatiseurs", "Service de réparation et maintenance de climatiseurs à domicile.", "25000", "FCFA"},
            {"Vente mil et sorgho", "Céréales de qualité, production locale. Livraison possible.", "800", "FCFA"},
            {"Moto Yamaha 125cc", "Moto en bon état, économique, parfaite pour la ville.", "650000", "FCFA"},
            {"Terrain à vendre Walia", "Terrain de 500m² dans le quartier Walia, titre foncier disponible.", "8000000", "FCFA"},
            {"Cours d'arabe littéraire", "Enseignant qualifié propose cours d'arabe pour tous niveaux.", "20000", "FCFA"},
            {"Vente poulets de chair", "Poulets élevés localement, chair tendre, prix de gros disponible.", "3500", "FCFA"},
            {"Réparation téléphones", "Réparation de tous types de smartphones, pièces d'origine.", "15000", "FCFA"},
            {"Maison à louer Moursal", "Belle villa 4 chambres avec jardin dans le quartier Moursal.", "250000", "FCFA"},
            {"Vente dattes Borkou", "Dattes fraîches du Borkou, qualité premium, conditionnement soigné.", "2500", "FCFA"},
            {"Cours de conduite", "Auto-école agréée, formation complète permis B, véhicule récent.", "85000", "FCFA"},
            {"Vente bétail Kanem", "Bovins et caprins du Kanem, animaux en bonne santé.", "450000", "FCFA"}
        };

        for (int i = 0; i < annonceData.length; i++) {
            String[] data = annonceData[i];
            
            Annonce annonce = new Annonce();
            annonce.setTitre(data[0]);
            annonce.setDescription(data[1]);
            annonce.setPrix(Double.parseDouble(data[2]));
            annonce.setDevise(data[3]);
            annonce.setTypePrix(TypePrix.FIXE);
            annonce.setVille(villes.get(random.nextInt(villes.size())));
            annonce.setUserId((long) (random.nextInt(5) + 1)); // Users 1-5
            annonce.setCategorie(categories.get(random.nextInt(categories.size())));
            annonce.setStatut(Statut.EN_ATTENTE); // En attente de validation admin
            annonce.setDateCreation(LocalDateTime.now().minusDays(random.nextInt(30)));
            annonce.setDatePublication(LocalDateTime.now().minusDays(random.nextInt(15)));
            annonce.setDateExpiration(LocalDateTime.now().plusDays(30));
            annonce.setVues(random.nextInt(100));
            annonce.setMessages(random.nextInt(10));
            
            // Ajouter des images correspondant au type d'annonce
            List<String> annonceImages;
            String titre = data[0].toLowerCase();
            
            if (titre.contains("toyota") || titre.contains("moto") || titre.contains("hilux")) {
                annonceImages = Arrays.asList(carImages.get(random.nextInt(carImages.size())));
            } else if (titre.contains("appartement") || titre.contains("maison") || titre.contains("terrain")) {
                annonceImages = Arrays.asList(houseImages.get(random.nextInt(houseImages.size())));
            } else if (titre.contains("téléphone") || titre.contains("réparation téléphones")) {
                annonceImages = Arrays.asList(phoneImages.get(random.nextInt(phoneImages.size())));
            } else if (titre.contains("mil") || titre.contains("dattes") || titre.contains("poulets")) {
                annonceImages = Arrays.asList(foodImages.get(random.nextInt(foodImages.size())));
            } else {
                annonceImages = Arrays.asList(serviceImages.get(random.nextInt(serviceImages.size())));
            }
            
            annonce.setImages(annonceImages);
            
            annonceRepository.save(annonce);
            System.out.println("📝 Annonce créée: " + data[0] + " (Statut: " + annonce.getStatut() + ")");
        }
    }
    
    private void seedCategories() {
        System.out.println("📋 Création des catégories...");
        
        String[] categorieNames = {"Véhicules", "Téléphones", "Immobilier", "Alimentation", "Services", "Autre"};
        
        for (String nom : categorieNames) {
            Categorie categorie = new Categorie();
            categorie.setNom(nom);
            categorieRepository.save(categorie);
            System.out.println("🏷️ Catégorie créée: " + nom);
        }
    }
}
package com.nrd.userservice.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nrd.userservice.dto.AuthResponseDto;
import com.nrd.userservice.dto.UserProfileUpdateDto;
import com.nrd.userservice.dto.UserRegistrationDto;
import com.nrd.userservice.dto.UserResponseDto;
import com.nrd.userservice.entity.User;
import com.nrd.userservice.exception.CustomException;
import com.nrd.userservice.repository.UserRepository;
import com.nrd.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    @Autowired
    private final JwtUtil jwtUtil;
    @Autowired
    private final FirebaseAuth firebaseAuth;

    //  Inscription
    public AuthResponseDto register(UserRegistrationDto dto) {
        log.info("📝 Tentative d'inscription: email={}, telephone={}, nom={}, prenom={}", 
            dto.getEmail(), dto.getTelephone(), dto.getNom(), dto.getPrenom());
            
        if ((dto.getTelephone() == null || dto.getTelephone().isEmpty()) &&
                (dto.getEmail() == null || dto.getEmail().isEmpty())) {
            log.error("❌ Erreur: Téléphone ou email requis");
            throw new CustomException("Téléphone ou email requis");
        }
        
        // Vérifier si l'email existe déjà en base locale AVANT Firebase
        if (dto.getEmail() != null && userRepository.existsByEmail(dto.getEmail())) {
            log.error("❌ Email déjà utilisé en base locale: {}", dto.getEmail());
            throw new CustomException("Cette adresse email est déjà utilisée");
        }

        try {
            log.info("🔥 Création utilisateur Firebase...");
            
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(dto.getEmail())
                    .setPhoneNumber(dto.getTelephone() != null ? "+235" + dto.getTelephone().replaceAll("[^0-9]", "") : null)
                    .setPassword(dto.getPassword())
                    .setDisplayName(dto.getPrenom() + " " + dto.getNom());

            log.info("📦 Requête Firebase prête: email={}, phone={}", 
                dto.getEmail(), dto.getTelephone() != null ? "+235" + dto.getTelephone().replaceAll("[^0-9]", "") : null);
                
            UserRecord userRecord = firebaseAuth.createUser(request);
            log.info("✅ Utilisateur Firebase créé: {}", userRecord.getUid());

                      User user = User.builder()
                    .firebaseUid(userRecord.getUid())
                    .email(dto.getEmail())
                    .telephone(dto.getTelephone())
                    .prenom(dto.getPrenom())
                    .nom(dto.getNom())
                    .password(passwordEncoder.encode(dto.getPassword()))
                    .socialNetworks(dto.getSocialNetworks() != null ? dto.getSocialNetworks() : Map.of())
                    .vendeur(false)
                    .role(User.Role.USER)
                    .build();

               user = userRepository.save(user);

            String token = jwtUtil.generateToken(user.getId() ,  user.getRole().name());


                 return new AuthResponseDto(mapToResponseDto(user), token);

        } catch (FirebaseAuthException e) {
            log.error("❌ Erreur Firebase: code={}, message={}", e.getErrorCode(), e.getMessage());
            
            // Messages d'erreur plus clairs
            String errorMessage;
            if ("EMAIL_ALREADY_EXISTS".equals(e.getErrorCode()) || "ALREADY_EXISTS".equals(e.getErrorCode())) {
                errorMessage = "Cette adresse email est déjà utilisée";
            } else if ("PHONE_NUMBER_ALREADY_EXISTS".equals(e.getErrorCode())) {
                errorMessage = "Ce numéro de téléphone est déjà utilisé";
            } else if ("WEAK_PASSWORD".equals(e.getErrorCode())) {
                errorMessage = "Le mot de passe doit contenir au moins 6 caractères";
            } else if ("INVALID_EMAIL".equals(e.getErrorCode())) {
                errorMessage = "Adresse email invalide";
            } else if ("INVALID_PHONE_NUMBER".equals(e.getErrorCode())) {
                errorMessage = "Numéro de téléphone invalide";
            } else {
                errorMessage = "Erreur lors de l'inscription: " + e.getMessage();
            }
            
            throw new CustomException(errorMessage);
        } catch (Exception e) {
            log.error("❌ Erreur générale lors de l'inscription: {}", e.getMessage(), e);
            throw new CustomException("Erreur lors de l'inscription");
        }
    }

    public String login(String identifier, String password) throws CustomException {
        log.info("🔐 Tentative de connexion pour: {}", identifier);
        
        // Vérifier d'abord en base locale (pour admin)
        User localUser = userRepository.findByEmailOrTelephone(identifier, identifier).orElse(null);
        if (localUser != null) {
            log.info("👤 Utilisateur trouvé en base locale: {} (Role: {})", localUser.getEmail(), localUser.getRole());
            
            if (localUser.isEstBloque()) {
                log.warn("❌ Compte bloqué: {}", identifier);
                throw new CustomException("Compte bloqué");
            }
            
            if (!passwordEncoder.matches(password, localUser.getPassword())) {
                log.warn("❌ Mot de passe incorrect pour: {}", identifier);
                throw new CustomException("Mot de passe incorrect");
            }
            
            log.info("✅ Connexion réussie pour: {} (ID: {})", localUser.getEmail(), localUser.getId());
            return jwtUtil.generateToken(localUser.getId(), localUser.getRole().name());
        }
        
        // Sinon essayer Firebase
        try {
            log.info("🔥 Tentative connexion Firebase pour: {}", identifier);
            UserRecord userRecord = identifier.contains("@") ?
                    firebaseAuth.getUserByEmail(identifier) :
                    firebaseAuth.getUserByPhoneNumber("+235" + identifier.replaceAll("[^0-9]", ""));

            User user = userRepository.findByFirebaseUid(userRecord.getUid())
                    .orElseThrow(() -> {
                        log.warn("❌ Utilisateur Firebase non trouvé en base: {}", identifier);
                        return new CustomException("Utilisateur non trouvé");
                    });

            if (!passwordEncoder.matches(password, user.getPassword())) {
                log.warn("❌ Mot de passe Firebase incorrect pour: {}", identifier);
                throw new CustomException("Mot de passe incorrect");
            }

            log.info("✅ Connexion Firebase réussie pour: {} (ID: {})", user.getEmail(), user.getId());
            return jwtUtil.generateToken(user.getId(), user.getRole().name());

        } catch (FirebaseAuthException e) {
            log.error("❌ Erreur Firebase pour {}: {}", identifier, e.getMessage());
            throw new CustomException("Utilisateur non trouvé ou mot de passe incorrect");
        }
    }

    public UserResponseDto updateProfile(Long id, UserProfileUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));

        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());
        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getAvatar() != null) user.setAvatar(dto.getAvatar());
        if (dto.getSocialNetworks() != null) user.setSocialNetworks(dto.getSocialNetworks());

        return mapToResponseDto(userRepository.save(user));
    }

    // -------------------- Tous les utilisateurs --------------------
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setPrenom(user.getPrenom());
        dto.setNom(user.getNom());
        dto.setTelephone(user.getTelephone());
        dto.setAvatar(user.getAvatar());
        dto.setSocialNetworks(user.isVendeur() ? user.getSocialNetworks() : null);
        dto.setVendeur(user.isVendeur());
        return dto;
    }

    public void blockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));

        if (user.getRole() == User.Role.ADMIN) {
            throw new CustomException("Impossible de bloquer un admin");
        }

        user.setEstBloque(true);
        userRepository.save(user);
    }
    
    public void unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));
        user.setEstBloque(false);
        userRepository.save(user);
    }

    public void promoteToAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));

        user.setRole(User.Role.ADMIN);
        userRepository.save(user);
    }

    public long getTotalUsers() {
        return userRepository.count();
    }

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);
        dto.setSocialNetworks(filterProvidedSocial(user.getSocialNetworks()));
        return dto;
    }


    public void becomeSeller(Long userId) {
        log.info("Tentative de promotion de l'utilisateur {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));
        if (user.isVendeur()) {
            log.warn("Utilisateur {} déjà vendeur", userId);
            return;
        }
        user.setVendeur(true);
        userRepository.save(user);
        log.info("Utilisateur {} promu vendeur avec succès", userId);
    }

    public String getUserName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));
        return user.getPrenom() + " " + user.getNom();
    }

    private Map<String, String> filterProvidedSocial(Map<String, String> social) {
        return social.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    public String authenticateWithGoogle(String idToken) {
        try {
            // Vérifier le token Firebase
            com.google.firebase.auth.FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String email = decodedToken.getEmail();
            String name = decodedToken.getName();
            String uid = decodedToken.getUid();
            
            log.info("🔥 Connexion OAuth Google: {} ({})", email, name);
            
            // Chercher l'utilisateur existant
            User user = userRepository.findByEmailOrFirebaseUid(email, uid).orElse(null);
            
            if (user == null) {
                // Créer un nouvel utilisateur
                String[] nameParts = name != null ? name.split(" ", 2) : new String[]{"Utilisateur", "Google"};
                
                user = User.builder()
                    .firebaseUid(uid)
                    .email(email)
                    .prenom(nameParts[0])
                    .nom(nameParts.length > 1 ? nameParts[1] : "")
                    .password(passwordEncoder.encode("oauth_" + uid)) // Mot de passe temporaire
                    .socialNetworks(Map.of())
                    .vendeur(false)
                    .role(User.Role.USER)
                    .build();
                
                user = userRepository.save(user);
                log.info("✅ Nouvel utilisateur OAuth créé: {} (ID: {})", email, user.getId());
            } else {
                log.info("👤 Utilisateur OAuth existant: {} (ID: {})", email, user.getId());
            }
            
            if (user.isEstBloque()) {
                throw new CustomException("Compte bloqué");
            }
            
            return jwtUtil.generateToken(user.getId(), user.getRole().name());
            
        } catch (com.google.firebase.auth.FirebaseAuthException e) {
            log.error("❌ Erreur vérification token OAuth: {}", e.getMessage());
            throw new CustomException("Token OAuth invalide");
        }
    }
}
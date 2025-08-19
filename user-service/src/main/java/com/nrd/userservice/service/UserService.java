package com.nrd.userservice.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import com.nrd.userservice.dto.UserLoginDto;
import com.nrd.userservice.dto.UserProfileUpdateDto;
import com.nrd.userservice.dto.UserRegistrationDto;
import com.nrd.userservice.dto.UserResponseDto;
import com.nrd.userservice.entity.User;
import com.nrd.userservice.exception.CustomException;
import com.nrd.userservice.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    @Value("${jwt.secret}") private String jwtSecret;
    @Value("${jwt.expiration}") private long jwtExpiration;

    // Initialisation Firebase (dans @Configuration ou static)
    private static final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    public UserResponseDto register(UserRegistrationDto dto) {
        // Validation : phone ou email requis
        if ((dto.getTelephone() == null || dto.getTelephone().isEmpty()) &&
                (dto.getEmail() == null || dto.getEmail().isEmpty())) {
            throw new CustomException("Téléphone ou email requis");
        }

        // Créer utilisateur Firebase
        UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                .setEmail(dto.getEmail())
                .setPhoneNumber(dto.getTelephone() != null ? "+237" + dto.getTelephone() : null) // Pays ex. Cameroun
                .setPassword(dto.getPassword())
                .setDisplayName(dto.getPrenom() + " " + dto.getNom());

        try {
            UserRecord userRecord = firebaseAuth.createUser(request);
            User user = modelMapper.map(dto, User.class);
            user.setFirebaseUid(userRecord.getUid());
            user.setSocialNetworks(dto.getSocialNetworks() != null ? dto.getSocialNetworks() : new HashMap<>());
            user = userRepository.save(user);

            // Générer JWT
            String token = generateToken(user.getId());
            user.setPassword(null); // Ne pas exposer
            UserResponseDto response = mapToResponseDto(user);
            response.setToken(token);
            return response;
        } catch (FirebaseAuthException e) {
            throw new CustomException("Erreur Firebase : " + e.getMessage());
        }
    }

    public UserResponseDto login(UserLoginDto dto) {
        // Vérif via Firebase (email ou phone)
        try {
            UserRecord userRecord = firebaseAuth.getUserByEmail(dto.getIdentifier()); // Ou phone
            User user = userRepository.findByFirebaseUid(userRecord.getUid())
                    .orElseThrow(() -> new CustomException("Utilisateur non trouvé"));
            if (!user.getPassword().equals(dto.getPassword())) { // Simplifié, à hacher en prod
                throw new CustomException("Mot de passe incorrect");
            }

            String token = generateToken(user.getId());
            UserResponseDto response = mapToResponseDto(user);
            response.setToken(token);
            return response;
        } catch (FirebaseAuthException e) {
            throw new CustomException("Erreur login : " + e.getMessage());
        }
    }

    public void upgradeToVendeur(Long userId, UserProfileUpdateDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User non trouvé"));
        if (!user.isIsVendeur()) {
            if (dto.getSocialNetworks() == null || dto.getSocialNetworks().isEmpty()) {
                throw new CustomException("Réseaux requis pour première annonce");
            }
            user.setSocialNetworks(dto.getSocialNetworks());
            user.setIsVendeur(true);
            userRepository.save(user);
        }
    }

    private String generateToken(Long userId) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    private UserResponseDto mapToResponseDto(User user) {
        UserResponseDto dto = modelMapper.map(user, UserResponseDto.class);
        dto.setSocialNetworks(filterProvidedSocial(user.getSocialNetworks()));
        return dto;
    }

    private Map<String, String> filterProvidedSocial(Map<String, String> social) {
        return social.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
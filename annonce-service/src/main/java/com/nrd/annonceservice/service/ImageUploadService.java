package com.nrd.annonceservice.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageUploadService {

    private final Cloudinary cloudinary;

    public List<String> uploadImages(List<MultipartFile> images, Long userId) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();

        if (images == null || images.isEmpty()) {
            return uploadedUrls;
        }

        for (MultipartFile image : images) {
            if (image != null && !image.isEmpty()) {
                String url = uploadImage(image, userId);
                if (url != null) {
                    uploadedUrls.add(url);
                }
            }
        }

        return uploadedUrls;
    }

    public String uploadImage(MultipartFile image, Long userId) throws IOException {
        try {
            log.info("📤 Upload image pour user {}: {} ({} bytes)", userId, image.getOriginalFilename(), image.getSize());

            // Créer le dossier pour l'utilisateur
            String folder = "annonces/user_" + userId;

            Map<String, Object> uploadParams = ObjectUtils.asMap(
                "folder", folder,
                "resource_type", "image",
                "allowed_formats", new String[]{"jpg", "jpeg", "png", "gif", "webp"}
            );

            Map<String, Object> uploadResult = cloudinary.uploader().upload(image.getBytes(), uploadParams);

            String imageUrl = (String) uploadResult.get("secure_url");
            log.info("✅ Image uploadée: {}", imageUrl);

            return imageUrl;

        } catch (Exception e) {
            log.error("❌ Erreur upload image pour user {}: {}", userId, e.getMessage());
            throw new IOException("Erreur lors de l'upload de l'image: " + e.getMessage(), e);
        }
    }

    public List<String> validateImageUrls(List<String> imageUrls) {
        // Valider que les URLs sont bien des URLs Cloudinary valides
        return imageUrls.stream()
                .filter(url -> url != null && url.contains("cloudinary.com"))
                .toList();
    }
    
    public void deleteImage(String imageUrl) {
        try {
            // Extraire le public_id de l'URL Cloudinary
            String publicId = extractPublicIdFromUrl(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Image supprimée: {}", publicId);
        } catch (Exception e) {
            log.error("Erreur suppression image {}: {}", imageUrl, e.getMessage());
        }
    }
    
    private String extractPublicIdFromUrl(String imageUrl) {
        // Exemple: https://res.cloudinary.com/demo/image/upload/v1234567890/annonces/user_1/abc123.jpg
        // Retourne: annonces/user_1/abc123
        String[] parts = imageUrl.split("/");
        int uploadIndex = -1;
        for (int i = 0; i < parts.length; i++) {
            if ("upload".equals(parts[i])) {
                uploadIndex = i;
                break;
            }
        }
        if (uploadIndex != -1 && uploadIndex + 2 < parts.length) {
            String pathWithExtension = String.join("/", java.util.Arrays.copyOfRange(parts, uploadIndex + 2, parts.length));
            // Enlever l'extension
            int dotIndex = pathWithExtension.lastIndexOf('.');
            return dotIndex > 0 ? pathWithExtension.substring(0, dotIndex) : pathWithExtension;
        }
        return imageUrl;
    }
}
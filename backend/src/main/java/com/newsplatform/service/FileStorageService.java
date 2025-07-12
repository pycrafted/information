package com.newsplatform.service;

import com.newsplatform.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

/**
 * Service pour la gestion du stockage de fichiers.
 * Couche Service : Gestion des uploads et stockage de fichiers
 * 
 * Responsabilités :
 * - Upload de fichiers
 * - Validation des types de fichiers
 * - Gestion du stockage local
 * - Nettoyage des fichiers temporaires
 * 
 * @author Équipe Développement
 * @version 1.0
 */
@Service
public class FileStorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    @Value("${file.max.size:10485760}") // 10MB par défaut
    private long maxFileSize;
    
    /**
     * Stocke un fichier uploadé.
     * 
     * @param file fichier à stocker
     * @return nom du fichier stocké
     * @throws BusinessException si l'upload échoue
     */
    public String storeFile(MultipartFile file) {
        try {
            validateFile(file);
            
            // Créer le répertoire s'il n'existe pas
            Path uploadDir = Paths.get(uploadPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            // Générer un nom de fichier unique
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + fileExtension;
            
            // Stocker le fichier
            Path filePath = uploadDir.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            logger.info("Fichier stocké avec succès: {}", uniqueFilename);
            return uniqueFilename;
            
        } catch (IOException e) {
            logger.error("Erreur lors du stockage du fichier", e);
            throw new BusinessException("Impossible de stocker le fichier", e);
        }
    }
    
    /**
     * Valide un fichier uploadé.
     * 
     * @param file fichier à valider
     * @throws BusinessException si le fichier est invalide
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Le fichier ne peut pas être vide");
        }
        
        if (file.getSize() > maxFileSize) {
            throw new BusinessException("Le fichier est trop volumineux. Taille maximale: " + (maxFileSize / 1024 / 1024) + "MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new BusinessException("Type de fichier non autorisé");
        }
    }
    
    /**
     * Vérifie si le type de contenu est autorisé.
     * 
     * @param contentType type de contenu
     * @return true si autorisé
     */
    private boolean isAllowedContentType(String contentType) {
        return contentType.startsWith("image/") || 
               contentType.startsWith("application/pdf") ||
               contentType.startsWith("text/");
    }
    
    /**
     * Extrait l'extension d'un nom de fichier.
     * 
     * @param filename nom du fichier
     * @return extension avec le point
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
    
    /**
     * Supprime un fichier stocké.
     * 
     * @param filename nom du fichier à supprimer
     * @return true si supprimé avec succès
     */
    public boolean deleteFile(String filename) {
        try {
            Path filePath = Paths.get(uploadPath, filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("Fichier supprimé avec succès: {}", filename);
                return true;
            }
            return false;
        } catch (IOException e) {
            logger.error("Erreur lors de la suppression du fichier: {}", filename, e);
            return false;
        }
    }
    
    /**
     * Vérifie si un fichier existe.
     * 
     * @param filename nom du fichier
     * @return true si le fichier existe
     */
    public boolean fileExists(String filename) {
        Path filePath = Paths.get(uploadPath, filename);
        return Files.exists(filePath);
    }
    
    /**
     * Obtient le chemin complet d'un fichier.
     * 
     * @param filename nom du fichier
     * @return chemin complet
     */
    public Path getFilePath(String filename) {
        return Paths.get(uploadPath, filename);
    }
}
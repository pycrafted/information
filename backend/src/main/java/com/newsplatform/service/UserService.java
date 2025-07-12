package com.newsplatform.service;

import com.newsplatform.entity.User;
import com.newsplatform.exception.BusinessException;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service pour la gestion des utilisateurs selon les principes DDD.
 * Couche Service : Orchestration de la logique métier utilisateur
 * 
 * Responsabilités :
 * - Gestion du cycle de vie des utilisateurs
 * - Validation des règles métier utilisateur
 * - Gestion des rôles et permissions
 * - Sécurité et chiffrement des mots de passe
 * 
 * @author Équipe Développement
 * @version 1.0 - Implémentation complète
 */
@Service
@Transactional
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===============================================
    // OPÉRATIONS DE CRÉATION
    // ===============================================
    
    /**
     * Crée un nouvel utilisateur avec validation métier.
     * 
     * @param email email de l'utilisateur
     * @param password mot de passe en clair
     * @param firstName prénom
     * @param lastName nom
     * @param role rôle de l'utilisateur
     * @return utilisateur créé
     * @throws ValidationException si les données sont invalides
     */
    @Transactional
    public User createUser(String email, String password, String firstName, String lastName, User.UserRole role) {
        // Validation des paramètres
        validateUserCreationParams(email, password, firstName, lastName, role);
        
        // Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(email)) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà : " + email);
        }
        
        try {
            // Chiffrement du mot de passe
            String encodedPassword = passwordEncoder.encode(password);
            
            // Création de l'utilisateur
            User user = new User(email, email, encodedPassword, role);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            User savedUser = userRepository.save(user);
            
            logger.info("Utilisateur créé avec succès - Email: {}, Rôle: {}", email, role);
            return savedUser;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de l'utilisateur", e);
            throw new BusinessException("Impossible de créer l'utilisateur", e);
        }
    }
    
    /**
     * Crée un utilisateur avec des paramètres par défaut.
     * 
     * @param email email de l'utilisateur
     * @param password mot de passe en clair
     * @param firstName prénom
     * @param lastName nom
     * @return utilisateur créé avec rôle VISITEUR par défaut
     */
    @Transactional
    public User createDefaultUser(String email, String password, String firstName, String lastName) {
        return createUser(email, password, firstName, lastName, User.UserRole.VISITEUR);
    }

    // ===============================================
    // OPÉRATIONS DE LECTURE
    // ===============================================
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param id ID de l'utilisateur
     * @return utilisateur trouvé
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
    }
    
    /**
     * Récupère un utilisateur par son email.
     * 
     * @param email email de l'utilisateur
     * @return utilisateur trouvé
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }
    
    /**
     * Récupère un utilisateur par son email (optionnel).
     * 
     * @param email email de l'utilisateur
     * @return Optional contenant l'utilisateur s'il existe
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Récupère tous les utilisateurs avec pagination.
     * 
     * @param pageable configuration de pagination
     * @return page d'utilisateurs
     */
    @Transactional(readOnly = true)
    public Page<User> getAllUsers(Pageable pageable) {
        try {
            return userRepository.findAll(pageable);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des utilisateurs", e);
            throw new BusinessException("Impossible de récupérer les utilisateurs", e);
        }
    }
    
    /**
     * Récupère les utilisateurs par rôle.
     * 
     * @param role rôle recherché
     * @param pageable configuration de pagination
     * @return page d'utilisateurs avec le rôle donné
     */
    @Transactional(readOnly = true)
    public Page<User> getUsersByRole(User.UserRole role, Pageable pageable) {
        try {
            return userRepository.findByRole(role, pageable);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des utilisateurs par rôle", e);
            throw new BusinessException("Impossible de récupérer les utilisateurs par rôle", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE MISE À JOUR
    // ===============================================
    
    /**
     * Met à jour les informations d'un utilisateur.
     * 
     * @param id ID de l'utilisateur
     * @param firstName nouveau prénom
     * @param lastName nouveau nom
     * @return utilisateur mis à jour
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public User updateUserInfo(UUID id, String firstName, String lastName) {
        User user = getUserById(id);
        
        // Validation des paramètres
        validateUserInfoParams(firstName, lastName);
        
        try {
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            logger.info("Informations utilisateur mises à jour - ID: {}", id);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour des informations utilisateur", e);
            throw new BusinessException("Impossible de mettre à jour les informations utilisateur", e);
        }
    }
    
    /**
     * Change le mot de passe d'un utilisateur.
     * 
     * @param id ID de l'utilisateur
     * @param newPassword nouveau mot de passe en clair
     * @return utilisateur mis à jour
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public User changePassword(UUID id, String newPassword) {
        User user = getUserById(id);
        
        // Validation du mot de passe
        validatePassword(newPassword);
        
        try {
            String encodedPassword = passwordEncoder.encode(newPassword);
            user.setPassword(encodedPassword);
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            logger.info("Mot de passe utilisateur changé - ID: {}", id);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Erreur lors du changement de mot de passe", e);
            throw new BusinessException("Impossible de changer le mot de passe", e);
        }
    }
    
    /**
     * Change le rôle d'un utilisateur.
     * 
     * @param id ID de l'utilisateur
     * @param newRole nouveau rôle
     * @return utilisateur mis à jour
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public User changeUserRole(UUID id, User.UserRole newRole) {
        User user = getUserById(id);
        
        // Validation du rôle
        if (newRole == null) {
            throw new ValidationException("Le rôle ne peut pas être null");
        }
        
        try {
            user.setRole(newRole);
            user.setUpdatedAt(LocalDateTime.now());
            
            User updatedUser = userRepository.save(user);
            logger.info("Rôle utilisateur changé - ID: {}, Nouveau rôle: {}", id, newRole);
            return updatedUser;
        } catch (Exception e) {
            logger.error("Erreur lors du changement de rôle utilisateur", e);
            throw new BusinessException("Impossible de changer le rôle utilisateur", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE SUPPRESSION
    // ===============================================
    
    /**
     * Supprime un utilisateur.
     * 
     * @param id ID de l'utilisateur à supprimer
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional
    public void deleteUser(UUID id) {
        User user = getUserById(id);
        
        try {
            userRepository.delete(user);
            logger.info("Utilisateur supprimé - ID: {}", id);
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'utilisateur", e);
            throw new BusinessException("Impossible de supprimer l'utilisateur", e);
        }
    }

    // ===============================================
    // OPÉRATIONS DE VALIDATION
    // ===============================================
    
    /**
     * Vérifie si un email existe déjà.
     * 
     * @param email email à vérifier
     * @return true si l'email existe
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Valide les identifiants d'authentification.
     * 
     * @param email email de l'utilisateur
     * @param password mot de passe en clair
     * @return utilisateur si les identifiants sont valides
     * @throws ValidationException si les identifiants sont invalides
     */
    @Transactional(readOnly = true)
    public User validateCredentials(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            throw new ValidationException("Email ou mot de passe incorrect");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ValidationException("Email ou mot de passe incorrect");
        }
        
        return user;
    }

    // ===============================================
    // MÉTHODES DE VALIDATION PRIVÉES
    // ===============================================
    
    private void validateUserCreationParams(String email, String password, String firstName, 
                                          String lastName, User.UserRole role) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("L'email ne peut pas être vide");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ValidationException("Format d'email invalide");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Le mot de passe ne peut pas être vide");
        }
        if (password.length() < 6) {
            throw new ValidationException("Le mot de passe doit contenir au moins 6 caractères");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("Le prénom ne peut pas être vide");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Le nom ne peut pas être vide");
        }
        if (role == null) {
            throw new ValidationException("Le rôle ne peut pas être null");
        }
    }
    
    private void validateUserInfoParams(String firstName, String lastName) {
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new ValidationException("Le prénom ne peut pas être vide");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new ValidationException("Le nom ne peut pas être vide");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new ValidationException("Le mot de passe ne peut pas être vide");
        }
        if (password.length() < 6) {
            throw new ValidationException("Le mot de passe doit contenir au moins 6 caractères");
        }
    }
}
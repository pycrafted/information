package com.newsplatform.service;

import com.newsplatform.entity.User;
import com.newsplatform.exception.ResourceNotFoundException;
import com.newsplatform.exception.ValidationException;
import com.newsplatform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Service SOAP pour la gestion des utilisateurs.
 * Couche Service : CRUD complet utilisateurs selon cahier des charges.
 * 
 * Fonctionnalités :
 * - Lister tous les utilisateurs
 * - Ajouter un nouvel utilisateur  
 * - Modifier un utilisateur existant
 * - Supprimer un utilisateur
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
@Service
@Transactional
public class UserSoapService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public UserSoapService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Liste tous les utilisateurs du système.
     * Opération réservée aux ADMINISTRATEURS.
     * 
     * @return liste de tous les utilisateurs
     */
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Récupère un utilisateur par son ID.
     * 
     * @param userId ID de l'utilisateur
     * @return utilisateur trouvé
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    @Transactional(readOnly = true)
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID: " + userId));
    }
    
    /**
     * Ajoute un nouvel utilisateur au système.
     * Opération réservée aux ADMINISTRATEURS.
     * 
     * @param username nom d'utilisateur (unique)
     * @param password mot de passe en clair (sera chiffré)
     * @param email adresse email (unique)
     * @param firstName prénom
     * @param lastName nom de famille
     * @param role rôle (VISITEUR, EDITEUR, ADMINISTRATEUR)
     * @return utilisateur créé
     * @throws ValidationException si les données sont invalides
     */
    public User addUser(String username, String password, String email, 
                       String firstName, String lastName, String role) {
        // Validation des paramètres
        validateUsername(username);
        validatePassword(password);
        validateEmail(email);
        validateRole(role);
        
        // Vérifier l'unicité du nom d'utilisateur
        if (userRepository.existsByUsername(username.trim())) {
            throw new ValidationException("Un utilisateur avec ce nom d'utilisateur existe déjà: " + username);
        }
        
        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(email.trim().toLowerCase())) {
            throw new ValidationException("Un utilisateur avec cet email existe déjà: " + email);
        }
        
        // Créer le nouvel utilisateur
        User newUser = new User();
        newUser.setUsername(username.trim());
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setEmail(email.trim().toLowerCase());
        newUser.setFirstName(firstName != null ? firstName.trim() : null);
        newUser.setLastName(lastName != null ? lastName.trim() : null);
        newUser.setRole(User.UserRole.valueOf(role.toUpperCase()));
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }
    
    /**
     * Met à jour un utilisateur existant.
     * Opération réservée aux ADMINISTRATEURS.
     * 
     * @param userId ID de l'utilisateur à modifier
     * @param email nouvel email (optionnel)
     * @param firstName nouveau prénom (optionnel)
     * @param lastName nouveau nom de famille (optionnel)
     * @param active nouveau statut actif/inactif (optionnel)
     * @return utilisateur modifié
     * @throws ResourceNotFoundException si l'utilisateur n'existe pas
     */
    public User updateUser(UUID userId, String email, String firstName, String lastName, Boolean active) {
        User existingUser = getUserById(userId);
        
        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
            existingUser.setEmail(email.trim().toLowerCase());
        }
        
        if (firstName != null && !firstName.trim().isEmpty()) {
            existingUser.setFirstName(firstName.trim());
        }
        
        if (lastName != null && !lastName.trim().isEmpty()) {
            existingUser.setLastName(lastName.trim());
        }
        
        if (active != null) {
            existingUser.setActive(active);
        }
        
        existingUser.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existingUser);
    }
    
    /**
     * Change le mot de passe d'un utilisateur.
     * 
     * @param userId ID de l'utilisateur
     * @param newPassword nouveau mot de passe en clair
     * @return utilisateur modifié
     */
    public User changeUserPassword(UUID userId, String newPassword) {
        User user = getUserById(userId);
        validatePassword(newPassword);
        
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Désactive un utilisateur du système.
     * 
     * @param userId ID de l'utilisateur à désactiver
     */
    public void deactivateUser(UUID userId) {
        User user = getUserById(userId);
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    
    /**
     * Compte le nombre total d'utilisateurs actifs.
     * 
     * @return nombre d'utilisateurs actifs
     */
    @Transactional(readOnly = true)
    public long countActiveUsers() {
        return userRepository.countByActiveTrue();
    }
    
    // ==================== MÉTHODES DE VALIDATION PRIVÉES ====================
    
    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Le nom d'utilisateur est obligatoire");
        }
        if (username.trim().length() < 3 || username.trim().length() > 50) {
            throw new ValidationException("Le nom d'utilisateur doit contenir entre 3 et 50 caractères");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new ValidationException("Le mot de passe est obligatoire");
        }
        if (password.length() < 8) {
            throw new ValidationException("Le mot de passe doit contenir au moins 8 caractères");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException("L'adresse email est obligatoire");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("L'adresse email n'est pas valide");
        }
    }
    
    private void validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new ValidationException("Le rôle est obligatoire");
        }
        try {
            User.UserRole.valueOf(role.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Rôle invalide: " + role + ". Rôles autorisés: VISITEUR, EDITEUR, ADMINISTRATEUR");
        }
    }
} 
package com.newsplatform.dto.request;

import com.newsplatform.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO de requête pour la gestion des utilisateurs via API REST.
 * Couche Présentation : Contrat REST pour CRUD utilisateurs (ADMINISTRATEUR uniquement)
 * Respecte le cahier des charges avec validation stricte.
 */
public class UserRequest {

    /**
     * Nom d'utilisateur (unique, obligatoire pour création)
     */
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres, points, tirets et underscores")
    private String username;

    /**
     * Adresse email (unique, obligatoire)
     */
    @NotBlank(message = "L'adresse email est obligatoire")
    @Email(message = "L'adresse email doit être valide")
    @Size(max = 100, message = "L'adresse email ne peut pas dépasser 100 caractères")
    private String email;

    /**
     * Mot de passe (obligatoire pour création, optionnel pour modification)
     */
    @Size(min = 8, max = 255, message = "Le mot de passe doit contenir entre 8 et 255 caractères")
    private String password;

    /**
     * Prénom de l'utilisateur
     */
    @Size(max = 100, message = "Le prénom ne peut pas dépasser 100 caractères")
    private String firstName;

    /**
     * Nom de famille de l'utilisateur
     */
    @Size(max = 100, message = "Le nom de famille ne peut pas dépasser 100 caractères")
    private String lastName;

    /**
     * Rôle de l'utilisateur (VISITEUR, EDITEUR, ADMINISTRATEUR)
     */
    @Pattern(regexp = "VISITEUR|EDITEUR|ADMINISTRATEUR", 
             message = "Le rôle doit être : VISITEUR, EDITEUR ou ADMINISTRATEUR")
    private String role;

    /**
     * Statut actif/inactif de l'utilisateur
     */
    private Boolean active;

    // Constructeurs
    public UserRequest() {}

    public UserRequest(String username, String email, String password, 
                      String firstName, String lastName, String role, Boolean active) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.active = active;
    }

    /**
     * Constructeur pour création d'utilisateur
     */
    public UserRequest(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true; // Actif par défaut
    }

    /**
     * Valide que les données sont suffisantes pour une création
     * 
     * @return true si les données sont valides pour création
     */
    public boolean isValidForCreation() {
        return username != null && !username.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               password != null && !password.trim().isEmpty() &&
               role != null && !role.trim().isEmpty();
    }

    /**
     * Valide que les données sont suffisantes pour une modification
     * 
     * @return true si au moins un champ modifiable est présent
     */
    public boolean isValidForUpdate() {
        return (email != null && !email.trim().isEmpty()) ||
               (firstName != null && !firstName.trim().isEmpty()) ||
               (lastName != null && !lastName.trim().isEmpty()) ||
               (password != null && !password.trim().isEmpty()) ||
               active != null;
    }

    /**
     * Convertit la chaîne de rôle en énumération
     * 
     * @return Rôle utilisateur ou null si invalide
     */
    public User.UserRole getRoleEnum() {
        if (role == null || role.trim().isEmpty()) {
            return null;
        }
        try {
            return User.UserRole.valueOf(role.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Getters et Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UserRequest{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", password='[PROTECTED]'" +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}

package com.newsplatformdesktopclient;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Modèle de données pour l'affichage des utilisateurs dans l'interface JavaFX.
 * Utilisé pour populer la TableView de gestion des utilisateurs.
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
public class UserDisplayModel {
    
    private final StringProperty id;
    private final StringProperty username;
    private final StringProperty email;
    private final StringProperty role;
    private final StringProperty status;
    
    /**
     * Constructeur complet pour un utilisateur
     */
    public UserDisplayModel(String id, String username, String email, String role, String status) {
        this.id = new SimpleStringProperty(id);
        this.username = new SimpleStringProperty(username);
        this.email = new SimpleStringProperty(email);
        this.role = new SimpleStringProperty(role);
        this.status = new SimpleStringProperty(status);
    }
    
    /**
     * Constructeur par défaut
     */
    public UserDisplayModel() {
        this("", "", "", "", "");
    }
    
    // Properties pour JavaFX TableView
    public StringProperty idProperty() { return id; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty emailProperty() { return email; }
    public StringProperty roleProperty() { return role; }
    public StringProperty statusProperty() { return status; }
    
    // Getters
    public String getId() { return id.get(); }
    public String getUsername() { return username.get(); }
    public String getEmail() { return email.get(); }
    public String getRole() { return role.get(); }
    public String getStatus() { return status.get(); }
    
    // Setters
    public void setId(String id) { this.id.set(id); }
    public void setUsername(String username) { this.username.set(username); }
    public void setEmail(String email) { this.email.set(email); }
    public void setRole(String role) { this.role.set(role); }
    public void setStatus(String status) { this.status.set(status); }
    
    @Override
    public String toString() {
        return String.format("User{id='%s', username='%s', email='%s', role='%s', status='%s'}", 
                           getId(), getUsername(), getEmail(), getRole(), getStatus());
    }
} 
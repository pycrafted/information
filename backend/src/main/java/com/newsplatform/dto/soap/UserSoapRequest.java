package com.newsplatform.dto.soap;

import com.newsplatform.entity.User;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * DTO de requête SOAP pour les opérations utilisateur.
 * Couche Présentation : Contrat SOAP pour CRUD utilisateurs
 * Respecte le cahier des charges : "Gestion des utilisateurs : lister, ajouter, modifier, supprimer"
 */
@XmlRootElement(name = "userRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserSoapRequest {

    /**
     * Type d'opération : LIST, ADD, UPDATE, DELETE
     */
    @XmlElement(required = true)
    private String operation;

    /**
     * Jeton d'authentification pour sécuriser l'accès
     */
    @XmlElement(required = true)
    private String authToken;

    /**
     * ID de l'utilisateur (pour UPDATE, DELETE)
     */
    @XmlElement
    private String userId;

    /**
     * Données utilisateur (pour ADD, UPDATE)
     */
    @XmlElement
    private UserData userData;

    /**
     * Paramètres de pagination (pour LIST)
     */
    @XmlElement
    private PaginationParams pagination;

    /**
     * Classe interne pour les données utilisateur
     */
    public static class UserData {
        @XmlElement
        private String username;
        
        @XmlElement
        private String email;
        
        @XmlElement
        private String password;
        
        @XmlElement
        private String firstName;
        
        @XmlElement
        private String lastName;
        
        @XmlElement
        private String role; // VISITEUR, EDITEUR, ADMINISTRATEUR
        
        @XmlElement
        private Boolean active;

        // Constructeurs
        public UserData() {}

        public UserData(String username, String email, String password, 
                       String firstName, String lastName, String role, Boolean active) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.active = active;
        }

        // Getters et Setters
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }
    }

    /**
     * Classe interne pour la pagination
     */
    public static class PaginationParams {
        @XmlElement
        private Integer page = 0;
        
        @XmlElement
        private Integer size = 10;
        
        @XmlElement
        private String sortBy = "username";
        
        @XmlElement
        private String sortDir = "ASC";

        // Constructeurs
        public PaginationParams() {}

        public PaginationParams(Integer page, Integer size, String sortBy, String sortDir) {
            this.page = page;
            this.size = size;
            this.sortBy = sortBy;
            this.sortDir = sortDir;
        }

        // Getters et Setters
        public Integer getPage() { return page; }
        public void setPage(Integer page) { this.page = page; }

        public Integer getSize() { return size; }
        public void setSize(Integer size) { this.size = size; }

        public String getSortBy() { return sortBy; }
        public void setSortBy(String sortBy) { this.sortBy = sortBy; }

        public String getSortDir() { return sortDir; }
        public void setSortDir(String sortDir) { this.sortDir = sortDir; }
    }

    // Constructeurs
    public UserSoapRequest() {}

    public UserSoapRequest(String operation, String authToken) {
        this.operation = operation;
        this.authToken = authToken;
    }

    // Méthodes factory pour les opérations courantes
    public static UserSoapRequest listUsers(String authToken) {
        return new UserSoapRequest("LIST", authToken);
    }

    public static UserSoapRequest listUsers(String authToken, PaginationParams pagination) {
        UserSoapRequest request = new UserSoapRequest("LIST", authToken);
        request.setPagination(pagination);
        return request;
    }

    public static UserSoapRequest addUser(String authToken, UserData userData) {
        UserSoapRequest request = new UserSoapRequest("ADD", authToken);
        request.setUserData(userData);
        return request;
    }

    public static UserSoapRequest updateUser(String authToken, String userId, UserData userData) {
        UserSoapRequest request = new UserSoapRequest("UPDATE", authToken);
        request.setUserId(userId);
        request.setUserData(userData);
        return request;
    }

    public static UserSoapRequest deleteUser(String authToken, String userId) {
        UserSoapRequest request = new UserSoapRequest("DELETE", authToken);
        request.setUserId(userId);
        return request;
    }

    // Getters et Setters
    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public UserData getUserData() { return userData; }
    public void setUserData(UserData userData) { this.userData = userData; }

    public PaginationParams getPagination() { return pagination; }
    public void setPagination(PaginationParams pagination) { this.pagination = pagination; }

    @Override
    public String toString() {
        return "UserSoapRequest{" +
                "operation='" + operation + '\'' +
                ", authToken='[PROTECTED]'" +
                ", userId='" + userId + '\'' +
                ", userData=" + userData +
                ", pagination=" + pagination +
                '}';
    }
} 
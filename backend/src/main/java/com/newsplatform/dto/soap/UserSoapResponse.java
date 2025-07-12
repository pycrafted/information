package com.newsplatform.dto.soap;

import com.newsplatform.entity.User;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO de réponse SOAP pour les opérations utilisateur.
 * Couche Présentation : Contrat SOAP pour retourner les résultats CRUD utilisateurs
 * Respecte le cahier des charges avec sécurisation par jetons d'authentification.
 */
@XmlRootElement(name = "userResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserSoapResponse {

    /**
     * Statut de l'opération
     */
    @XmlElement(required = true)
    private boolean success;

    /**
     * Message de réponse
     */
    @XmlElement
    private String message;

    /**
     * Code d'erreur (si applicable)
     */
    @XmlElement
    private String errorCode;

    /**
     * Utilisateur unique (pour ADD, UPDATE, GET)
     */
    @XmlElement
    private UserInfo user;

    /**
     * Liste d'utilisateurs (pour LIST)
     */
    @XmlElement
    private List<UserInfo> users;

    /**
     * Informations de pagination (pour LIST)
     */
    @XmlElement
    private PageInfo pageInfo;

    /**
     * Statistiques de l'opération
     */
    @XmlElement
    private OperationStats stats;

    /**
     * Classe interne pour les informations utilisateur sécurisées
     */
    public static class UserInfo {
        @XmlElement
        private String id;
        
        @XmlElement
        private String username;
        
        @XmlElement
        private String email;
        
        @XmlElement
        private String firstName;
        
        @XmlElement
        private String lastName;
        
        @XmlElement
        private String role;
        
        @XmlElement
        private String roleDescription;
        
        @XmlElement
        private Boolean active;
        
        @XmlElement
        private LocalDateTime createdAt;
        
        @XmlElement
        private LocalDateTime lastLogin;

        // Constructeurs
        public UserInfo() {}

        public UserInfo(User user) {
            this.id = user.getId().toString();
            this.username = user.getUsername();
            this.email = user.getEmail();
            this.firstName = user.getFirstName();
            this.lastName = user.getLastName();
            this.role = user.getRole().name();
            this.roleDescription = user.getRole().getDescription();
            this.active = user.getActive();
            this.createdAt = user.getCreatedAt();
            this.lastLogin = user.getLastLogin();
        }

        // Getters et Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }

        public String getRoleDescription() { return roleDescription; }
        public void setRoleDescription(String roleDescription) { this.roleDescription = roleDescription; }

        public Boolean getActive() { return active; }
        public void setActive(Boolean active) { this.active = active; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDateTime getLastLogin() { return lastLogin; }
        public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
    }

    /**
     * Classe interne pour les informations de pagination
     */
    public static class PageInfo {
        @XmlElement
        private Integer currentPage;
        
        @XmlElement
        private Integer totalPages;
        
        @XmlElement
        private Long totalElements;
        
        @XmlElement
        private Integer pageSize;
        
        @XmlElement
        private Boolean hasNext;
        
        @XmlElement
        private Boolean hasPrevious;

        // Constructeurs
        public PageInfo() {}

        public PageInfo(Integer currentPage, Integer totalPages, Long totalElements, 
                       Integer pageSize, Boolean hasNext, Boolean hasPrevious) {
            this.currentPage = currentPage;
            this.totalPages = totalPages;
            this.totalElements = totalElements;
            this.pageSize = pageSize;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        // Getters et Setters
        public Integer getCurrentPage() { return currentPage; }
        public void setCurrentPage(Integer currentPage) { this.currentPage = currentPage; }

        public Integer getTotalPages() { return totalPages; }
        public void setTotalPages(Integer totalPages) { this.totalPages = totalPages; }

        public Long getTotalElements() { return totalElements; }
        public void setTotalElements(Long totalElements) { this.totalElements = totalElements; }

        public Integer getPageSize() { return pageSize; }
        public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }

        public Boolean getHasNext() { return hasNext; }
        public void setHasNext(Boolean hasNext) { this.hasNext = hasNext; }

        public Boolean getHasPrevious() { return hasPrevious; }
        public void setHasPrevious(Boolean hasPrevious) { this.hasPrevious = hasPrevious; }
    }

    /**
     * Classe interne pour les statistiques d'opération
     */
    public static class OperationStats {
        @XmlElement
        private LocalDateTime timestamp;
        
        @XmlElement
        private String operation;
        
        @XmlElement
        private Long executionTimeMs;
        
        @XmlElement
        private Integer affectedRecords;

        // Constructeurs
        public OperationStats() {}

        public OperationStats(String operation, Long executionTimeMs, Integer affectedRecords) {
            this.timestamp = LocalDateTime.now();
            this.operation = operation;
            this.executionTimeMs = executionTimeMs;
            this.affectedRecords = affectedRecords;
        }

        // Getters et Setters
        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getOperation() { return operation; }
        public void setOperation(String operation) { this.operation = operation; }

        public Long getExecutionTimeMs() { return executionTimeMs; }
        public void setExecutionTimeMs(Long executionTimeMs) { this.executionTimeMs = executionTimeMs; }

        public Integer getAffectedRecords() { return affectedRecords; }
        public void setAffectedRecords(Integer affectedRecords) { this.affectedRecords = affectedRecords; }
    }

    // Constructeurs
    public UserSoapResponse() {}

    public UserSoapResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    // Méthodes factory pour les réponses courantes
    public static UserSoapResponse success(String message) {
        return new UserSoapResponse(true, message);
    }

    public static UserSoapResponse failure(String message) {
        return new UserSoapResponse(false, message);
    }

    public static UserSoapResponse failure(String message, String errorCode) {
        UserSoapResponse response = new UserSoapResponse(false, message);
        response.setErrorCode(errorCode);
        return response;
    }

    public static UserSoapResponse withUser(User user, String message) {
        UserSoapResponse response = success(message);
        response.setUser(new UserInfo(user));
        return response;
    }

    public static UserSoapResponse withUsers(List<User> users, PageInfo pageInfo, String message) {
        UserSoapResponse response = success(message);
        response.setUsers(users.stream().map(UserInfo::new).toList());
        response.setPageInfo(pageInfo);
        return response;
    }

    // Getters et Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }

    public UserInfo getUser() { return user; }
    public void setUser(UserInfo user) { this.user = user; }

    public List<UserInfo> getUsers() { return users; }
    public void setUsers(List<UserInfo> users) { this.users = users; }

    public PageInfo getPageInfo() { return pageInfo; }
    public void setPageInfo(PageInfo pageInfo) { this.pageInfo = pageInfo; }

    public OperationStats getStats() { return stats; }
    public void setStats(OperationStats stats) { this.stats = stats; }

    @Override
    public String toString() {
        return "UserSoapResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", errorCode='" + errorCode + '\'' +
                ", userCount=" + (users != null ? users.size() : (user != null ? 1 : 0)) +
                '}';
    }
} 
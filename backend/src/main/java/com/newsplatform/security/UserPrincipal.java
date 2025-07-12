package com.newsplatform.security;

import com.newsplatform.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;

/**
 * Principal personnalisé pour l'authentification Spring Security.
 * Couche Sécurité : Encapsulation des informations utilisateur pour Spring Security
 * Implémente UserDetails pour compatibilité avec le framework de sécurité.
 */
public class UserPrincipal implements UserDetails {

    private final UUID id;
    private final String username;
    private final String email;
    private final User.UserRole role;
    private final boolean active;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructeur principal
     * 
     * @param id ID unique de l'utilisateur
     * @param username Nom d'utilisateur
     * @param email Email de l'utilisateur
     * @param role Rôle métier de l'utilisateur
     * @param active Statut d'activation du compte
     */
    public UserPrincipal(UUID id, String username, String email, User.UserRole role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.active = active;
    }

    /**
     * Constructeur avec autorités
     * 
     * @param id ID unique de l'utilisateur
     * @param username Nom d'utilisateur
     * @param email Email de l'utilisateur
     * @param role Rôle métier de l'utilisateur
     * @param active Statut d'activation du compte
     * @param authorities Autorités Spring Security
     */
    public UserPrincipal(UUID id, String username, String email, User.UserRole role, 
                        boolean active, Collection<? extends GrantedAuthority> authorities) {
        this(id, username, email, role, active);
        this.authorities = authorities;
    }

    /**
     * Méthode factory pour créer un UserPrincipal depuis une entité User
     * 
     * @param user Entité utilisateur du domaine
     * @return UserPrincipal configuré
     */
    public static UserPrincipal create(User user) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getActive()
        );
    }

    /**
     * Méthode factory avec autorités Spring Security
     * 
     * @param user Entité utilisateur du domaine
     * @param authorities Autorités Spring Security
     * @return UserPrincipal configuré avec autorités
     */
    public static UserPrincipal create(User user, Collection<? extends GrantedAuthority> authorities) {
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            user.getActive(),
            authorities
        );
    }

    // Getters métier personnalisés
    public UUID getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public User.UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        this.authorities = authorities;
    }

    // Implémentation UserDetails pour Spring Security

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        // Le mot de passe n'est pas stocké dans le principal pour sécurité
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Géré par le statut active
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Géré par le statut active
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Géré par l'expiration des jetons JWT
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        UserPrincipal that = (UserPrincipal) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "UserPrincipal{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }
}

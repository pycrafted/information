package com.newsplatform.repository;

import com.newsplatform.entity.User;
import com.newsplatform.entity.User.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository pour la gestion des utilisateurs
 * Couche Persistance : Accès aux données utilisateur avec requêtes personnalisées
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Requêtes de recherche par attributs uniques
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsernameOrEmail(String username, String email);

    // Vérifications d'existence pour validation
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByUsernameOrEmail(String username, String email);

    // Requêtes par rôle
    List<User> findByRole(UserRole role);
    
    Page<User> findByRole(UserRole role, Pageable pageable);

    // Requêtes par statut d'activation
    List<User> findByActiveTrue();
    
    List<User> findByActiveFalse();
    
    Page<User> findByActiveTrue(Pageable pageable);
    
    Page<User> findByActiveFalse(Pageable pageable);

    // Requêtes par date de dernière connexion
    List<User> findByLastLoginAfter(LocalDateTime date);
    
    List<User> findByLastLoginBefore(LocalDateTime date);
    
    List<User> findByLastLoginIsNull();

    // Requêtes combinées
    List<User> findByRoleAndActiveTrue(UserRole role);
    
    Page<User> findByRoleAndActiveTrueOrderByCreatedAtDesc(UserRole role, Pageable pageable);

    // Requêtes de recherche textuelles
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    @Query("SELECT u FROM User u WHERE " +
           "u.active = true AND (" +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<User> searchActiveUsers(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Statistiques et comptages
    long countByRole(UserRole role);
    
    long countByActiveTrue();
    
    long countByActiveFalse();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin > :date")
    long countUsersConnectedSince(@Param("date") LocalDateTime date);

    // Requêtes d'administration
    @Query("SELECT u FROM User u WHERE u.role = :role AND u.createdAt BETWEEN :startDate AND :endDate")
    List<User> findUsersByRoleCreatedBetween(
        @Param("role") UserRole role, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate
    );

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NULL AND u.createdAt < :date")
    List<User> findUsersNeverLoggedInCreatedBefore(@Param("date") LocalDateTime date);

    // Requêtes pour la gestion des administrateurs
    @Query("SELECT u FROM User u WHERE u.role = 'ADMINISTRATEUR' AND u.active = true")
    List<User> findActiveAdministrators();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = 'ADMINISTRATEUR' AND u.active = true")
    long countActiveAdministrators();

    // Requêtes pour l'audit
    @Query("SELECT u FROM User u ORDER BY u.lastLogin DESC")
    List<User> findAllOrderByLastLoginDesc();
    
    @Query("SELECT u FROM User u WHERE u.updatedAt > :date ORDER BY u.updatedAt DESC")
    List<User> findRecentlyUpdatedUsers(@Param("date") LocalDateTime date);

    // Requêtes pour la pagination avancée
    Page<User> findByRoleOrderByCreatedAtDesc(UserRole role, Pageable pageable);
    
    Page<User> findByActiveTrueOrderByLastLoginDesc(Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:role IS NULL OR u.role = :role) AND " +
           "(:active IS NULL OR u.active = :active) " +
           "ORDER BY u.createdAt DESC")
    Page<User> findUsersWithFilters(
        @Param("role") UserRole role, 
        @Param("active") Boolean active, 
        Pageable pageable
    );
}

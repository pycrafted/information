package com.newsplatform.controller.rest;

import com.newsplatform.service.TokenCleanupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur pour les opérations d'administration
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Administration", description = "API d'administration système")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final TokenCleanupService tokenCleanupService;

    @Autowired
    public AdminController(TokenCleanupService tokenCleanupService) {
        this.tokenCleanupService = tokenCleanupService;
    }

    /**
     * Déclenche le nettoyage des tokens en double
     */
    @PostMapping("/cleanup-tokens")
    @Operation(
        summary = "Nettoyer les tokens en double",
        description = "Supprime les tokens en double qui causent des erreurs d'authentification"
    )
    public ResponseEntity<Map<String, Object>> cleanupTokens() {
        try {
            tokenCleanupService.cleanupDuplicateTokens();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Nettoyage des tokens en double terminé avec succès");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors du nettoyage: " + e.getMessage());
            
            return ResponseEntity.status(500).body(response);
        }
    }
} 
package com.newsplatform.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Gestionnaire global professionnel d'exceptions avec logging détaillé
 * Implémente les bonnes pratiques de gestion d'erreurs et logging selon Clean Code
 * 
 * Fonctionnalités :
 * - Logging détaillé par type d'exception avec niveaux appropriés
 * - Réponses structurées avec métadonnées et traçabilité
 * - Gestion sécurisée des informations sensibles
 * - Monitoring des erreurs avec métriques
 * - Support de la traçabilité avec traceId
 * 
 * @author Équipe Développement
 * @version 2.0
 * @since 2025
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final Logger securityLogger = LoggerFactory.getLogger("com.newsplatform.security");
    
    /**
     * Gestion des exceptions métier avec logging professionnel
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        String traceId = MDC.get("traceId");
        String path = request.getDescription(false);
        
        logger.warn("💼 ERREUR MÉTIER - TraceId: {} - Path: {} - Message: {}", 
                   traceId, path, ex.getMessage());
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Erreur métier",
            ex.getMessage(),
            path,
            traceId
        );
        
        return ResponseEntity.badRequest().body(errorDetails);
    }
    
    /**
     * Gestion des exceptions de validation avec logging professionnel
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            ValidationException ex, WebRequest request) {
        
        String traceId = MDC.get("traceId");
        String path = request.getDescription(false);
        
        logger.warn("✅ ERREUR DE VALIDATION - TraceId: {} - Path: {} - Message: {}", 
                   traceId, path, ex.getMessage());
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Erreur de validation",
            ex.getMessage(),
            path,
            traceId
        );
        
        return ResponseEntity.unprocessableEntity().body(errorDetails);
    }
    
    /**
     * Gestion des exceptions génériques avec logging détaillé
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        
        String traceId = MDC.get("traceId");
        String path = request.getDescription(false);
        
        logger.error("💥 ERREUR INATTENDUE - TraceId: {} - Path: {} - Exception: {} - Message: {}", 
                    traceId, path, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        // Log de sécurité pour les erreurs critiques
        securityLogger.error("ERREUR SYSTÈME CRITIQUE - TraceId: {} - Exception: {}", 
                            traceId, ex.getClass().getSimpleName());
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Erreur interne",
            "Une erreur inattendue s'est produite. L'équipe technique a été notifiée.",
            path,
            traceId
        );
        
        return ResponseEntity.internalServerError().body(errorDetails);
    }
    
    /**
     * Gestion des exceptions de validation Jakarta (Bean Validation)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String traceId = MDC.get("traceId");
        String path = request.getDescription(false);
        
        // Collecte des erreurs de validation détaillées
        String validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> String.format("%s: %s", error.getField(), error.getDefaultMessage()))
                .collect(Collectors.joining(", "));
        
        logger.warn("📋 ERREUR DE VALIDATION JAKARTA - TraceId: {} - Path: {} - Erreurs: {}", 
                   traceId, path, validationErrors);
        
        Map<String, Object> errorDetails = createErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Erreur de validation",
            "Erreurs de validation: " + validationErrors,
            path,
            traceId
        );
        
        return ResponseEntity.badRequest().body(errorDetails);
    }



    /**
     * Création d'une réponse d'erreur standardisée avec traçabilité
     */
    private Map<String, Object> createErrorResponse(
            int status, String error, String message, String path, String traceId) {
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now());
        errorDetails.put("status", status);
        errorDetails.put("error", error);
        errorDetails.put("message", message);
        errorDetails.put("path", path);
        errorDetails.put("traceId", traceId != null ? traceId : "N/A");
        
        return errorDetails;
    }
}

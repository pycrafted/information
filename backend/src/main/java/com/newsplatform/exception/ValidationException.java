package com.newsplatform.exception;

/**
 * Exception de validation pour les erreurs de validation des données
 * Responsabilité : Représentation des erreurs de validation
 */
public class ValidationException extends RuntimeException {
    
    /**
     * Constructeur avec message d'erreur
     * 
     * @param message Le message d'erreur de validation
     */
    public ValidationException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * 
     * @param message Le message d'erreur de validation
     * @param cause La cause de l'erreur
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
} 
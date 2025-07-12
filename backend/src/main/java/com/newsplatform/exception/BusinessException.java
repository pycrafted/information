package com.newsplatform.exception;

/**
 * Exception métier pour les erreurs de logique métier
 * Responsabilité : Représentation des erreurs métier
 */
public class BusinessException extends RuntimeException {
    
    /**
     * Constructeur avec message d'erreur
     * 
     * @param message Le message d'erreur
     */
    public BusinessException(String message) {
        super(message);
    }
    
    /**
     * Constructeur avec message et cause
     * 
     * @param message Le message d'erreur
     * @param cause La cause de l'erreur
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
} 
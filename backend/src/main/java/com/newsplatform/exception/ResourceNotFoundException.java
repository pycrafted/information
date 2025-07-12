package com.newsplatform.exception;

/**
 * Exception personnalisée pour signaler qu'une ressource n'a pas été trouvée.
 * Utilisée pour retourner une erreur 404 dans l'API REST.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

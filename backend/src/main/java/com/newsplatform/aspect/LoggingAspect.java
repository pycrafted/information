package com.newsplatform.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.Arrays;
import java.util.UUID;

/**
 * Aspect professionnel pour le logging automatique et monitoring des performances
 * Implémente les bonnes pratiques de logging selon Clean Code
 * 
 * Fonctionnalités :
 * - Logging automatique des méthodes avec paramètres
 * - Mesure des performances avec StopWatch
 * - Gestion des erreurs avec stack traces détaillées
 * - Traçabilité avec traceId unique par requête
 * - Logging spécialisé par couche (Controller, Service, Repository)
 * 
 * @author Équipe Développement
 * @version 2.0
 * @since 2025
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final Logger perfLogger = LoggerFactory.getLogger("com.newsplatform.performance");
    private static final Logger securityLogger = LoggerFactory.getLogger("com.newsplatform.security");
    
    // Seuils de performance (en millisecondes)
    private static final long SLOW_METHOD_THRESHOLD = 1000; // 1 seconde
    private static final long VERY_SLOW_METHOD_THRESHOLD = 5000; // 5 secondes

    /**
     * Point de coupe pour tous les contrôleurs REST
     */
    @Pointcut("execution(* com.newsplatform.controller..*(..))")
    public void controllerMethods() {}

    /**
     * Point de coupe pour tous les services métier
     */
    @Pointcut("execution(* com.newsplatform.service..*(..))")
    public void serviceMethods() {}

    /**
     * Point de coupe pour tous les repositories
     */
    @Pointcut("execution(* com.newsplatform.repository..*(..))")
    public void repositoryMethods() {}

    /**
     * Point de coupe pour toutes les façades
     */
    @Pointcut("execution(* com.newsplatform.facade..*(..))")
    public void facadeMethods() {}

    /**
     * Logging complet autour des méthodes de contrôleur avec mesure de performance
     */
    @Around("controllerMethods()")
    public Object logAroundControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAroundMethod(joinPoint, "CONTROLLER");
    }

    /**
     * Logging complet autour des méthodes de service avec mesure de performance
     */
    @Around("serviceMethods()")
    public Object logAroundServiceMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAroundMethod(joinPoint, "SERVICE");
    }

    /**
     * Logging complet autour des méthodes de repository avec mesure de performance
     */
    @Around("repositoryMethods()")
    public Object logAroundRepositoryMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAroundMethod(joinPoint, "REPOSITORY");
    }

    /**
     * Logging complet autour des méthodes de façade avec mesure de performance
     */
    @Around("facadeMethods()")
    public Object logAroundFacadeMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        return logAroundMethod(joinPoint, "FACADE");
    }

    /**
     * Méthode centrale de logging avec mesure de performance
     */
    private Object logAroundMethod(ProceedingJoinPoint joinPoint, String layer) throws Throwable {
        // Génération d'un traceId unique si pas déjà présent
        String traceId = MDC.get("traceId");
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
            MDC.put("traceId", traceId);
        }

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        // Masquer les mots de passe dans les logs
        Object[] sanitizedArgs = sanitizeArguments(args);
        
        StopWatch stopWatch = new StopWatch();
        
        try {
            // Log d'entrée avec paramètres
            logger.debug("🚀 [{}] Début {}.{}() avec paramètres: {}", 
                        layer, className, methodName, 
                        sanitizedArgs.length > 0 ? Arrays.toString(sanitizedArgs) : "aucun");
            
            stopWatch.start();
            Object result = joinPoint.proceed();
            stopWatch.stop();
            
            long executionTime = stopWatch.getTotalTimeMillis();
            
            // Log de sortie avec performance
            logger.debug("✅ [{}] Fin {}.{}() - Durée: {}ms", 
                        layer, className, methodName, executionTime);
            
            // Log de performance si méthode lente
            logPerformanceIfSlow(className, methodName, executionTime, layer);
            
            return result;
            
        } catch (Exception e) {
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            
            long executionTime = stopWatch.getTotalTimeMillis();
            
            // Log d'erreur détaillé
            logger.error("❌ [{}] Erreur dans {}.{}() après {}ms - Exception: {} - Message: {}", 
                        layer, className, methodName, executionTime, 
                        e.getClass().getSimpleName(), e.getMessage());
            
            // Log de sécurité pour certaines erreurs critiques
            if (isSecurityRelatedError(e)) {
                securityLogger.warn("Erreur sécurité dans {}.{}(): {}", 
                                  className, methodName, e.getMessage());
            }
            
            throw e;
        } finally {
            // Nettoyage du MDC si on a créé le traceId
            if (MDC.get("traceId") != null && MDC.get("traceId").equals(traceId)) {
                MDC.remove("traceId");
            }
        }
    }

    /**
     * Masque les informations sensibles dans les arguments de log
     */
    private Object[] sanitizeArguments(Object[] args) {
        if (args == null) return new Object[0];
        
        Object[] sanitized = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg != null) {
                String argString = arg.toString();
                // Masquer les mots de passe
                if (argString.toLowerCase().contains("password") || 
                    argString.toLowerCase().contains("token") ||
                    argString.toLowerCase().contains("secret")) {
                    sanitized[i] = "[MASQUÉ]";
                } else if (argString.length() > 200) {
                    // Tronquer les arguments très longs
                    sanitized[i] = argString.substring(0, 200) + "...";
                } else {
                    sanitized[i] = arg;
                }
            } else {
                sanitized[i] = null;
            }
        }
        return sanitized;
    }

    /**
     * Log spécialisé pour les performances si la méthode est lente
     */
    private void logPerformanceIfSlow(String className, String methodName, long executionTime, String layer) {
        if (executionTime > VERY_SLOW_METHOD_THRESHOLD) {
            perfLogger.warn("🐌 TRÈS LENT - [{}] {}.{}() a pris {}ms (> {}ms)", 
                           layer, className, methodName, executionTime, VERY_SLOW_METHOD_THRESHOLD);
        } else if (executionTime > SLOW_METHOD_THRESHOLD) {
            perfLogger.info("⚠️  LENT - [{}] {}.{}() a pris {}ms (> {}ms)", 
                           layer, className, methodName, executionTime, SLOW_METHOD_THRESHOLD);
        } else {
            perfLogger.debug("⚡ RAPIDE - [{}] {}.{}() a pris {}ms", 
                            layer, className, methodName, executionTime);
        }
    }

    /**
     * Détermine si l'erreur est liée à la sécurité
     */
    private boolean isSecurityRelatedError(Exception e) {
        String errorType = e.getClass().getSimpleName().toLowerCase();
        String errorMessage = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
        
        return errorType.contains("security") || 
               errorType.contains("auth") || 
               errorType.contains("access") ||
               errorMessage.contains("unauthorized") ||
               errorMessage.contains("forbidden") ||
               errorMessage.contains("authentication");
    }

    /**
     * Log spécialisé pour les exceptions non gérées
     */
    @AfterThrowing(pointcut = "execution(* com.newsplatform..*(..))", throwing = "exception")
    public void logUnhandledException(JoinPoint joinPoint, Throwable exception) {
        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        logger.error("💥 EXCEPTION NON GÉRÉE dans {}.{}(): {} - Cause: {}", 
                    className, methodName, exception.getMessage(), 
                    exception.getCause() != null ? exception.getCause().getMessage() : "N/A", exception);
    }
}

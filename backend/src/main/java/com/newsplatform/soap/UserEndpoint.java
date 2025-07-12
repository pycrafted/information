package com.newsplatform.soap;

import com.newsplatform.dto.soap.UserSoapRequest;
import com.newsplatform.dto.soap.UserSoapResponse;
import com.newsplatform.entity.User;
import com.newsplatform.service.TokenService;
import com.newsplatform.service.UserSoapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint SOAP pour la gestion des utilisateurs.
 * Couche Présentation : Interface SOAP pour CRUD utilisateurs
 * Respecte le cahier des charges : "Gestion des utilisateurs : lister, ajouter, modifier, supprimer"
 */
@Endpoint
public class UserEndpoint {

    private static final String NAMESPACE_URI = "http://newsplatform.com/soap/users";

    private final UserSoapService userSoapService;
    private final TokenService tokenService;

    @Autowired
    public UserEndpoint(UserSoapService userSoapService, TokenService tokenService) {
        this.userSoapService = userSoapService;
        this.tokenService = tokenService;
    }

    /**
     * Service SOAP unifié pour la gestion des utilisateurs
     * Interface SOAP : Opérations CRUD selon le cahier des charges
     * 
     * @param request Requête SOAP avec opération (LIST, ADD, UPDATE, DELETE)
     * @return Réponse SOAP avec résultats de l'opération
     */
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "userRequest")
    @ResponsePayload
    public UserSoapResponse manageUsers(@RequestPayload UserSoapRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Validation du jeton d'authentification
            if (!isValidAuthToken(request.getAuthToken())) {
                return UserSoapResponse.failure("Jeton d'authentification invalide", "AUTH_INVALID");
            }

            // Vérification des permissions administrateur
            if (!isAdminToken(request.getAuthToken())) {
                return UserSoapResponse.failure("Accès refusé - Privilèges administrateur requis", "ACCESS_DENIED");
            }

            // Dispatch selon l'opération demandée
            UserSoapResponse response = switch (request.getOperation().toUpperCase()) {
                case "LIST" -> handleListUsers(request);
                case "ADD" -> handleAddUser(request);
                case "UPDATE" -> handleUpdateUser(request);
                case "DELETE" -> handleDeleteUser(request);
                default -> UserSoapResponse.failure("Opération non supportée: " + request.getOperation(), "INVALID_OPERATION");
            };

            // Ajout des statistiques d'exécution
            long executionTime = System.currentTimeMillis() - startTime;
            response.setStats(new UserSoapResponse.OperationStats(
                request.getOperation(),
                executionTime,
                getAffectedRecordsCount(response)
            ));

            return response;

        } catch (Exception e) {
            return UserSoapResponse.failure(
                "Erreur du service SOAP : " + e.getMessage(),
                "SERVICE_ERROR"
            );
        }
    }

    /**
     * Gestion de l'opération LIST : Lister les utilisateurs
     * 
     * @param request Requête SOAP avec paramètres de pagination
     * @return Liste d'utilisateurs avec pagination
     */
    private UserSoapResponse handleListUsers(UserSoapRequest request) {
        try {
            // Configuration de la pagination par défaut
            UserSoapRequest.PaginationParams pagination = request.getPagination();
            if (pagination == null) {
                pagination = new UserSoapRequest.PaginationParams(0, 10, "username", "ASC");
            }

            // Récupération de tous les utilisateurs (pour l'instant sans pagination Spring Data)
            List<User> allUsers = userSoapService.getAllUsers();
            
            // Simulation de pagination manuelle pour le moment
            int totalElements = allUsers.size();
            int page = pagination.getPage();
            int size = pagination.getSize();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            int fromIndex = page * size;
            int toIndex = Math.min(fromIndex + size, totalElements);
            
            List<User> paginatedUsers = fromIndex < totalElements ? 
                allUsers.subList(fromIndex, toIndex) : List.of();

            // Création des informations de pagination
            UserSoapResponse.PageInfo pageInfo = new UserSoapResponse.PageInfo(
                page,
                totalPages,
                (long) totalElements,
                size,
                page < totalPages - 1,
                page > 0
            );

            return UserSoapResponse.withUsers(
                paginatedUsers,
                pageInfo,
                "Utilisateurs récupérés avec succès"
            );

        } catch (Exception e) {
            return UserSoapResponse.failure("Erreur lors de la récupération des utilisateurs: " + e.getMessage());
        }
    }

    /**
     * Gestion de l'opération ADD : Ajouter un utilisateur
     * 
     * @param request Requête SOAP avec données utilisateur
     * @return Utilisateur créé
     */
    private UserSoapResponse handleAddUser(UserSoapRequest request) {
        try {
            UserSoapRequest.UserData userData = request.getUserData();
            if (userData == null) {
                return UserSoapResponse.failure("Données utilisateur obligatoires pour l'ajout", "MISSING_DATA");
            }

            User newUser = userSoapService.addUser(
                userData.getUsername(),
                userData.getPassword(),
                userData.getEmail(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getRole()
            );

            return UserSoapResponse.withUser(newUser, "Utilisateur créé avec succès");

        } catch (Exception e) {
            return UserSoapResponse.failure("Erreur lors de la création de l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Gestion de l'opération UPDATE : Modifier un utilisateur
     * 
     * @param request Requête SOAP avec ID et nouvelles données
     * @return Utilisateur modifié
     */
    private UserSoapResponse handleUpdateUser(UserSoapRequest request) {
        try {
            String userId = request.getUserId();
            UserSoapRequest.UserData userData = request.getUserData();
            
            if (userId == null || userId.trim().isEmpty()) {
                return UserSoapResponse.failure("ID utilisateur obligatoire pour la modification", "MISSING_ID");
            }
            
            if (userData == null) {
                return UserSoapResponse.failure("Données utilisateur obligatoires pour la modification", "MISSING_DATA");
            }

            UUID userUUID = UUID.fromString(userId);
            
            User updatedUser = userSoapService.updateUser(
                userUUID,
                userData.getEmail(),
                userData.getFirstName(),
                userData.getLastName(),
                userData.getActive()
            );

            // Si un nouveau mot de passe est fourni, le changer
            if (userData.getPassword() != null && !userData.getPassword().trim().isEmpty()) {
                updatedUser = userSoapService.changeUserPassword(userUUID, userData.getPassword());
            }

            return UserSoapResponse.withUser(updatedUser, "Utilisateur modifié avec succès");

        } catch (IllegalArgumentException e) {
            return UserSoapResponse.failure("ID utilisateur invalide", "INVALID_ID");
        } catch (Exception e) {
            return UserSoapResponse.failure("Erreur lors de la modification de l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Gestion de l'opération DELETE : Supprimer (désactiver) un utilisateur
     * 
     * @param request Requête SOAP avec ID utilisateur
     * @return Confirmation de suppression
     */
    private UserSoapResponse handleDeleteUser(UserSoapRequest request) {
        try {
            String userId = request.getUserId();
            
            if (userId == null || userId.trim().isEmpty()) {
                return UserSoapResponse.failure("ID utilisateur obligatoire pour la suppression", "MISSING_ID");
            }

            UUID userUUID = UUID.fromString(userId);
            
            // Désactivation sécurisée plutôt que suppression physique
            userSoapService.deactivateUser(userUUID);

            return UserSoapResponse.success("Utilisateur désactivé avec succès");

        } catch (IllegalArgumentException e) {
            return UserSoapResponse.failure("ID utilisateur invalide", "INVALID_ID");
        } catch (Exception e) {
            return UserSoapResponse.failure("Erreur lors de la suppression de l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Validation du jeton d'authentification
     * Sécurité : Vérification que le jeton est valide
     * 
     * @param authToken Jeton à valider
     * @return true si le jeton est valide
     */
    private boolean isValidAuthToken(String authToken) {
        if (authToken == null || authToken.trim().isEmpty()) {
            return false;
        }
        
        try {
            return tokenService.validateAccessToken(authToken).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Vérification des privilèges administrateur
     * Sécurité : Seuls les administrateurs peuvent gérer les utilisateurs
     * 
     * @param authToken Jeton à vérifier
     * @return true si le jeton appartient à un administrateur
     */
    private boolean isAdminToken(String authToken) {
        try {
            return tokenService.isAdminToken(authToken);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Calcul du nombre d'enregistrements affectés par l'opération
     * 
     * @param response Réponse SOAP
     * @return Nombre d'enregistrements affectés
     */
    private int getAffectedRecordsCount(UserSoapResponse response) {
        if (response.getUsers() != null) {
            return response.getUsers().size();
        } else if (response.getUser() != null) {
            return 1;
        } else {
            return response.isSuccess() ? 1 : 0;
        }
    }
} 
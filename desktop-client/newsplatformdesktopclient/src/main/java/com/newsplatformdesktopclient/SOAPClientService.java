package com.newsplatformdesktopclient;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Service client SOAP pour la communication avec le backend.
 * Couche Service Client : Appels SOAP réels vers les services backend
 * Respecte le cahier des charges avec authentification sécurisée par jetons.
 */
public class SOAPClientService {
    
    private static final String SOAP_URL_AUTH = "http://localhost:8080/soap/auth";
    private static final String SOAP_URL_USERS = "http://localhost:8080/soap/users";
    private static final String SOAP_ACTION_LOGIN = "http://newsplatform.com/soap/auth/login";
    private static final String SOAP_ACTION_LOGOUT = "http://newsplatform.com/soap/auth/logout";
    private static final String SOAP_ACTION_USERS = "http://newsplatform.com/soap/users/manageUsers";
    
    private final HttpClient httpClient;
    
    public SOAPClientService() {
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }
    
    /**
     * Authentification utilisateur via service SOAP
     * Respecte le cahier des charges : "Authentification d'un utilisateur (login + mot de passe)"
     * 
     * @param username Nom d'utilisateur
     * @param password Mot de passe
     * @return Réponse d'authentification avec jeton JWT
     * @throws Exception si erreur de communication
     */
    public AuthenticationResponse authenticateUser(String username, String password) throws Exception {
        String soapRequest = buildLoginSoapRequest(username, password);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SOAP_URL_AUTH))
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", SOAP_ACTION_LOGIN)
            .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return parseLoginResponse(response.body());
        } else {
            throw new Exception("Erreur HTTP: " + response.statusCode());
        }
    }
    
    /**
     * Récupération de la liste des utilisateurs via SOAP
     * Respecte le cahier des charges : "Gestion des utilisateurs : lister"
     * 
     * @param authToken Jeton d'authentification
     * @return Liste des utilisateurs
     * @throws Exception si erreur de communication
     */
    public List<UserInfo> getUserList(String authToken) throws Exception {
        String soapRequest = buildUserListSoapRequest(authToken);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SOAP_URL_USERS))
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", SOAP_ACTION_USERS)
            .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return parseUserListResponse(response.body());
        } else {
            throw new Exception("Erreur HTTP: " + response.statusCode());
        }
    }
    
    /**
     * Ajout d'un utilisateur via SOAP
     * Respecte le cahier des charges : "Gestion des utilisateurs : ajouter"
     * 
     * @param authToken Jeton d'authentification
     * @param userInfo Informations du nouvel utilisateur
     * @return Utilisateur créé
     * @throws Exception si erreur de communication
     */
    public UserInfo addUser(String authToken, UserInfo userInfo) throws Exception {
        String soapRequest = buildAddUserSoapRequest(authToken, userInfo);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SOAP_URL_USERS))
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", SOAP_ACTION_USERS)
            .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return parseAddUserResponse(response.body());
        } else {
            throw new Exception("Erreur HTTP: " + response.statusCode());
        }
    }
    
    /**
     * Suppression d'un utilisateur via SOAP
     * Respecte le cahier des charges : "Gestion des utilisateurs : supprimer"
     * 
     * @param authToken Jeton d'authentification
     * @param userId ID de l'utilisateur à supprimer
     * @return true si suppression réussie
     * @throws Exception si erreur de communication
     */
    public boolean deleteUser(String authToken, String userId) throws Exception {
        String soapRequest = buildDeleteUserSoapRequest(authToken, userId);
        
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(SOAP_URL_USERS))
            .header("Content-Type", "text/xml; charset=utf-8")
            .header("SOAPAction", SOAP_ACTION_USERS)
            .POST(HttpRequest.BodyPublishers.ofString(soapRequest))
            .build();
        
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            return parseDeleteUserResponse(response.body());
        } else {
            throw new Exception("Erreur HTTP: " + response.statusCode());
        }
    }
    
    // ==================== CONSTRUCTION DES REQUÊTES SOAP ====================
    
    private String buildLoginSoapRequest(String username, String password) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:auth="http://newsplatform.com/soap/auth">
                <soap:Header/>
                <soap:Body>
                    <auth:loginRequest>
                        <auth:username>%s</auth:username>
                        <auth:password>%s</auth:password>
                        <auth:clientIp>127.0.0.1</auth:clientIp>
                        <auth:userAgent>JavaFX-Client-1.0</auth:userAgent>
                    </auth:loginRequest>
                </soap:Body>
            </soap:Envelope>
            """.formatted(username, password);
    }
    
    private String buildUserListSoapRequest(String authToken) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:users="http://newsplatform.com/soap/users">
                <soap:Header/>
                <soap:Body>
                    <users:userRequest>
                        <users:operation>LIST</users:operation>
                        <users:authToken>%s</users:authToken>
                        <users:pagination>
                            <users:page>0</users:page>
                            <users:size>100</users:size>
                            <users:sortBy>username</users:sortBy>
                            <users:sortDir>ASC</users:sortDir>
                        </users:pagination>
                    </users:userRequest>
                </soap:Body>
            </soap:Envelope>
            """.formatted(authToken);
    }
    
    private String buildAddUserSoapRequest(String authToken, UserInfo userInfo) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:users="http://newsplatform.com/soap/users">
                <soap:Header/>
                <soap:Body>
                    <users:userRequest>
                        <users:operation>ADD</users:operation>
                        <users:authToken>%s</users:authToken>
                        <users:userData>
                            <users:username>%s</users:username>
                            <users:email>%s</users:email>
                            <users:password>%s</users:password>
                            <users:firstName>%s</users:firstName>
                            <users:lastName>%s</users:lastName>
                            <users:role>%s</users:role>
                            <users:active>true</users:active>
                        </users:userData>
                    </users:userRequest>
                </soap:Body>
            </soap:Envelope>
            """.formatted(authToken, userInfo.getUsername(), userInfo.getEmail(), 
                         userInfo.getPassword(), userInfo.getFirstName(), 
                         userInfo.getLastName(), userInfo.getRole());
    }
    
    private String buildDeleteUserSoapRequest(String authToken, String userId) {
        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
                          xmlns:users="http://newsplatform.com/soap/users">
                <soap:Header/>
                <soap:Body>
                    <users:userRequest>
                        <users:operation>DELETE</users:operation>
                        <users:authToken>%s</users:authToken>
                        <users:userId>%s</users:userId>
                    </users:userRequest>
                </soap:Body>
            </soap:Envelope>
            """.formatted(authToken, userId);
    }
    
    // ==================== PARSING DES RÉPONSES SOAP ====================
    
    private AuthenticationResponse parseLoginResponse(String xmlResponse) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        // Recherche du succès de l'authentification
        NodeList successNodes = doc.getElementsByTagName("success");
        if (successNodes.getLength() == 0) {
            throw new Exception("Réponse SOAP invalide");
        }
        
        boolean success = Boolean.parseBoolean(successNodes.item(0).getTextContent());
        
        if (success) {
            NodeList tokenNodes = doc.getElementsByTagName("accessToken");
            if (tokenNodes.getLength() > 0) {
                String token = tokenNodes.item(0).getTextContent();
                return new AuthenticationResponse(true, "Connexion réussie", token);
            } else {
                throw new Exception("Jeton d'accès manquant dans la réponse");
            }
        } else {
            NodeList messageNodes = doc.getElementsByTagName("message");
            String message = messageNodes.getLength() > 0 ? 
                messageNodes.item(0).getTextContent() : "Échec de l'authentification";
            return new AuthenticationResponse(false, message, null);
        }
    }
    
    private List<UserInfo> parseUserListResponse(String xmlResponse) throws Exception {
        List<UserInfo> users = new ArrayList<>();
        
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        NodeList userNodes = doc.getElementsByTagName("users");
        for (int i = 0; i < userNodes.getLength(); i++) {
            Element userElement = (Element) userNodes.item(i);
            
            UserInfo user = new UserInfo();
            user.setId(getElementValue(userElement, "id"));
            user.setUsername(getElementValue(userElement, "username"));
            user.setEmail(getElementValue(userElement, "email"));
            user.setFirstName(getElementValue(userElement, "firstName"));
            user.setLastName(getElementValue(userElement, "lastName"));
            user.setRole(getElementValue(userElement, "role"));
            user.setActive(Boolean.parseBoolean(getElementValue(userElement, "active")));
            
            users.add(user);
        }
        
        return users;
    }
    
    private UserInfo parseAddUserResponse(String xmlResponse) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        Element userElement = (Element) doc.getElementsByTagName("user").item(0);
        if (userElement == null) {
            throw new Exception("Utilisateur non trouvé dans la réponse");
        }
        
        UserInfo user = new UserInfo();
        user.setId(getElementValue(userElement, "id"));
        user.setUsername(getElementValue(userElement, "username"));
        user.setEmail(getElementValue(userElement, "email"));
        user.setFirstName(getElementValue(userElement, "firstName"));
        user.setLastName(getElementValue(userElement, "lastName"));
        user.setRole(getElementValue(userElement, "role"));
        user.setActive(Boolean.parseBoolean(getElementValue(userElement, "active")));
        
        return user;
    }
    
    private boolean parseDeleteUserResponse(String xmlResponse) throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlResponse)));
        
        NodeList successNodes = doc.getElementsByTagName("success");
        return successNodes.getLength() > 0 && 
               Boolean.parseBoolean(successNodes.item(0).getTextContent());
    }
    
    private String getElementValue(Element parent, String tagName) {
        NodeList nodeList = parent.getElementsByTagName(tagName);
        return nodeList.getLength() > 0 ? nodeList.item(0).getTextContent() : "";
    }
    
    // ==================== CLASSES DE DONNÉES ====================
    
    /**
     * Classe de réponse pour l'authentification
     */
    public static class AuthenticationResponse {
        private final boolean success;
        private final String message;
        private final String accessToken;
        
        public AuthenticationResponse(boolean success, String message, String accessToken) {
            this.success = success;
            this.message = message;
            this.accessToken = accessToken;
        }
        
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getAccessToken() { return accessToken; }
    }
    
    /**
     * Classe d'informations utilisateur pour SOAP
     */
    public static class UserInfo {
        private String id;
        private String username;
        private String email;
        private String password;
        private String firstName;
        private String lastName;
        private String role;
        private boolean active;
        
        // Constructeurs
        public UserInfo() {}
        
        public UserInfo(String username, String email, String password, 
                       String firstName, String lastName, String role) {
            this.username = username;
            this.email = email;
            this.password = password;
            this.firstName = firstName;
            this.lastName = lastName;
            this.role = role;
            this.active = true;
        }
        
        // Getters et Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }
        
        public String getFullName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return username;
        }
        
        public String getStatusText() {
            return active ? "Actif" : "Inactif";
        }
    }
} 
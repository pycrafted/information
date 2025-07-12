package com.newsplatformdesktopclient;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import java.util.List;

/**
 * Application Client JavaFX pour la gestion des utilisateurs.
 * Respecte les exigences du cahier des charges :
 * - Authentification via Web Service SOAP
 * - Interface GUI pour CRUD utilisateurs
 * - Appels sécurisés avec jetons d'authentification
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
public class NewsplatformUserManagementApp extends Application {
    
    private Stage primaryStage;
    private String authToken;
    private boolean isAuthenticated = false;
    
    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setTitle("Newsplatform - Gestion des Utilisateurs");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        
        // Afficher l'écran de connexion au démarrage
        showLoginScreen();
        
        primaryStage.show();
    }
    
    /**
     * Affiche l'écran de connexion SOAP
     */
    private void showLoginScreen() {
        VBox loginLayout = new VBox(20);
        loginLayout.setAlignment(Pos.CENTER);
        loginLayout.setPadding(new Insets(50));
        
        // Titre
        Label titleLabel = new Label("Connexion Administrateur");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Formulaire de connexion
        GridPane loginForm = new GridPane();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setHgap(10);
        loginForm.setVgap(10);
        
        Label usernameLabel = new Label("Nom d'utilisateur:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("admin");
        
        Label passwordLabel = new Label("Mot de passe:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Votre mot de passe");
        
        Button loginButton = new Button("Se connecter");
        loginButton.setDefaultButton(true);
        
        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-text-fill: red;");
        
        // Assemblage du formulaire
        loginForm.add(usernameLabel, 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(passwordLabel, 0, 1);
        loginForm.add(passwordField, 1, 1);
        loginForm.add(loginButton, 1, 2);
        
        loginLayout.getChildren().addAll(titleLabel, loginForm, statusLabel);
        
        // Action de connexion
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Veuillez remplir tous les champs");
                return;
            }
            
            // Tentative d'authentification via SOAP
            authenticateUser(username, password, statusLabel);
        });
        
        // Permettre la connexion avec Enter dans les champs
        usernameField.setOnAction(e -> loginButton.fire());
        passwordField.setOnAction(e -> loginButton.fire());
        
        Scene loginScene = new Scene(loginLayout, 400, 300);
        primaryStage.setScene(loginScene);
    }
    
    /**
     * Authentifie l'utilisateur via le service SOAP
     */
    private void authenticateUser(String username, String password, Label statusLabel) {
        try {
            statusLabel.setText("Connexion en cours...");
            statusLabel.setStyle("-fx-text-fill: blue;");
            
            // Appel réel au service SOAP d'authentification
            SOAPClientService soapService = new SOAPClientService();
            SOAPClientService.AuthenticationResponse response = soapService.authenticateUser(username, password);
            
            if (response.isSuccess()) {
                authToken = response.getAccessToken();
                isAuthenticated = true;
                
                statusLabel.setText("Connexion réussie !");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                // Passer à l'interface de gestion des utilisateurs
                showUserManagementScreen();
            } else {
                statusLabel.setText(response.getMessage());
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            
        } catch (Exception e) {
            statusLabel.setText("Erreur de connexion: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Affiche l'interface principale de gestion des utilisateurs
     */
    private void showUserManagementScreen() {
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(20));
        
        // Barre d'outils
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Label welcomeLabel = new Label("Gestion des Utilisateurs - Connecté");
        welcomeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        
        Button refreshButton = new Button("Actualiser");
        Button addUserButton = new Button("Ajouter Utilisateur");
        Button logoutButton = new Button("Déconnexion");
        
        toolbar.getChildren().addAll(welcomeLabel, new Label("   "), refreshButton, addUserButton, logoutButton);
        
        // Liste des utilisateurs
        TableView<UserDisplayModel> userTable = new TableView<>();
        
        TableColumn<UserDisplayModel, String> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        idColumn.setPrefWidth(200);
        
        TableColumn<UserDisplayModel, String> usernameColumn = new TableColumn<>("Nom d'utilisateur");
        usernameColumn.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());
        usernameColumn.setPrefWidth(150);
        
        TableColumn<UserDisplayModel, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
        emailColumn.setPrefWidth(200);
        
        TableColumn<UserDisplayModel, String> roleColumn = new TableColumn<>("Rôle");
        roleColumn.setCellValueFactory(cellData -> cellData.getValue().roleProperty());
        roleColumn.setPrefWidth(120);
        
        TableColumn<UserDisplayModel, String> statusColumn = new TableColumn<>("Statut");
        statusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusColumn.setPrefWidth(100);
        
        userTable.getColumns().addAll(idColumn, usernameColumn, emailColumn, roleColumn, statusColumn);
        
        // Boutons d'action sur les utilisateurs sélectionnés
        HBox actionButtons = new HBox(10);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button editButton = new Button("Modifier");
        Button deleteButton = new Button("Supprimer");
        Button changePasswordButton = new Button("Changer mot de passe");
        
        actionButtons.getChildren().addAll(editButton, deleteButton, changePasswordButton);
        
        // Assemblage de l'interface
        mainLayout.getChildren().addAll(toolbar, new Separator(), userTable, actionButtons);
        
        // Actions des boutons
        refreshButton.setOnAction(e -> loadUserList(userTable));
        addUserButton.setOnAction(e -> showAddUserDialog(userTable));
        editButton.setOnAction(e -> showEditUserDialog(userTable.getSelectionModel().getSelectedItem(), userTable));
        deleteButton.setOnAction(e -> deleteSelectedUser(userTable));
        changePasswordButton.setOnAction(e -> showChangePasswordDialog(userTable.getSelectionModel().getSelectedItem()));
        logoutButton.setOnAction(e -> {
            isAuthenticated = false;
            authToken = null;
            showLoginScreen();
        });
        
        // Charger la liste initiale
        loadUserList(userTable);
        
        Scene mainScene = new Scene(mainLayout, 900, 600);
        primaryStage.setScene(mainScene);
    }
    
    /**
     * Charge la liste des utilisateurs via SOAP
     */
    private void loadUserList(TableView<UserDisplayModel> userTable) {
        userTable.getItems().clear();
        
        try {
            // Appel réel au service SOAP listUsers
            SOAPClientService soapService = new SOAPClientService();
            List<SOAPClientService.UserInfo> users = soapService.getUserList(authToken);
            
            // Conversion en modèle d'affichage
            for (SOAPClientService.UserInfo user : users) {
                UserDisplayModel displayUser = new UserDisplayModel(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole(),
                    user.getStatusText()
                );
                userTable.getItems().add(displayUser);
            }
            
        } catch (Exception e) {
            // En cas d'erreur, afficher une alerte
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de chargement");
            alert.setHeaderText("Impossible de charger la liste des utilisateurs");
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    /**
     * Affiche la boîte de dialogue d'ajout d'utilisateur
     */
    private void showAddUserDialog(TableView<UserDisplayModel> userTable) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Ajouter Utilisateur");
        alert.setHeaderText("Fonctionnalité à implémenter");
        alert.setContentText("L'ajout d'utilisateur via SOAP sera implémenté prochainement.");
        alert.showAndWait();
    }
    
    /**
     * Affiche la boîte de dialogue de modification d'utilisateur
     */
    private void showEditUserDialog(UserDisplayModel user, TableView<UserDisplayModel> userTable) {
        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Veuillez sélectionner un utilisateur à modifier");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Modifier Utilisateur");
        alert.setHeaderText("Modification de: " + user.getUsername());
        alert.setContentText("La modification d'utilisateur via SOAP sera implémentée prochainement.");
        alert.showAndWait();
    }
    
    /**
     * Supprime l'utilisateur sélectionné
     */
    private void deleteSelectedUser(TableView<UserDisplayModel> userTable) {
        UserDisplayModel selectedUser = userTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Veuillez sélectionner un utilisateur à supprimer");
            alert.showAndWait();
            return;
        }
        
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmer la suppression");
        confirmation.setHeaderText("Supprimer l'utilisateur: " + selectedUser.getUsername());
        confirmation.setContentText("Cette action est irréversible. Continuer ?");
        
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Appel SOAP deleteUser
                    SOAPClientService soapService = new SOAPClientService();
                    boolean deleted = soapService.deleteUser(authToken, selectedUser.getId());
                    
                    if (deleted) {
                        userTable.getItems().remove(selectedUser);
                        
                        Alert success = new Alert(Alert.AlertType.INFORMATION);
                        success.setTitle("Suppression réussie");
                        success.setHeaderText("Utilisateur supprimé avec succès");
                        success.showAndWait();
                    } else {
                        Alert error = new Alert(Alert.AlertType.ERROR);
                        error.setTitle("Erreur de suppression");
                        error.setHeaderText("Impossible de supprimer l'utilisateur");
                        error.showAndWait();
                    }
                } catch (Exception e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Erreur de suppression");
                    error.setHeaderText("Erreur lors de la suppression");
                    error.setContentText("Erreur: " + e.getMessage());
                    error.showAndWait();
                }
            }
        });
    }
    
    /**
     * Affiche la boîte de dialogue de changement de mot de passe
     */
    private void showChangePasswordDialog(UserDisplayModel user) {
        if (user == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setHeaderText("Veuillez sélectionner un utilisateur");
            alert.showAndWait();
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Changer mot de passe");
        alert.setHeaderText("Changement pour: " + user.getUsername());
        alert.setContentText("Le changement de mot de passe via SOAP sera implémenté prochainement.");
        alert.showAndWait();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
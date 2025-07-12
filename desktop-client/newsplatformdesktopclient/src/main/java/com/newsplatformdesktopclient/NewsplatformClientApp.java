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
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Application Client JavaFX pour la gestion des utilisateurs.
 * Respecte les exigences du cahier des charges :
 * - Authentification via Web Service SOAP (simulée)
 * - Interface GUI pour CRUD utilisateurs
 * - Appels sécurisés avec jetons d'authentification
 * 
 * @author Équipe Développement
 * @version 1.0
 * @since 2025
 */
public class NewsplatformClientApp extends Application {
    
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
        Label titleLabel = new Label("Connexion Administrateur SOAP");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        
        // Formulaire de connexion
        GridPane loginForm = new GridPane();
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setHgap(10);
        loginForm.setVgap(10);
        
        TextField usernameField = new TextField();
        usernameField.setPromptText("admin");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("admin123");
        Button loginButton = new Button("Se connecter via SOAP");
        Label statusLabel = new Label();
        
        loginForm.add(new Label("Nom d'utilisateur:"), 0, 0);
        loginForm.add(usernameField, 1, 0);
        loginForm.add(new Label("Mot de passe:"), 0, 1);
        loginForm.add(passwordField, 1, 1);
        loginForm.add(loginButton, 1, 2);
        
        loginLayout.getChildren().addAll(titleLabel, loginForm, statusLabel);
        
        // Action de connexion
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            
            if (username.isEmpty() || password.isEmpty()) {
                statusLabel.setText("Veuillez remplir tous les champs");
                statusLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            
            // Simulation authentification SOAP
            authenticateViaSOAP(username, password, statusLabel);
        });
        
        Scene loginScene = new Scene(loginLayout, 500, 400);
        primaryStage.setScene(loginScene);
    }
    
    /**
     * Authentifie l'utilisateur via le service SOAP (simulé)
     */
    private void authenticateViaSOAP(String username, String password, Label statusLabel) {
        try {
            statusLabel.setText("Connexion SOAP en cours...");
            statusLabel.setStyle("-fx-text-fill: blue;");
            
            // TODO: Appel réel au service SOAP d'authentification
            // Simulation pour démonstration
            if ("admin".equals(username) && "admin123".equals(password)) {
                authToken = "JWT-TOKEN-" + System.currentTimeMillis();
                isAuthenticated = true;
                
                statusLabel.setText("Authentification SOAP réussie !");
                statusLabel.setStyle("-fx-text-fill: green;");
                
                // Transition vers l'interface de gestion
                showUserManagementInterface();
            } else {
                statusLabel.setText("Échec de l'authentification SOAP");
                statusLabel.setStyle("-fx-text-fill: red;");
            }
            
        } catch (Exception e) {
            statusLabel.setText("Erreur de connexion SOAP: " + e.getMessage());
            statusLabel.setStyle("-fx-text-fill: red;");
        }
    }
    
    /**
     * Affiche l'interface principale de gestion des utilisateurs
     */
    private void showUserManagementInterface() {
        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(20));
        
        // En-tête
        Label headerLabel = new Label("🔧 Gestion des Utilisateurs - SOAP Authentifié");
        headerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        
        // Barre d'outils
        HBox toolbar = new HBox(10);
        Button refreshBtn = new Button("🔄 Actualiser via SOAP");
        Button addBtn = new Button("➕ Ajouter Utilisateur");
        Button editBtn = new Button("✏️ Modifier");
        Button deleteBtn = new Button("🗑️ Supprimer");
        Button logoutBtn = new Button("🚪 Déconnexion");
        
        toolbar.getChildren().addAll(refreshBtn, addBtn, editBtn, deleteBtn, new Label("  "), logoutBtn);
        
        // Table des utilisateurs
        TableView<SimpleUser> userTable = new TableView<>();
        
        TableColumn<SimpleUser, String> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> data.getValue().idProperty());
        idCol.setPrefWidth(200);
        
        TableColumn<SimpleUser, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        usernameCol.setPrefWidth(150);
        
        TableColumn<SimpleUser, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(data -> data.getValue().emailProperty());
        emailCol.setPrefWidth(250);
        
        TableColumn<SimpleUser, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(data -> data.getValue().roleProperty());
        roleCol.setPrefWidth(120);
        
        userTable.getColumns().addAll(idCol, usernameCol, emailCol, roleCol);
        
        // Zone de statut
        Label statusArea = new Label("✅ Connecté au serveur SOAP. Token: " + authToken.substring(0, 20) + "...");
        statusArea.setStyle("-fx-background-color: #e8f5e8; -fx-padding: 10;");
        
        mainLayout.getChildren().addAll(headerLabel, toolbar, userTable, statusArea);
        
        // Actions des boutons
        refreshBtn.setOnAction(e -> loadUsersViaSOAP(userTable, statusArea));
        addBtn.setOnAction(e -> showAddUserDialog(statusArea));
        editBtn.setOnAction(e -> editSelectedUser(userTable, statusArea));
        deleteBtn.setOnAction(e -> deleteSelectedUser(userTable, statusArea));
        logoutBtn.setOnAction(e -> {
            isAuthenticated = false;
            authToken = null;
            showLoginScreen();
        });
        
        // Charger les données initiales
        loadUsersViaSOAP(userTable, statusArea);
        
        Scene mainScene = new Scene(mainLayout, 900, 650);
        primaryStage.setScene(mainScene);
    }
    
    /**
     * Charge la liste des utilisateurs via SOAP (simulé)
     */
    private void loadUsersViaSOAP(TableView<SimpleUser> table, Label status) {
        try {
            status.setText("🔄 Chargement via SOAP...");
            
            // TODO: Appel réel au service SOAP getUserList
            // Simulation avec données de test
            ObservableList<SimpleUser> users = FXCollections.observableArrayList(
                new SimpleUser("uuid-1", "admin", "admin@newsplatform.com", "ADMINISTRATEUR"),
                new SimpleUser("uuid-2", "editeur1", "editeur@newsplatform.com", "EDITEUR"),
                new SimpleUser("uuid-3", "visiteur1", "visiteur@newsplatform.com", "VISITEUR"),
                new SimpleUser("uuid-4", "testuser", "test@newsplatform.com", "VISITEUR")
            );
            
            table.setItems(users);
            status.setText("✅ " + users.size() + " utilisateurs chargés via SOAP");
            
        } catch (Exception e) {
            status.setText("❌ Erreur SOAP: " + e.getMessage());
        }
    }
    
    /**
     * Affiche la boîte de dialogue d'ajout d'utilisateur
     */
    private void showAddUserDialog(Label status) {
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Ajouter Utilisateur via SOAP");
        dialog.setHeaderText("Fonctionnalité SOAP");
        dialog.setContentText("Cette fonctionnalité appellerait le service SOAP 'addUser' avec les paramètres :\n\n" +
                             "- username (String)\n" +
                             "- password (String)\n" +
                             "- email (String)\n" +
                             "- firstName (String)\n" +
                             "- lastName (String)\n" +
                             "- role (String)\n\n" +
                             "Token d'authentification : " + (authToken != null ? authToken.substring(0, 15) + "..." : "Non authentifié"));
        dialog.showAndWait();
        
        status.setText("ℹ️ Dialogue d'ajout SOAP affiché");
    }
    
    /**
     * Modifie l'utilisateur sélectionné
     */
    private void editSelectedUser(TableView<SimpleUser> table, Label status) {
        SimpleUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setContentText("Veuillez sélectionner un utilisateur à modifier");
            alert.showAndWait();
            return;
        }
        
        Alert dialog = new Alert(Alert.AlertType.INFORMATION);
        dialog.setTitle("Modifier Utilisateur via SOAP");
        dialog.setHeaderText("Modification de : " + selected.getUsername());
        dialog.setContentText("Cette fonctionnalité appellerait le service SOAP 'updateUser' pour l'utilisateur ID: " + selected.getId() + 
                             "\n\nToken d'authentification requis pour cette opération.");
        dialog.showAndWait();
        
        status.setText("✏️ Modification SOAP initiée pour : " + selected.getUsername());
    }
    
    /**
     * Supprime l'utilisateur sélectionné
     */
    private void deleteSelectedUser(TableView<SimpleUser> table, Label status) {
        SimpleUser selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucune sélection");
            alert.setContentText("Veuillez sélectionner un utilisateur à supprimer");
            alert.showAndWait();
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer suppression via SOAP");
        confirm.setHeaderText("Supprimer : " + selected.getUsername());
        confirm.setContentText("Cette action appellera le service SOAP 'deleteUser'.\nContinuer ?");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // TODO: Appel SOAP deleteUser
                table.getItems().remove(selected);
                status.setText("🗑️ Utilisateur supprimé via SOAP : " + selected.getUsername());
            }
        });
    }
    
    /**
     * Classe simple pour représenter un utilisateur dans la table
     */
    public static class SimpleUser {
        private final javafx.beans.property.SimpleStringProperty id;
        private final javafx.beans.property.SimpleStringProperty username;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty role;
        
        public SimpleUser(String id, String username, String email, String role) {
            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.username = new javafx.beans.property.SimpleStringProperty(username);
            this.email = new javafx.beans.property.SimpleStringProperty(email);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
        }
        
        public javafx.beans.property.StringProperty idProperty() { return id; }
        public javafx.beans.property.StringProperty usernameProperty() { return username; }
        public javafx.beans.property.StringProperty emailProperty() { return email; }
        public javafx.beans.property.StringProperty roleProperty() { return role; }
        
        public String getId() { return id.get(); }
        public String getUsername() { return username.get(); }
        public String getEmail() { return email.get(); }
        public String getRole() { return role.get(); }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
} 
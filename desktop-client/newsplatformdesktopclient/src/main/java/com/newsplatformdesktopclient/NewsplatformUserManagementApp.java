package com.newsplatformdesktopclient;

import javafx.application.Application;
import javafx.application.Platform;
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
import java.util.regex.Pattern;

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
        // Créer un Dialog personnalisé pour l'ajout d'utilisateur
        Dialog<SOAPClientService.UserInfo> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Créer un nouvel utilisateur");
        
        // Boutons OK et Cancel
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
        
        // Création du formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Champs du formulaire
        TextField usernameField = new TextField();
        usernameField.setPromptText("Nom d'utilisateur");
        
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        
        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Prénom");
        
        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Nom");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("USER", "EDITOR", "ADMIN");
        roleComboBox.setValue("USER"); // Valeur par défaut
        
        CheckBox activeCheckBox = new CheckBox("Compte actif");
        activeCheckBox.setSelected(true); // Actif par défaut
        
        // Assemblage du formulaire
        grid.add(new Label("Nom d'utilisateur:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Mot de passe:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Prénom:"), 0, 3);
        grid.add(firstNameField, 1, 3);
        grid.add(new Label("Nom:"), 0, 4);
        grid.add(lastNameField, 1, 4);
        grid.add(new Label("Rôle:"), 0, 5);
        grid.add(roleComboBox, 1, 5);
        grid.add(activeCheckBox, 1, 6);
        
        // Validation en temps réel
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);
        
        // Écouter les changements pour activer/désactiver le bouton Ajouter
        Runnable validateForm = () -> {
            boolean isValid = !usernameField.getText().trim().isEmpty() &&
                            !emailField.getText().trim().isEmpty() &&
                            !passwordField.getText().isEmpty() &&
                            !firstNameField.getText().trim().isEmpty() &&
                            !lastNameField.getText().trim().isEmpty() &&
                            roleComboBox.getValue() != null &&
                            isValidEmail(emailField.getText().trim()) &&
                            isValidPassword(passwordField.getText());
            addButton.setDisable(!isValid);
        };
        
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        
        dialog.getDialogPane().setContent(grid);
        
        // Conversion du résultat en UserInfo
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                SOAPClientService.UserInfo userInfo = new SOAPClientService.UserInfo(
                    usernameField.getText().trim(),
                    emailField.getText().trim(),
                    passwordField.getText(),
                    firstNameField.getText().trim(),
                    lastNameField.getText().trim(),
                    roleComboBox.getValue()
                );
                userInfo.setActive(activeCheckBox.isSelected());
                return userInfo;
            }
            return null;
        });
        
        // Focus sur le premier champ
        Platform.runLater(() -> usernameField.requestFocus());
        
        // Traitement du résultat
        dialog.showAndWait().ifPresent(userInfo -> {
            try {
                // Appel SOAP pour ajouter l'utilisateur
                SOAPClientService soapService = new SOAPClientService();
                SOAPClientService.UserInfo createdUser = soapService.addUser(authToken, userInfo);
                
                // Succès - Rafraîchir la liste
                loadUserList(userTable);
                
                // Afficher message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Utilisateur ajouté");
                successAlert.setHeaderText("Succès");
                successAlert.setContentText("L'utilisateur '" + createdUser.getUsername() + 
                                          "' a été créé avec succès.\nID: " + createdUser.getId());
                successAlert.showAndWait();
                
                // Sélectionner le nouvel utilisateur dans la liste
                selectUserInTable(userTable, createdUser.getId());
                
            } catch (Exception e) {
                // Gestion des erreurs
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur d'ajout");
                errorAlert.setHeaderText("Impossible d'ajouter l'utilisateur");
                
                String errorMessage = e.getMessage();
                if (errorMessage.contains("duplicate") || errorMessage.contains("already exists")) {
                    errorAlert.setContentText("Un utilisateur avec ce nom ou email existe déjà.");
                } else if (errorMessage.contains("validation")) {
                    errorAlert.setContentText("Données invalides. Vérifiez les champs.");
                } else if (errorMessage.contains("authentication") || errorMessage.contains("token")) {
                    errorAlert.setContentText("Session expirée. Veuillez vous reconnecter.");
                } else {
                    errorAlert.setContentText("Erreur: " + errorMessage);
                }
                
                errorAlert.showAndWait();
            }
        });
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
        
        // Récupérer les données complètes de l'utilisateur via SOAP
        SOAPClientService.UserInfo fullUserInfo = null;
        try {
            SOAPClientService soapService = new SOAPClientService();
            List<SOAPClientService.UserInfo> allUsers = soapService.getUserList(authToken);
            
            // Chercher l'utilisateur par ID pour récupérer toutes ses données
            for (SOAPClientService.UserInfo userInfo : allUsers) {
                if (user.getId().equals(userInfo.getId())) {
                    fullUserInfo = userInfo;
                    break;
                }
            }
            
            if (fullUserInfo == null) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Utilisateur introuvable");
                errorAlert.setContentText("Impossible de récupérer les données de l'utilisateur.");
                errorAlert.showAndWait();
                return;
            }
            
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur de chargement");
            errorAlert.setHeaderText("Impossible de récupérer les données utilisateur");
            errorAlert.setContentText("Erreur: " + e.getMessage());
            errorAlert.showAndWait();
            return;
        }
        
        // Créer le dialog de modification avec données pré-remplies
        Dialog<SOAPClientService.UserInfo> dialog = new Dialog<>();
        dialog.setTitle("Modifier un utilisateur");
        dialog.setHeaderText("Modification de: " + fullUserInfo.getUsername());
        
        // Boutons Sauvegarder et Annuler
        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Création du formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Champs du formulaire pré-remplis
        TextField usernameField = new TextField(fullUserInfo.getUsername());
        usernameField.setDisable(true); // Username non modifiable
        usernameField.setStyle("-fx-opacity: 0.6;");
        
        TextField emailField = new TextField(fullUserInfo.getEmail() != null ? fullUserInfo.getEmail() : "");
        emailField.setPromptText("Email");
        
        TextField firstNameField = new TextField(fullUserInfo.getFirstName() != null ? fullUserInfo.getFirstName() : "");
        firstNameField.setPromptText("Prénom");
        
        TextField lastNameField = new TextField(fullUserInfo.getLastName() != null ? fullUserInfo.getLastName() : "");
        lastNameField.setPromptText("Nom");
        
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("USER", "EDITOR", "ADMIN");
        roleComboBox.setValue(fullUserInfo.getRole() != null ? fullUserInfo.getRole() : "USER");
        
        CheckBox activeCheckBox = new CheckBox("Compte actif");
        activeCheckBox.setSelected(fullUserInfo.isActive());
        
        // Assemblage du formulaire
        grid.add(new Label("Nom d'utilisateur:"), 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Prénom:"), 0, 2);
        grid.add(firstNameField, 1, 2);
        grid.add(new Label("Nom:"), 0, 3);
        grid.add(lastNameField, 1, 3);
        grid.add(new Label("Rôle:"), 0, 4);
        grid.add(roleComboBox, 1, 4);
        grid.add(activeCheckBox, 1, 5);
        
        // Validation en temps réel
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Validation et détection des changements
        SOAPClientService.UserInfo originalData = fullUserInfo; // Référence pour comparaison
        
        Runnable validateForm = () -> {
            boolean isValid = !emailField.getText().trim().isEmpty() &&
                            !firstNameField.getText().trim().isEmpty() &&
                            !lastNameField.getText().trim().isEmpty() &&
                            roleComboBox.getValue() != null &&
                            isValidEmail(emailField.getText().trim());
            
            // Vérifier si des changements ont été effectués
            boolean hasChanges = !emailField.getText().trim().equals(originalData.getEmail() != null ? originalData.getEmail() : "") ||
                               !firstNameField.getText().trim().equals(originalData.getFirstName() != null ? originalData.getFirstName() : "") ||
                               !lastNameField.getText().trim().equals(originalData.getLastName() != null ? originalData.getLastName() : "") ||
                               !roleComboBox.getValue().equals(originalData.getRole()) ||
                               activeCheckBox.isSelected() != originalData.isActive();
            
            saveButton.setDisable(!isValid || !hasChanges);
        };
        
        // Écouter les changements pour validation
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        roleComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        activeCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        
        // Validation initiale
        validateForm.run();
        
        dialog.getDialogPane().setContent(grid);
        
        // Conversion du résultat en UserInfo modifié
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                SOAPClientService.UserInfo updatedInfo = new SOAPClientService.UserInfo();
                updatedInfo.setId(originalData.getId());
                updatedInfo.setUsername(originalData.getUsername()); // Username non modifiable
                updatedInfo.setEmail(emailField.getText().trim());
                updatedInfo.setFirstName(firstNameField.getText().trim());
                updatedInfo.setLastName(lastNameField.getText().trim());
                updatedInfo.setRole(roleComboBox.getValue());
                updatedInfo.setActive(activeCheckBox.isSelected());
                return updatedInfo;
            }
            return null;
        });
        
        // Focus sur le premier champ modifiable
        Platform.runLater(() -> emailField.requestFocus());
        
        // Traitement du résultat
        dialog.showAndWait().ifPresent(updatedUserInfo -> {
            try {
                // Appel SOAP pour modifier l'utilisateur
                SOAPClientService soapService = new SOAPClientService();
                SOAPClientService.UserInfo modifiedUser = soapService.updateUser(authToken, updatedUserInfo.getId(), updatedUserInfo);
                
                // Succès - Rafraîchir la liste
                loadUserList(userTable);
                
                // Afficher message de succès
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Utilisateur modifié");
                successAlert.setHeaderText("Succès");
                successAlert.setContentText("L'utilisateur '" + modifiedUser.getUsername() + 
                                          "' a été modifié avec succès.");
                successAlert.showAndWait();
                
                // Maintenir la sélection sur l'utilisateur modifié
                selectUserInTable(userTable, modifiedUser.getId());
                
            } catch (Exception e) {
                // Gestion des erreurs spécifiques
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur de modification");
                errorAlert.setHeaderText("Impossible de modifier l'utilisateur");
                
                String errorMessage = e.getMessage();
                if (errorMessage.contains("duplicate") || errorMessage.contains("already exists")) {
                    errorAlert.setContentText("Un utilisateur avec cet email existe déjà.");
                } else if (errorMessage.contains("validation")) {
                    errorAlert.setContentText("Données invalides. Vérifiez les champs.");
                } else if (errorMessage.contains("authentication") || errorMessage.contains("token")) {
                    errorAlert.setContentText("Session expirée. Veuillez vous reconnecter.");
                } else if (errorMessage.contains("not found") || errorMessage.contains("inexistant")) {
                    errorAlert.setContentText("L'utilisateur n'existe plus sur le serveur.");
                } else {
                    errorAlert.setContentText("Erreur: " + errorMessage);
                }
                
                errorAlert.showAndWait();
            }
        });
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
    
    /**
     * Valide le format d'une adresse email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Regex simple mais efficace pour la validation email
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email.trim());
    }
    
    /**
     * Valide la robustesse d'un mot de passe
     */
    private boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        // Vérifier qu'il contient au moins :
        // - 1 minuscule, 1 majuscule, 1 chiffre
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        
        return hasLower && hasUpper && hasDigit;
    }
    
    /**
     * Sélectionne un utilisateur dans la table par son ID
     */
    private void selectUserInTable(TableView<UserDisplayModel> userTable, String userId) {
        if (userId == null) return;
        
        for (UserDisplayModel user : userTable.getItems()) {
            if (userId.equals(user.getId())) {
                userTable.getSelectionModel().select(user);
                userTable.scrollTo(user);
                break;
            }
        }
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
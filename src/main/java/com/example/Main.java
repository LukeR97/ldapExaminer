package com.example;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Pos;
import javafx.stage.Stage;

public class Main extends Application {

    private Label statusLabel;
    private TextField serverAddressField;
    private TextField bindDNAddressField;
    private TextField passwordField;

    @Override
    public void start(Stage primaryStage) {
        serverAddressField = new TextField();
        serverAddressField.setPromptText("LDAP Server Address");

        bindDNAddressField = new TextField();
        bindDNAddressField.setPromptText("LDAP bindDN");

        passwordField = new TextField();
        passwordField.setPromptText("Password");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            String serverAddress = serverAddressField.getText();
            String bindDn = bindDNAddressField.getText();
            // NEED TO SET PASSWORD STYLING HERE
            String password = passwordField.getText();
            connectToLDAPServer(serverAddress, bindDn, password);
        });

        statusLabel = new Label("\n To connect to your ldap server. Please provide the following: - \n Server Address \n bindDN \n password");
        statusLabel.setWrapText(true);

        VBox inputBox = new VBox(20);
        inputBox.setAlignment(Pos.TOP_CENTER);
        inputBox.getChildren().addAll(serverAddressField, bindDNAddressField, passwordField, connectButton);

        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.getChildren().addAll(inputBox, createSpacer(), statusLabel);

        StackPane root = new StackPane();
        root.getChildren().addAll(mainBox);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LDAP Connection Status");
        primaryStage.show();
    }

    private Label createSpacer() {
        Label spacer = new Label();
        spacer.setMinHeight(10);
        return spacer;
    }

    private void connectToLDAPServer(String serverAddress, String bindDn, String password){
        try {
            //cn=admin,dc=example,dc=com
            LDAPConnection ldapConnection = new LDAPConnection(serverAddress, 389, bindDn, password);
            Stage primaryStage = (Stage) serverAddressField.getScene().getWindow();
            primaryStage.close();
            showSearchScreen(primaryStage);
            ldapConnection.close();
        } catch(LDAPException e){
            statusLabel.setText("Failed to connect to LDAP Server " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MOVE FUNCTION
    private void showSearchScreen(Stage primaryStatge) {
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");
        Button searchButton = new Button("Search");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(searchBar, searchButton);

        Scene searchScene = new Scene(vbox, 300, 250);
        primaryStatge.setScene(searchScene);
        primaryStatge.setTitle("LDAP Search");
        primaryStatge.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
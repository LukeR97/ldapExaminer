package com.example;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    private Label statusLabel;
    private TextField serverAddressField;
    private TextField passwordField;

    @Override
    public void start(Stage primaryStage) {
        serverAddressField = new TextField();
        serverAddressField.setPromptText("LDAP Server Address");

        passwordField = new TextField();
        passwordField.setPromptText("Password");

        Button connectButton = new Button("Connect");
        connectButton.setOnAction(e -> {
            String serverAddress = serverAddressField.getText();
            // NEED TO SET PASSWORD STYLING HERE
            String password = passwordField.getText();
            connectToLDAPServer(serverAddress, password);
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(serverAddressField, passwordField, connectButton);

        statusLabel = new Label("Connecting to LDAP server...");
        statusLabel.setWrapText(true);

        StackPane root = new StackPane();
        root.getChildren().addAll(vbox, statusLabel);

        Scene scene = new Scene(root, 300, 250);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LDAP Connection Status");
        primaryStage.show();
    }

    private void connectToLDAPServer(String serverAddress, String password){
        try {
            LDAPConnection ldapConnection = new LDAPConnection(serverAddress, 389, "cn=admin,dc=example,dc=com", password);
            //LOOK AT POSSIBLY JUST USING ORIGINAL SCREEN INSTEAD?
            ((Stage) serverAddressField.getScene().getWindow()).close();
            showSearchScreen();
            ldapConnection.close();
        } catch(LDAPException e){
            statusLabel.setText("Failed to connect to LDAP Server " + e.getMessage());
            e.printStackTrace();
        }
    }

    // MOVE FUNCTION
    private void showSearchScreen() {
        Stage searchStage = new Stage();

        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");
        Button searchButton = new Button("Search");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(searchBar, searchButton);

        Scene searchScene = new Scene(vbox, 300, 250);
        searchStage.setScene(searchScene);
        searchStage.setTitle("LDAP Search");
        searchStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
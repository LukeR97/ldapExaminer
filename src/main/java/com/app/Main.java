package com.app;

import java.util.Collection;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.SearchResult;
import com.unboundid.ldap.sdk.SearchResultEntry;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
            try{
                LDAPConnection ldapConnection = Ldap.connectToLDAPServer(serverAddress, 389, bindDn, password);
                getSearchScreen(ldapConnection);
            } catch (LDAPException er){
                er.printStackTrace();
            }
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

    // MOVE FUNCTION
    private void showSearchScreen(Stage primaryStatge, LDAPConnection ldapConnection) {
        TextField searchBar = new TextField();
        searchBar.setPromptText("Search");
        Button searchButton = new Button("Search");
        Button analyzeButton = new Button("Analyze");
        VBox resultBox = new VBox(10);

        searchButton.setOnAction(e -> {
            String username = searchBar.getText();
            com.unboundid.ldap.sdk.SearchResult searchResult = Ldap.searchLDAP(username, ldapConnection);
            com.unboundid.ldap.sdk.SearchResultEntry entry = searchResult.getSearchEntries().get(0);
            System.out.println(searchResult.getSearchEntries().get(0));
            resultBox.getChildren().clear();
            getAttributes(entry, resultBox);
            resultBox.getChildren().add(createSpacer());
            resultBox.getChildren().add(analyzeButton);
            
            analyzeButton.setOnAction(event -> {
                List<Attribute> analyzeResults = Ldap.analyzeLDAP(entry, ldapConnection);
                resultBox.getChildren().removeIf(node -> node instanceof Label);
                for(Attribute attribute : analyzeResults){
                    Label resultLabel = new Label(attribute.getName() + ": " + attribute.getValue());
                    resultBox.getChildren().add(resultLabel);
                }
                System.out.println(analyzeResults);
            });
        });

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(searchBar, searchButton, resultBox);

        Scene searchScene = new Scene(vbox, 600, 500);
        primaryStatge.setScene(searchScene);
        primaryStatge.setTitle("LDAP Search");
        primaryStatge.show();
    }

    private void getSearchScreen(LDAPConnection ldapConnection){
        Stage primaryStage = (Stage) serverAddressField.getScene().getWindow();
        primaryStage.close();
        showSearchScreen(primaryStage, ldapConnection);
    }

    private void getAttributes(com.unboundid.ldap.sdk.SearchResultEntry entry, VBox resultBox){
        Collection<com.unboundid.ldap.sdk.Attribute> attributes = entry.getAttributes();
        for (com.unboundid.ldap.sdk.Attribute attribute : attributes){
            Label label = new Label(attribute.getName() + ": ");
            String value = attribute.getValue();
            if(value != null && !value.isEmpty()){
                label.setText(label.getText() + value);
            } else {
                label.setText(label.getText() + "N/A");
            }
            resultBox.getChildren().add(label);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
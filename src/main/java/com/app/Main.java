package com.app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.unboundid.ldap.sdk.Attribute;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
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

        VBox.setMargin(inputBox, new Insets(20));

        VBox mainBox = new VBox(10);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.getChildren().addAll(inputBox, createSpacer(), statusLabel);

        StackPane root = new StackPane();
        root.getChildren().addAll(mainBox);

        Scene scene = new Scene(root, 500, 350);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LDAP Examiner Connect");
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

        TableView<Attribute> attributeTable = new TableView<>();
        attributeTable.setEditable(false);

        TableColumn<Attribute, String> attributeNameCol = new TableColumn<>("Attribute Name");
        attributeNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Attribute, String> attributeValueCol = new TableColumn<>("Value");
        attributeValueCol.setCellValueFactory(new PropertyValueFactory<>("value"));

        //attributeTable.getColumns().addAll(attributeNameCol, attributeValueCol);
        attributeTable.getColumns().add(attributeNameCol);
        attributeTable.getColumns().add(attributeValueCol);

        VBox resultBox = new VBox(10);

        searchButton.setOnAction(e -> {
            String username = searchBar.getText();
            com.unboundid.ldap.sdk.SearchResult searchResult = Ldap.searchLDAP(username, ldapConnection);
            com.unboundid.ldap.sdk.SearchResultEntry entry = searchResult.getSearchEntries().get(0);
            //System.out.println(searchResult.getSearchEntries().get(0));
            ObservableList<Attribute> attributes = FXCollections.observableArrayList(getAttributes(entry));
            attributeTable.setItems(attributes);
            
            analyzeButton.setOnAction(event -> {
                List<Attribute> analyzeResults = Ldap.analyzeLDAP(entry, ldapConnection);
                attributes.add(new Attribute("-------Analyze Results Below-------", "-------"));
                attributes.addAll(analyzeResults);
            });
        });

        resultBox.getChildren().addAll(searchBar, searchButton, attributeTable, analyzeButton);

        VBox.setMargin(searchBar, new Insets(20));
        VBox.setMargin(searchButton, new Insets(20));
        VBox.setMargin(resultBox, new Insets(20));

        ScrollPane scrollPane = new ScrollPane(resultBox);
        scrollPane.setFitToWidth(true);

        VBox root = new VBox(10);
        root.getChildren().addAll(scrollPane);

        Scene searchScene = new Scene(root, 600, 500);
        primaryStatge.setScene(searchScene);
        primaryStatge.setTitle("LDAP Search");
        primaryStatge.show();
    }

    private void getSearchScreen(LDAPConnection ldapConnection){
        Stage primaryStage = (Stage) serverAddressField.getScene().getWindow();
        primaryStage.close();
        showSearchScreen(primaryStage, ldapConnection);
    }

    private List<Attribute> getAttributes(com.unboundid.ldap.sdk.SearchResultEntry entry) {
    List<Attribute> attributes = new ArrayList<>();
    Collection<com.unboundid.ldap.sdk.Attribute> ldapAttributes = entry.getAttributes();
    for (com.unboundid.ldap.sdk.Attribute ldapAttribute : ldapAttributes) {
        Attribute attribute = new Attribute(ldapAttribute.getName(), ldapAttribute.getValue() != null ? ldapAttribute.getValue() : "N/A");
        attributes.add(attribute);
    }
    return attributes;
}

    public static void main(String[] args) {
        launch(args);
    }
}

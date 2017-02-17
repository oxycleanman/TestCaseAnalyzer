package com.analyzer.main;

import com.analyzer.data.DataHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.event.ChangeEvent;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MainWindowController {

    @FXML
    private TextArea testCaseName;
    @FXML
    private VBox leftPanel;
    @FXML
    private Label vdiName;
    @FXML
    private Label datasheetName;
    @FXML
    private BorderPane mainWindow;
    @FXML
    private MenuItem configMenuItem;
    @FXML
    private MenuItem exitMenuItem;
    @FXML
    private Button searchButton;
    @FXML
    private Label errorMessage;
    @FXML
    private ListView<String> keywordPanel;
    @FXML
    private VBox rightPanel;
    @FXML
    private ListView<String> dataPanel;
    @FXML
    private VBox centerDetailPanel;
    @FXML
    private Button saveChangesToDataButton;
    @FXML
    private ListView<String> recentUpdates;
    @FXML
    private Button addNewKeywordButton;
    @FXML
    private Button deleteKeywordButton;
    @FXML
    private Button revertChangesButton;
    @FXML
    private HBox bottomPanel;
    @FXML
    private ProgressBar loadingBar;

    public ObservableMap<String, ObservableList<String>> results;

    public void initialize() {
        dataPanel.setCellFactory(dataPanel -> new DataCell());
        keywordPanel.setCellFactory(keywordPanel -> new KeywordCell());

        Task loadKeywords = DataHelper.loadKeywords();
        loadKeywords.setOnSucceeded(e -> loadingBar.setVisible(false));
        loadingBar.progressProperty().bind(loadKeywords.progressProperty());

        new Thread(loadKeywords).start();
    }

    @FXML
    public void buttonHandler(ActionEvent event) throws IOException {
        //Handle search button click
        if (event.getSource().equals(searchButton) || event.getSource().equals(revertChangesButton)) {
            this.vdiName.setText("");
            this.datasheetName.setText("");

            Service<ObservableMap<String, ObservableList<String>>> searchService = DataHelper.getTestData(this.testCaseName.getText());
            loadingBar.progressProperty().unbind();
            loadingBar.setVisible(true);
            loadingBar.progressProperty().bind(searchService.progressProperty());

            searchService.setOnSucceeded(e -> {
                this.results = searchService.getValue();
                loadingBar.setVisible(false);
                if (results.containsKey("errors")) {
                    errorMessage.setText(results.get("errors").get(0));
                    this.leftPanel.setVisible(false);
                    this.rightPanel.setVisible(false);
                    this.centerDetailPanel.setVisible(false);
                    this.bottomPanel.setVisible(false);
                } else if (results.get("vdi").size() > 1) {
                    errorMessage.setText("Multiple test cases found, please narrow search criteria");
                    this.leftPanel.setVisible(false);
                    this.rightPanel.setVisible(false);
                    this.centerDetailPanel.setVisible(false);
                    this.bottomPanel.setVisible(false);
                } else {
                    errorMessage.setText("");
                    String vdiName = "";
                    String datasheet = results.get("datasheet").get(0);
                    for (String s : results.get("vdi")) {
                        vdiName += s + ", ";
                    }
                    vdiName = vdiName.substring(0, vdiName.length() - 2);
                    this.keywordPanel.setItems(results.get("keywords"));
                    //Sort the fields going into dataPanel
                    ObservableList<String> tempList = FXCollections.observableArrayList();
                    tempList.addAll(results.get("data").sorted((string1, string2) -> string1.compareToIgnoreCase(string2)));
                    results.put("data", tempList);
                    this.dataPanel.setItems(results.get("data"));
                    this.vdiName.setText(vdiName);
                    this.datasheetName.setText(datasheet);
                    this.leftPanel.setVisible(true);
                    this.rightPanel.setVisible(true);
                    this.centerDetailPanel.setVisible(true);
                    this.recentUpdates.setVisible(true);
                    this.bottomPanel.setVisible(true);
                }
            });

            searchService.start();
        } else if (event.getSource().equals(saveChangesToDataButton)) {
            //Handle save changes button click on the Data panel
            Service<Boolean> writeDataService = DataHelper.writeTestData(this.results);
            loadingBar.setVisible(true);
            loadingBar.progressProperty().unbind();
            loadingBar.progressProperty().bind(writeDataService.progressProperty());
            writeDataService.setOnSucceeded(e -> {
                loadingBar.setVisible(false);
                this.recentUpdates.setItems(DataHelper.updatedDataSheets);
            });
            writeDataService.start();
        } else if (event.getSource().equals(deleteKeywordButton)) {
            //Handle delete keyword button click
            this.results.get("keywords").remove(this.keywordPanel.getSelectionModel().getSelectedIndex());
        }
    }

    @FXML
    public void showConfigDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("configWindow.fxml"));
        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            ConfigWindowController controller = loader.getController();
            Map<String, String> configSettings = new HashMap<>();
            configSettings.put("directory", controller.getDirectoryInput().getText());
            DataHelper.writeConfig(configSettings);
            DataHelper.directory = controller.getDirectoryInput().getText();
        } else {
            dialog.close();
        }
    }

    @FXML
    public void showKeywordPicker() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("KeywordWindow.fxml"));

        try {
            dialog.getDialogPane().setContent(loader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }

        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            KeywordWindowController controller = loader.getController();
            this.keywordPanel.getItems().add(controller.keywordListPanel.getSelectionModel().getSelectedItem());
            this.keywordPanel.getSelectionModel().select(controller.keywordListPanel.getSelectionModel().getSelectedItem());
            this.keywordPanel.requestFocus();
            dialog.close();
        } else {
            dialog.close();
        }
    }

    @FXML
    public void handleExitMenuItemClick() {
        System.exit(0);
    }

}

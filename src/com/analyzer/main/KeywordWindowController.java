package com.analyzer.main;

import com.analyzer.data.DataHelper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Created by CXL1751 on 2/7/2017.
 */
public class KeywordWindowController {

    @FXML
    public ListView<String> keywordListPanel;
    @FXML
    private Button searchButton;
    @FXML
    private TextField searchTerm;
    @FXML
    private Label errorText;

    public void initialize() {
        keywordListPanel.setItems(DataHelper.getKeywords());
    }

    public KeywordWindowController() {

    }

    @FXML
    public void searchButtonHandler() {
        String searchTerm = this.searchTerm.getText().trim();
        if (searchTerm != null && !searchTerm.equals("")) {
            ObservableList<String> keywordList = DataHelper.getKeywords();
            ObservableList<String> searchResults = FXCollections.observableArrayList();
            keywordList.forEach(string -> {
                if (string.equalsIgnoreCase(searchTerm) || string.contains(searchTerm)) {
                    searchResults.add(string);
                }
            });
            if (searchResults.size() > 0) {
                this.keywordListPanel.setItems(searchResults);
                this.errorText.setText(null);
            } else {
                this.errorText.setText("No Results Found");
            }
        }
    }
}

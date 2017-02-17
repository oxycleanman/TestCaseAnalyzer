package com.analyzer.main;

import com.analyzer.data.DataHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.io.File;

/**
 * Created by mailc on 2/5/2017.
 */
public class ConfigWindowController {

    @FXML
    private DialogPane configWindow;
    @FXML
    private TextField directoryInput;
    @FXML
    private Button changeDirectoryButton;

    public void initialize() {
        directoryInput.setText(DataHelper.directory);
    }

    @FXML
    public void openDirectoryChooser() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Set Local Repository Directory");
        File file = directoryChooser.showDialog(configWindow.getScene().getWindow());
        if (file != null) {
            directoryInput.setText(file.getAbsolutePath());
        } else {
            System.out.println("Config Directory Chooser canceled");
        }
    }

    public TextField getDirectoryInput() {
        return directoryInput;
    }
}

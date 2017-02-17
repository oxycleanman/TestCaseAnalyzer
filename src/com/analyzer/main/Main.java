package com.analyzer.main;

import com.analyzer.data.DataHelper;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Map;

public class Main extends Application {

    public DataHelper helper;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("mainWindow.fxml"));
        primaryStage.setTitle("Test Case Analyzer");
        primaryStage.setScene(new Scene(root, 1300, 800));
        primaryStage.show();

    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        helper = new DataHelper();
        //Read config file and get settings
        Map<String, String> configSettings = DataHelper.readConfig();
        DataHelper.directory = configSettings.get("directory");

    }
}

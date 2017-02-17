package com.analyzer.main;

import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by CXL1751 on 2/6/2017.
 */
public class DataCell extends ListCell<String> {

    private TextField dataField = new TextField();
    private Label dataLabel = new Label();
    private HBox graphic = new HBox();

    public DataCell() {
        ListCell thisCell = this;
        graphic.setSpacing(10);
        graphic.setHgrow(dataField, Priority.ALWAYS);
        graphic.getChildren().add(dataLabel);
        graphic.getChildren().add(dataField);
        thisCell.setGraphic(graphic);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        List<String> items = new ArrayList<>();
        if (!empty) {
            items = Arrays.asList(item.split(": "));
        }
        if (isEditing()) {
            dataLabel.setText(items.get(0));
            if (items.size() <= 1) {
                dataField.setText(null);
            } else {
                dataField.setText(items.get(1).trim());
            }
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        } else {
            setContentDisplay(ContentDisplay.TEXT_ONLY);
            if (items.size() == 0) {
                setText(null);
            } else {
                setText(item);
                if (items.size() > 1) {
                    setStyle("-fx-background-color: LightGreen");
                } else {
                    setStyle("");
                }
            }
        }
    }

    @Override
    public void startEdit() {
        super.startEdit();
        List<String> items = Arrays.asList(getItem().split(": "));
        dataLabel.setText(items.get(0));
        if (items.size() <= 1) {
            dataField.setText(null);
        } else {
            dataField.setText(items.get(1).trim());
        }
        dataField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                commitEdit(dataLabel.getText() + ": " + dataField.getText());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        dataField.requestFocus();
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        setItem(newValue);
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        setText(getItem());
        setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}

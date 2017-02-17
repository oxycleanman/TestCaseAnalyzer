package com.analyzer.main;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.input.*;

/**
 * Created by CXL1751 on 2/6/2017.
 */
public class KeywordCell extends ListCell<String> {

        private final Label keywordText = new Label();
        private TextField inputText = new TextField();
        private Double topPadding = 0.00d;

        public KeywordCell() {
            ListCell thisCell = this;
            keywordText.setText(getItem());
            thisCell.setGraphic(keywordText);

            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            setAlignment(Pos.CENTER);

            setOnDragDetected(event -> {
                if (getItem() == null) {
                    return;
                }

                Dragboard dragboard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem());
                dragboard.setContent(content);

                event.consume();
            });

            setOnDragOver(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }

                event.consume();
            });

            setOnDragEntered(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    topPadding = getPadding().getTop();
                    setPadding(new Insets(20,getPadding().getRight(), getPadding().getBottom(),getPadding().getLeft()));
                }
            });

            setOnDragExited(event -> {
                if (event.getGestureSource() != thisCell &&
                        event.getDragboard().hasString()) {
                    setPadding(new Insets(topPadding,getPadding().getRight(), getPadding().getBottom(),getPadding().getLeft()));
                }
            });

            setOnDragDropped(event -> {
                if (getItem() == null) {
                    return;
                }

                Dragboard db = event.getDragboard();
                boolean success = false;

                if (db.hasString()) {
                    ObservableList<String> items = getListView().getItems();
                    int draggedIndex = items.indexOf(db.getString());
                    int droppedIndex = items.indexOf(getItem());

                    //New Code
                    if (draggedIndex < droppedIndex) {
                        //moved down
                        System.out.println("before modification: " + items.toString());
                        items.remove(draggedIndex);
                        items.add(droppedIndex - 1, db.getString());

                        System.out.println("after modification: " + items.toString());
                    } else {
                        //moved up
                        System.out.println("before modification: " + items.toString());
                        items.remove(draggedIndex);
                        items.add(droppedIndex, db.getString());
                        System.out.println("after modification: " + items.toString());
                    }
                    getListView().setItems(items);
                    //Select the item we just moved
                    getListView().getSelectionModel().select(db.getString());



                    success = true;
                }
                event.setDropCompleted(success);

                event.consume();
            });

            setOnDragDone(DragEvent::consume);
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                keywordText.setText(null);
            } else {
                keywordText.setText(getItem());
                if (isEditing()) {
                    inputText.setText(item);
                    this.setGraphic(inputText);
                } else {
                    this.setGraphic(keywordText);
                }

            }
        }

    @Override
    public void startEdit() {
        super.startEdit();
        inputText.setText(getItem());
        inputText.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                commitEdit(inputText.getText());
            } else if (e.getCode() == KeyCode.ESCAPE) {
                cancelEdit();
            }
        });
        setGraphic(inputText);
        inputText.requestFocus();
    }

    @Override
    public void commitEdit(String newValue) {
        super.commitEdit(newValue);
        setItem(keywordText.getText());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        keywordText.setText(getItem());
        setGraphic(keywordText);
    }

}

package graph.component;

import DataStruct.AlimP;
import DataStruct.TargetP;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import model.Lang;
import model.targetAdjust;


public class ComboEditingCellAlim extends TableCell<AlimP, TargetP> {
	
    private ComboBox<String> comboBox;

    public ComboEditingCellAlim() {
    }

    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox();
            setText(null);
            setGraphic(comboBox);
            comboBox.requestFocus();
            comboBox.show();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getTarget().getNomS());
        setGraphic(null);
    }

    @Override
    public void updateItem(TargetP item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(getTarget().getNomS());
                    
                }
                setText(getTarget().getNomS());
                setGraphic(comboBox);
            } else {
                setText(getTarget().getNomS());
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
    	 ObservableList<String> targetData     = FXCollections.observableArrayList();
    			 for (targetAdjust t :targetAdjust.values()) {
    				 targetData.add(new TargetP(t).getNomS());
    			 }
        comboBox = new ComboBox<String>(targetData);
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget().getNomS());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(new TargetP(targetAdjust.getByName(comboBox.getSelectionModel().getSelectedItem(), Lang.EN)));
                setText(getTarget().getNomS());
                setGraphic(null);
            }
        });
      
       
        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        comboBox.setOnAction((e) -> {
         
            commitEdit(new TargetP(targetAdjust.getByName(comboBox.getSelectionModel().getSelectedItem(), Lang.EN)));

        });
        comboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
                commitEdit(new TargetP(targetAdjust.getByName(comboBox.getSelectionModel().getSelectedItem(), Lang.EN)));
    
            }
        });
    }

    private void comboBoxConverter(ComboBox<TargetP> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<TargetP>() {
                @Override
                protected void updateItem(TargetP item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.getNomS());
                    }
                }
            };
        });
    }

    private TargetP getTarget() {
        return getItem() == null ? new TargetP(targetAdjust.CALCIUM) : getItem();
    }
}
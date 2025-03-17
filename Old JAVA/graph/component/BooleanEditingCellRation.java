package graph.component;

import DataStruct.AlimP;
import DataStruct.RationP;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellEditEvent;

public class BooleanEditingCellRation extends TableCell<RationP, Boolean> {
    private CheckBox checkBox;
    public BooleanEditingCellRation() {
        checkBox = new CheckBox();
       checkBox.setDisable(true);
     
        checkBox.selectedProperty().addListener(new ChangeListener<Boolean> () {
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
              if(isEditing())
            	requestFocus();	
    

                    commitEdit(newValue == null ? false : newValue);
            }
        });

        this.setGraphic(checkBox);
        this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        this.setEditable(true);
    }
    @Override
    public void startEdit() {
        super.startEdit();
        if (isEmpty()) {
            return;
        }
        checkBox.setDisable(false);
    
    }
    @Override
    public void cancelEdit() {
        super.cancelEdit();
        checkBox.setDisable(true);
    }
    @Override
    public void commitEdit(Boolean value) {

        super.commitEdit(value);
            checkBox.setDisable(true);
    }

    @Override
    public void updateItem(Boolean item, boolean empty) {
        super.updateItem(item, empty);
        if (!isEmpty()) {
        	checkBox.setVisible(true);
            checkBox.setSelected(item);
        }else {
        	checkBox.setVisible(false);
        }
    }
}
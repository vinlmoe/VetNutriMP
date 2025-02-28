
package graph.component;

import DataStruct.ConstrainP;
import DataStruct.RationP;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.KeyCode;

public class StringEditingCell<T> extends TableCell<T, String> {

	TextField textfield = new TextField();
	private String originalValue = null;
	

 

    public void StringEditingCells() {
   
	}

	@Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            originalValue = getItem();
            setText(null);
            setGraphic(textfield);
            textfield.selectAll();
            textfield.requestFocus();
        }
    }

	@Override
	public void commitEdit(String item) {
	    // This block is necessary to support commit on losing focus, because 
	    // the baked-in mechanism sets our editing state to false before we can 
	    // intercept the loss of focus. The default commitEdit(...) method 
	    // simply bails if we are not editing...
		
	    if (!isEditing() && !item.equals(getItem())) {
	        TableView<T> table = getTableView();
	        if (table != null) {
	            TableColumn<T, String> column = getTableColumn();
	            CellEditEvent<T, String> event = new CellEditEvent<>(
	                table, new TablePosition<T,String>(table, getIndex(), column), 
	                TableColumn.editCommitEvent(), item
	            );
	            Event.fireEvent(column, event);
	        }
	    }

	    super.commitEdit(item);
	}
	
    @Override
    public void cancelEdit() {
        super.cancelEdit();

commitEdit(textfield.getText());
setText(textfield.getText());

        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(item);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textfield != null) {
                    textfield.setText(getString());
//                    setGraphic(null);
                }
                setText(null);
                setGraphic(textfield);
            } else {
                setText(getString());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textfield = new TextField(getString());
        textfield.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textfield.setOnAction((e) -> commitEdit(textfield.getText()));
   /*  textfield.setOnAction(e -> {
            cancelEdit();
        });*/

    	textfield.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                textfield.setText(originalValue);
                setText(originalValue);
            }
        });
 
        textfield.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
   
                commitEdit(textfield.getText());
               
                setText(textfield.getText());

            }
        });
    }

    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}

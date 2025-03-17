package graph.component;

import java.text.DecimalFormat;
import java.text.ParsePosition;

import DataStruct.RationP;
import DataStruct.RationP;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.KeyCode;

public class IntEditingCellRation extends TableCell<RationP, Integer> {
	  DecimalFormat format = new DecimalFormat( "#" );
	TextField textfield = new TextField();
	private String  originalValue = null;
	

 

    public IntEditingCellRation() {
   
	}


	@Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            originalValue = String.valueOf(getItem());
            setText(null);
            setGraphic(textfield);
            textfield.selectAll();
            textfield.requestFocus();
        }
    }

	@Override
	public void commitEdit(Integer item) {
	    // This block is necessary to support commit on losing focus, because 
	    // the baked-in mechanism sets our editing state to false before we can 
	    // intercept the loss of focus. The default commitEdit(...) method 
	    // simply bails if we are not editing...
		
	    if (!isEditing() && !item.equals(String.valueOf(getItem()))) {
	        TableView<RationP> table = getTableView();
	        if (table != null) {
	            TableColumn<RationP, Integer> column = getTableColumn();
	            CellEditEvent<RationP, Integer> event = new CellEditEvent<>(
	                table, new TablePosition<RationP,Integer>(table, getIndex(), column), 
	                TableColumn.editCommitEvent(), (item)
	            );
	            Event.fireEvent(column, event);
	        }
	    }

	    super.commitEdit(item);
	    }
    @Override
    public void cancelEdit() {
        super.cancelEdit();

commitEdit(Integer.parseInt( textfield.getText()));
setText(textfield.getText());

        setGraphic(null);
    }

    @Override
   public void updateItem(Integer item, boolean empty) {

super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textfield != null) {
                    textfield.setText(getStringInteger());
//                    setGraphic(null);
                }
                setText(null);
                setGraphic(textfield);
            } else {
                setText(getStringInteger());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textfield = new TextField(getText());
        textfield.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textfield.setOnAction((e) -> commitEdit(Integer.parseInt(getText())));
   /*  textfield.setOnAction(e -> {
            cancelEdit();
        });*/
      


        textfield.setTextFormatter( new TextFormatter<>(c ->
        {
            if ( c.getControlNewText().isEmpty() )
            {
                return c;
            }

            ParsePosition parsePosition = new ParsePosition( 0 );
            Object object = format.parse( c.getControlNewText(), parsePosition );

            if ( object == null || parsePosition.getIndex() < c.getControlNewText().length() )
            {
                return null;
            }
            else
            {
                return c;
            }
        }));
    	textfield.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                textfield.setText(originalValue);
                setText(originalValue);
            }
        });
 
        textfield.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
         
                commitEdit(Integer.parseInt( textfield.getText()));
               
                setText(textfield.getText());
          
            }
        });
    }

    private Integer getInteger() {
        return getItem() == null ? 0 : getItem();
    }
    private String getStringInteger() {
        return getItem() == null ? "0" : String.valueOf(getItem());
    }
}


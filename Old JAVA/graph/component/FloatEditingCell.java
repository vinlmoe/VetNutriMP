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

public class FloatEditingCell<T> extends TableCell<T, Float> {
	  DecimalFormat format = new DecimalFormat( "#.0" );
	TextField textfield = new TextField();
	private String  originalValue = null;
	

 

    public FloatEditingCell() {
   
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
	public void commitEdit(Float item) {
	    // This block is necessary to support commit on losing focus, because 
	    // the baked-in mechanism sets our editing state to false before we can 
	    // intercept the loss of focus. The default commitEdit(...) method 
	    // simply bails if we are not editing...
		
	    if (!isEditing() && !item.equals(String.valueOf(getItem()))) {
	        TableView<T> table = getTableView();
	        if (table != null) {
	            TableColumn<T, Float> column = getTableColumn();
	            CellEditEvent<T, Float> event = new CellEditEvent<>(
	                table, new TablePosition<T,Float>(table, getIndex(), column), 
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

commitEdit(Float.parseFloat(floatable(textfield.getText())));
setText(floatable(textfield.getText()));

        setGraphic(null);
    }

    @Override
   public void updateItem(Float item, boolean empty) {

super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (textfield != null) {
                    textfield.setText(getStringFloat());
//                    setGraphic(null);
                }
                setText(null);
                setGraphic(textfield);
            } else {
                setText(getStringFloat());
                setGraphic(null);
            }
        }
    }

    private void createTextField() {
        textfield = new TextField(getText());
        textfield.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        textfield.setOnAction((e) ->    commitEdit(Float.parseFloat( floatable(textfield.getText()))));
   /*  textfield.setOnAction(e -> {
            cancelEdit();
        });*/
      


 /*       textfield.setTextFormatter( new TextFormatter<>(c ->
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
        }));*/
    	textfield.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                textfield.setText(originalValue);
                setText(originalValue);
            }else if(e.getCode().equals(KeyCode.ENTER)) {
            	 commitEdit(Float.parseFloat( floatable(textfield.getText())));
                 
                 setText(floatable(textfield.getText()));
           
            }
        });
 
        textfield.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
         
                commitEdit(Float.parseFloat( floatable(textfield.getText())));
               
                setText(floatable(textfield.getText()));
          
            }
        });
    }

    private float getFloat() {
        return getItem() == null ? 0 : getItem();
    }
    private String getStringFloat() {
        return getItem() == null ? "" : String.valueOf(getItem());
    }

	private String noPoint(String s){
		String ReP="";
		String t=".";
		String[] st = s.split(",");
		if(st.length==1){
			ReP = st[0];
		}else{
			ReP = st[0]+t+st[1];}

		return ReP;
	}
	private String floatable(String s) {
		
		s=noPoint(s);
		try { Float.parseFloat(s);
		}
		catch(Exception e) {
			s="0";
		}
		return s;
		
	}
}



package graph.component;

import java.util.ResourceBundle;

import DataStruct.ConstrainP;
import DataStruct.EquationConsP;
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

public class StringCellbundle<T> extends TableCell<T, String> {
private ResourceBundle res;
	private String originalValue = null;
	

 

    public StringCellbundle(ResourceBundle res) {
   this.res=res;
	}



    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if(item!=null) {
        setText(res.getString(item));
        }
        else {setText("");}
        
    }


    private String getString() {
        return getItem() == null ? "" : getItem();
    }
}

package graph.component;

import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.BiblioP;
import DataStruct.ConstrainP;
import DataStruct.TargetP;
import Enumerise.NutrientBase;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;

import model.BiblioRef;
import model.Lang;
import model.targetAdjust;


public class ComboEditingCellBiblio<T> extends TableCell<T, BiblioRef> {
	
    private ComboBox<BiblioRef> comboBox;
    private ResourceBundle bundle;
    ObservableList<BiblioRef> ol=FXCollections.observableArrayList();



    public ComboEditingCellBiblio( ObservableList<BiblioP> observableList) {
  for (BiblioP b:observableList) {
	  this.ol.add(b.getBiblio());
  }
    
    }

    
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createComboBox(ol);
            setText(null);
            setGraphic(comboBox);
            comboBox.requestFocus();
            comboBox.show();
        }
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getTarget().toString());
        setGraphic(null);
    }

    @Override
    public void updateItem(BiblioRef item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(item);
                    
                }
                setText((getTarget().toString()));
                setGraphic(comboBox);
            } else {
                setText((getTarget().toString()));
                setGraphic(null);
            }
        }
    }

    private void createComboBox(ObservableList ol) {

        comboBox = new ComboBox<BiblioRef>(ol);
     
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(comboBox.getSelectionModel().getSelectedItem());
                setText((getTarget().toString()));
                setGraphic(null);
            }
        });
      
       
        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        comboBox.setOnAction((e) -> {
         
        	   commitEdit(comboBox.getSelectionModel().getSelectedItem());

        });
        comboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
            	   commitEdit(comboBox.getSelectionModel().getSelectedItem());
    
            }
        });
    }

public void setList( ObservableList <BiblioRef> ol) {
	this.ol=ol;
}

    private BiblioRef getTarget() {
        return getItem() == null ? new  BiblioRef() : getItem();
    }
}
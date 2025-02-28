package graph.component;

import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.ConditionConverter;
import DataStruct.ConstrainP;
import DataStruct.KindDataConverter;
import DataStruct.NutrientP;
import DataStruct.TargetP;
import Enumerise.KindData;
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
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import model.Lang;
import model.targetAdjust;


public class ComboEditingCellUnitConstrain extends TableCell<ConstrainP, KindData> {
	
    private ComboBox<KindData> comboBox;
    private ResourceBundle bundle;

    public ComboEditingCellUnitConstrain(ResourceBundle rb) {
    	this.bundle=rb;
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

        setText((getTarget().getUnit()));
        setGraphic(null);
    }

    @Override
    public void updateItem(KindData item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(item);
                    
                }
                setText((getTarget().getUnit()));
                setGraphic(comboBox);
            } else {
                setText(((getTarget().getUnit())));
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
    	 ObservableList<KindData> targetData     = FXCollections.observableArrayList();
    	switch(((ConstrainP) this.getTableRow().getItem()).getMNE()) {
    	case INGREDIENT:
    	case INDICAT:
    	
    		break;
    		default:
    			targetData.add(KindData.FDESC);
    			targetData.add(KindData.DM);
    			targetData.add(KindData.FENER);
    			break;
    	}
    	
    		
        comboBox = new ComboBox<KindData>(targetData);
    comboBox.setConverter(new KindDataConverter());
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(comboBox.getSelectionModel().getSelectedItem());
                setText(((getTarget().getUnit())));
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
   


    private void comboBoxConverter(ComboBox<KindData> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<KindData>() {
                @Override
                protected void updateItem(KindData item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(((item.getUnit())));
                    }
                }
            };
        });
    }

    private KindData getTarget() {
        return getItem() == null ? null : getItem();
    }
}
package graph.component;

import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.ConditionConverter;
import DataStruct.ConstrainP;
import DataStruct.NutrientP;
import DataStruct.TargetP;
import Enumerise.ConditionEnum;
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


public class ComboEditingCellConstrainConstrain extends TableCell<ConstrainP, ConditionEnum> {
	
    private ComboBox<ConditionEnum> comboBox;
    private ResourceBundle bundle;

    public ComboEditingCellConstrainConstrain(ResourceBundle rb) {
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

        setText(bundle.getString(getTarget().getName()));
        setGraphic(null);
    }

    @Override
    public void updateItem(ConditionEnum item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(item);
                    
                }
                setText((bundle.getString(getTarget().getName())));
                setGraphic(comboBox);
            } else {
                setText((bundle.getString(getTarget().getName())));
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
    	 ObservableList<ConditionEnum> targetData     = FXCollections.observableArrayList();
    	switch(((ConstrainP) this.getTableRow().getItem()).getMNE()) {
    	case INGREDIENT:
    	case INDICAT:
    		targetData.add(ConditionEnum.EXCLIDE);
    		targetData.add(ConditionEnum.INCLUDE);
    		break;
    		default:
    			targetData.add(ConditionEnum.MORE);
    			targetData.add(ConditionEnum.LESS);
    			break;
    	}
    	
    		
        comboBox = new ComboBox<ConditionEnum>(targetData);
    comboBox.setConverter(new ConditionConverter(bundle));
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(comboBox.getSelectionModel().getSelectedItem());
                setText((bundle.getString(getTarget().getName())));
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
   


    private void comboBoxConverter(ComboBox<ConditionEnum> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<ConditionEnum>() {
                @Override
                protected void updateItem(ConditionEnum item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText((bundle.getString(item.getName())));
                    }
                }
            };
        });
    }

    private ConditionEnum getTarget() {
        return getItem() == null ? null : getItem();
    }
}
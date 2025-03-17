package graph.component;

import DataStruct.NutrientP;
import DataStruct.NutrientRefP;
import Enumerise.UnitReqEnum;
import Enumerise.MainNutrientEnum;
import Enumerise.UnitEnum;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;


public class ComboEditingCellUnitReqGen extends TableCell<NutrientRefP, UnitReqEnum> {
	
    private ComboBox<UnitReqEnum> comboBox;

    public ComboEditingCellUnitReqGen() {
    	
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
        this.getTableRow().getItem();
    }
    

    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(getTarget().toString());
        setGraphic(null);
    }

   @Override
    public void updateItem(UnitReqEnum item, boolean empty) {

        super.updateItem(item, empty);

      if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(getTarget());
                    
                }
                setText(getTarget().toString());
                setGraphic(comboBox);
            } else {
                setText(getTarget().toString());
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
    	 ObservableList<UnitReqEnum> targetData     = FXCollections.observableArrayList();
    			
    				 if ( ((NutrientRefP) this.getTableRow().getItem()).getMNE().equals(MainNutrientEnum.ANA))  {
    				 targetData.add(UnitReqEnum.NO);
    			 }else {
    				 targetData.add(UnitReqEnum.MCAL);
    				 targetData.add(UnitReqEnum.KGBW);
    				 targetData.add(UnitReqEnum.KGMW);
    			 }
        comboBox = new ComboBox<UnitReqEnum>(targetData);
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
            	commitEdit(comboBox.getSelectionModel().getSelectedItem());
            	setText(getTarget().toString());
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

    private void comboBoxConverter(ComboBox<UnitReqEnum> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<UnitReqEnum>() {
                @Override
                protected void updateItem(UnitReqEnum item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item.toString());
                    }
                }
            };
        });
    }

    private UnitReqEnum getTarget() {
        return getItem() == null ?  UnitReqEnum.NO : getItem();
    }
}
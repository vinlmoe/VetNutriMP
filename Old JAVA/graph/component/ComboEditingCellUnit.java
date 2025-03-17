package graph.component;

import DataStruct.NutrientP;
import DataStruct.UnitP;
import Enumerise.UnitEnum;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;

import javafx.scene.control.TableCell;
import javafx.scene.input.KeyCode;


public class ComboEditingCellUnit extends TableCell<NutrientP, UnitP> {
	
    private ComboBox<String> comboBox;

    public ComboEditingCellUnit() {
    	
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

        setText(getTarget().getNomS());
        setGraphic(null);
    }

   @Override
    public void updateItem(UnitP item, boolean empty) {

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
    			 for (UnitEnum t :UnitEnum.values()) {
    				 if ( t.getIDFamily()== ((NutrientP) this.getTableRow().getItem()).getUnit().getUnit().getIDFamily() )  {
    				 targetData.add(new UnitP(t).getNomS());
    			 }}
        comboBox = new ComboBox<String>(targetData);
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget().getNomS());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(new UnitP(UnitEnum.getByName(comboBox.getSelectionModel().getSelectedItem(),  ((NutrientP) this.getTableRow().getItem()).getUnit().getUnit().getIDFamily())));
                setText(getTarget().getNomS());
                setGraphic(null);
            }
        });
      
       
        comboBox.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
        comboBox.setOnAction((e) -> {
         
        commitEdit(new UnitP(UnitEnum.getByName(comboBox.getSelectionModel().getSelectedItem(),   ((NutrientP) this.getTableRow().getItem()).getUnit().getUnit().getIDFamily())));

        });
        comboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            if (!newValue) {
              commitEdit(new UnitP(UnitEnum.getByName(comboBox.getSelectionModel().getSelectedItem(),   ((NutrientP) this.getTableRow().getItem()).getUnit().getUnit().getIDFamily())));
    
            }
        });
    }

    private void comboBoxConverter(ComboBox<UnitP> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<UnitP>() {
                @Override
                protected void updateItem(UnitP item, boolean empty) {
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

    private UnitP getTarget() {
        return getItem() == null ? new UnitP(UnitEnum.BUg) : getItem();
    }
}
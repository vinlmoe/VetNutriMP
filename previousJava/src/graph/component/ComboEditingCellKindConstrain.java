package graph.component;

import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.ConstrainP;
import DataStruct.TargetP;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
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
import model.Lang;
import model.targetAdjust;


public class ComboEditingCellKindConstrain extends TableCell<ConstrainP, String> {
	
    private ComboBox<String> comboBox;
    private ResourceBundle bundle;

    public ComboEditingCellKindConstrain(ResourceBundle rb) {
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

        setText(bundle.getString(getTarget()));
        setGraphic(null);
    }

    @Override
    public void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            if (isEditing()) {
                if (comboBox != null) {
                	comboBox.setValue(item);
                    
                }
                setText((bundle.getString(getTarget())));
                setGraphic(comboBox);
            } else {
                setText((bundle.getString(getTarget())));
                setGraphic(null);
            }
        }
    }

    private void createComboBox() {
    	 ObservableList<String> targetData     = FXCollections.observableArrayList();
    	 targetData.add("INGREDIENT");
    	 targetData.add("ENER");
    			 for (NutrientBase t :NutrientBase.values()) {
    				 targetData.add(t.getLabel());
    			 }
    			 targetData.add(NutrientLipid.O3.getLabel());
    			 targetData.add(NutrientLipid.O6.getLabel());
    			 targetData.add(NutrientLipid.EPADHA.getLabel());
    			 for (NutrientMacro t :NutrientMacro.values()) {
    				 targetData.add(t.getLabel());
    			 }
    			 for (NutrientMin t :NutrientMin.values()) {
    				 targetData.add(t.getLabel());
    			 }
    			 targetData.add("ENER");
        comboBox = new ComboBox<String>(targetData);
     comboBoxConverter(comboBox);
   //     comboBoxConverter(comboBox);
comboBox.setValue(getTarget());
    	comboBox.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.ESCAPE)) {
                commitEdit(comboBox.getSelectionModel().getSelectedItem());
                setText((bundle.getString(getTarget())));
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

    private void comboBoxConverter(ComboBox<String> comboBox) {
        // Define rendering of the list of values in ComboBox drop down. 

        comboBox.setCellFactory((c) -> {
            return new ListCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText((bundle.getString(item)));
                    }
                }
            };
        });
    }

    private String getTarget() {
        return getItem() == null ? "PROT" : getItem();
    }
}
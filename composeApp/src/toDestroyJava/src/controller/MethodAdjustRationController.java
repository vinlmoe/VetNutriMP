package controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.UnitReqEnum;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import graph.component.ComboEditingCellUnitReqGen;
import graph.component.ComboEditingCellUnitReqTarg;
import graph.component.FloatEditingCell;
import graph.component.StringEditingCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.TargetDefinitionEv;
import model.AdjustSaveEv;
import model.targetAdjust;

public class MethodAdjustRationController  implements Initializable {

private ObservableList<TargetDefinitionEv>list=FXCollections.observableArrayList();
	
	
	private Stage stage;



	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;


	//Définition graphique 

	@FXML
	private TextField measureText;
	@FXML
	private TextField perEnerText;
	@FXML 
	private ComboBox<targetAdjust> lastCombo;
	@FXML
	private ComboBox<AdjustSaveEv> loadCombo;
	@FXML
	private VBox vbModif;
	
private TargetEditorControler TEC;
	



@FXML
private Button addButton;

	

@FXML
private void close() {
	stage.close();
}



	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

		


	
		
		lastCombo.getItems().addAll(targetAdjust.values());
		lastCombo.getSelectionModel().select(targetAdjust.ENERGIE);
		
	lastCombo.setConverter(
            new StringConverter<targetAdjust>() {
                @Override
                public String toString(targetAdjust ta) {
                    if (ta == null) {
                        return "";
                    } else {
                        return resources.getString(ta.toString());
                    }
                }

                @Override
                public targetAdjust fromString(String s) {
                    try {
                        return targetAdjust.ENERGIE;
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
            });	
	
	loadCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
if (newValue!=null) {
	System.out.println(" Update EXT"+TEC.getData().get(0).getValue()	);
	TEC.setList(newValue.getList());}
	});
			
	
	} 


	VetNutri mainApp;
	public void setMainApp(VetNutri methodEditorControler, boolean edition) {
		this.mainApp=methodEditorControler;
	
		this.edition=edition;
		loadCombo.getItems().addAll(mainApp.getmMethodList());
		TEC=setCoefTable();
		TEC.setList(new AdjustSaveEv().getList());
	}
	public void Update() {
		loadCombo.getItems().clear();
		loadCombo.getItems().addAll(mainApp.getmMethodList());
	}


	public ObservableList<TargetDefinitionEv> getData() {
		System.out.println("TEX "+TEC.getData().get(0).getValue());
	return TEC.getData();
	}
	private TargetEditorControler setCoefTable () {
	    
    	TargetEditorControler coefwin;
   	 FXMLLoader loader = new FXMLLoader();
           loader.setResources(bundle);
           loader.setLocation(VetNutri.class.getResource("/view/methodTable.fxml"));
        
         
   	  try {
			vbModif.getChildren().add((AnchorPane)loader.load());
			  coefwin=loader.getController();
	            coefwin.setMainApp(this, stage, true);
	    return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
   }

public void bip() {
	perEnerText.setText("9");
}
public float getPerEner() {
	return Float.parseFloat(perEnerText.getText());
}
public float getMeasure() {
	return Float.parseFloat(measureText.getText());
}

public targetAdjust getLastTarget() {
	return lastCombo.getSelectionModel().getSelectedItem()
;}

private String noPoint(String s){
	String ReP="";
	String t=".";
	String[] st = s.split(",");
	if(st.length==1){
		ReP = st[0];
	}else{
		ReP = st[0]+t+st[1];}
	try {
		Float.parseFloat(ReP);
	}catch (NumberFormatException e) {
		if (ReP!="") {
			ReP="0.0";}
	}
	return ReP;
}
		public void other( String  e) {
			TEC.other(e);
		}
		}

package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.AAEnum;
import Enumerise.KindData;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import equation.MiniReqAnalyszer;
import equation.RequirementAnalyzer;
import graph.component.FloatEditingCell;
import graph.component.IntakeTab;
import graph.component.LabelData;
import graph.component.StringEditingCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import model.AlimentRation;
import model.RationCalculator;

public class IntakeTabPaneController  implements Initializable {


private Stage stage;
private FormulationEditorControler FEC;
boolean indeletion=false;
RationCalculator calc;
boolean glob;
MiniReqAnalyszer ra;
private LabelData[] NutrientBEE;
boolean abort=false;
private ResourceBundle bundle;
private boolean analyzed=false;

//Définition graphique 
@FXML 
private TabPane mainTabPane;

private  Map<Integer, IntakeTabController> map = new HashMap<Integer, IntakeTabController>();


@Override 
public void initialize(URL location, ResourceBundle resources) {

	bundle=resources;
	//AlimTabl

              mainTabPane.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number> (){
            	     @Override
            	     public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
            	
            	    	 if (newValue!=null){
        		        	if (map.containsKey( newValue.intValue()))	{
        		        		if (calc!=null & ra!=null) {
        		        			map.get( newValue.intValue()).updateValues(calc, ra, glob);
        		        		}
        		        	else {
        		        		
        		        		map.get( newValue.intValue()).returnNALabel();;
        		        	}}
            	     }}
            	 });

}


public void setTabs(ObservableList<KindData>kd) {
	mainTabPane.getTabs().clear();
	map.clear();
	for (KindData k:kd) {
	creatTab(k);	}
}

public void updateValues(RationCalculator calc, RequirementAnalyzer ra, Boolean glob) {
this.glob=glob;
	if (calc!=null & ra!=null) {
		this.calc=calc;
		this.ra=new MiniReqAnalyszer(ra);
	
		
map.get( mainTabPane.getSelectionModel().getSelectedIndex()).updateValues(this.calc, this.ra, glob);

	}
	else {
		map.get( mainTabPane.getSelectionModel().getSelectedIndex()).returnNALabel();;
	}

    
}
public void setMainApp(  ObservableList<KindData>kd, MainWinController main) {
	

setTabs(kd);
	
}
public void setMainApp(  ObservableList<KindData>kd, FormulationEditorControler FEC) {
	

setTabs(kd);
	
}


private void creatTab(KindData kd ) {
	 IntakeTabController coefwin;
  	 FXMLLoader loader = new FXMLLoader();
          loader.setResources(bundle);
          loader.setLocation(VetNutri.class.getResource("/view/intakeTab.fxml"));
  	  try {
  		  Tab rt=(Tab)loader.load();
  		  //rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
  		  coefwin=loader.getController();
		  
			
          coefwin.setTab(kd);
map.put((mainTabPane.getTabs().size()), coefwin);
			mainTabPane.getTabs().add( rt);
		
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

}

public LabelData[] getNut(KindData k) {
	
	for (IntakeTabController itc:map.values()) {
		if (itc.getKindData().equals(k)) {
			itc.updateValues(calc, ra, glob);
			return itc.getNutrientBEE();		
		}
	}
	return null;
}



		}

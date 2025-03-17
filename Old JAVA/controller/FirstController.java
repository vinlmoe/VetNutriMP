package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import org.controlsfx.control.CheckListView;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import DataStruct.SpeciesConverter;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import graph.component.AutocompletionlTextField;
import graph.component.FloatEditingCell;
import graph.component.StringEditingCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AlimDBList;
import model.AnimalEv;
import model.Espece;
import model.Sex;
import model.Vet;
import model.alimDB;
import model.targetAdjust;

public class FirstController  implements Initializable {

@FXML
CheckListView<alimDB> listAlimDB;
	
@FXML
private ComboBox<String> langCombo;

private Vet vet;
@FXML
public void continuer() {
	ObservableList<alimDB>d=listAlimDB.getCheckModel().getCheckedItems();
	AlimDBList ADBL =new AlimDBList();
	for (alimDB al:d) {
		ADBL.add(al);
	}
	vet.setLanguage(langCombo.getSelectionModel().getSelectedItem());
	vet.setAlDBL(ADBL);
	mainApp.setVet(vet);
if (langCombo.getSelectionModel().getSelectedItem()=="Français") {
	
  mainApp.setBun( ResourceBundle.getBundle("language/label", new Locale("FR", "fr")));
}else {
	mainApp.setBun( ResourceBundle.getBundle("language/label", new Locale("EN", "en")));
}
	stage.close();
}

@FXML
public void  exit() {
	System.exit(0);
}
private Stage stage;
	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

	langCombo.getItems().add("Français");
	langCombo.getItems().add("English");
langCombo.getSelectionModel().selectFirst();
	}

	VetNutri mainApp;
	public void setMainApp( Stage stage, VetNutri v, Vet vet, AlimDBList alDBL) {
		
		this.stage=stage;
	this.mainApp=v;
	this.vet=vet;
	System.out.println(vet.getAlDBL().values().size()+ " SIZE");
	langCombo.getSelectionModel().select(vet.getLanguage());
	listAlimDB.getItems().addAll(alDBL.values());
	listAlimDB.getCheckModel().clearChecks();;
	for (alimDB al:listAlimDB.getItems()) {
	
	if (vet.getAlDBL().contains(al.getUUID())){
		System.out.println(vet.getAlDBL().get(al.getUUID()).getsNom());
		listAlimDB.getCheckModel().check(al);
	}
	}
	
	this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		public void handle(WindowEvent we) {
		
			System.exit(0);
		
		}
	});        
		
	}



	

		
		}

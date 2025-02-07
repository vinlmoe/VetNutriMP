package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import application.DataConnector;
import equation.Equation;

import graph.component.FloatEditingCell;
import graph.component.StringEditingCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import model.AlimentRation;

public class TableFoodController  implements Initializable {

private ObservableList<CoefP>list=FXCollections.observableArrayList();
	
	
	private Stage stage;
	boolean indeletion=false;


	boolean abort=false;
	private ResourceBundle bundle;
	private boolean analyzed=false;


	//Définition graphique 
	@FXML 
	private TableView <AlimP> alimTable;
	@FXML
	private TableColumn<AlimP, Integer>idColumn;
	@FXML 
	private TableColumn <AlimP, String>brandColumn ; 
	@FXML 
	private TableColumn <AlimP, String> rangeColumn ; 

	@FXML 
	private TableColumn <AlimP, String>nameColumn ; 

	Comparator<AlimP> comparator = Comparator.comparingDouble(AlimP::getDE);
	


	public  void addAlim(AlimP al) {
		if (al!=null) {
			if (!alimTable.getItems().contains(al)) {
		alimTable.getItems().add(al);		
		alimTable.getItems().sort(comparator);
		for (int i=0;i<alimTable.getItems().size();i++) {
			alimTable.getItems().get(i).setId(i+1);
		}
		alimTable.refresh();
		}
}
	
	}

public void addAlim(ObservableList<AlimP> ol) {
	for (AlimP al:ol) {
	if (al!=null) {
		if (!alimTable.getItems().contains(al)) {
	alimTable.getItems().add(al);		
	
	}
}}
	alimTable.getItems().sort(comparator);
	for (int i=0;i<alimTable.getItems().size();i++) {
		alimTable.getItems().get(i).setId(i+1);
	}
	alimTable.refresh();

}

public void deleteAlim() {
	indeletion=true;
	if(alimTable.getSelectionModel().getSelectedItem()!=null) {
alimTable.getItems().remove(alimTable.getSelectionModel().getSelectedItem());
alimTable.getItems().sort(comparator);
for (int i=0;i<alimTable.getItems().size();i++) {
	alimTable.getItems().get(i).setId(i+1);
}
indeletion=false;
		alimTable.refresh();
		
		
	}
}
public void setID() {

	for (int i=0;i<alimTable.getItems().size();i++) {
		alimTable.getItems().get(i).setId(i+1);
	}
}

public void deleteAllAlim() {
	indeletion=true;
	alimTable.getItems().clear();
	alimTable.refresh();
	indeletion=false;

}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl
		comparator=comparator.reversed();

		alimTable.setEditable(false);
		idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
		brandColumn.setCellValueFactory(new PropertyValueFactory<>("Brand"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		rangeColumn.setCellValueFactory(new PropertyValueFactory<>("range"));
		alimTable.setRowFactory(tv -> new TableRow<AlimP>() {
            private Tooltip tooltip = new Tooltip();
            @Override
            public void updateItem(AlimP person, boolean empty) {
                super.updateItem(person, empty);
                if (person == null) {
                    setTooltip(null);
                } else {
                	
                	
                	WebView  web = new WebView();
            		WebEngine webEngine = web.getEngine();
            		webEngine.loadContent
            		(
            				new AlimentRation(person.getAlim()).Resume(bundle)
            		);
            		web.setPrefHeight(400);
            		web.setPrefWidth(300);
            		web.setFontScale(0.8);
            		Tooltip  tip = new Tooltip();
            		tip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            		tip.setGraphic(web);
            tip.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
            		setTooltip(tip);
                  
                }
            }
        });
	}
	
	public void setItems(ObservableList<AlimP> listA)  {
	alimTable.setItems( listA);
	
	}
public TableView<AlimP> getAlimTable() {
	return alimTable;
}
	
	public void setMainApp( Stage stage, boolean analyzed) {
		
		this.stage=stage;
		this.analyzed=analyzed;
		if (!analyzed) {
			alimTable.getColumns().remove(idColumn);
		}
		
	}


	public ObservableList<AlimP> getData() {
	return this.alimTable.getItems();
	}
	
	public AlimP getSelected() {
		return alimTable.getSelectionModel().getSelectedItem();
	}
	
	public boolean isIndeletion() {
		return indeletion;
	}
		
		}

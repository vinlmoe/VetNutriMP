package controller;

import java.net.URL;
import java.util.ResourceBundle;
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
import javafx.scene.control.Button;
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
import javafx.stage.Stage;
import javafx.util.Callback;

public class CoefEditorControler  implements Initializable {

private ObservableList<CoefP>list=FXCollections.observableArrayList();
	
	
	private Stage stage;



	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;


	//Définition graphique 
@FXML
private Label labK;
	@FXML
	private TableView<CoefP> coefTable;
	@FXML
	private TableColumn<CoefP, Float> valueColumn;

	@FXML
	private TableColumn <CoefP, String>nameColumn;


	@FXML
	private TextField nameText;
	



@FXML
private Button addButton;

	@FXML 
	private  void addCoef() {
		list=coefTable.getItems();
		if (coefTable.isVisible()&!list.isEmpty()) {
		CoefP nEqu=new CoefP("New", 1, list.get(0).getGroupUUID());
		 
		 String transUUID=nEqu.getUUID();
		 
		list.add(nEqu);
		 
	
	
		 
		Update(list);
		coefTable.refresh();}
	}

@FXML
private void close() {
	stage.close();
}
@FXML
private void deleteCoef() {
	if(coefTable.getSelectionModel().getSelectedItem()!=null&coefTable.getItems().size()>1) {
		coefTable.getItems().remove(  coefTable.getSelectionModel().getSelectedIndex());
		
	
		coefTable.refresh();
	}
}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

		coefTable.setEditable(true);

		valueColumn.setCellValueFactory(new PropertyValueFactory<>("coef"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("description"));



	
		Callback<TableColumn<CoefP, String>, TableCell<CoefP, String>> stringCellFactory
		= (TableColumn<CoefP, String> param) -> new StringEditingCell<CoefP>();
		Callback<TableColumn<CoefP, Float>, TableCell<CoefP, Float>> floatCellFactory
		= (TableColumn<CoefP, Float> param) -> new FloatEditingCell<CoefP>();
		nameColumn.setCellFactory(stringCellFactory);
		valueColumn.setCellFactory(floatCellFactory);
		valueColumn .setOnEditCommit((CellEditEvent<CoefP, Float> event) -> {
			TablePosition<CoefP, Float> pos = event.getTablePosition();
			float value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setCoef(value)	;});
			
		nameColumn .setOnEditCommit((CellEditEvent<CoefP, String> event) -> {
			TablePosition<CoefP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setDescription(value);	});
			
	
	} 
	public void Update(ObservableList<CoefP> listA)  {
		coefTable.setItems( listA);
	}

	ReferenceEditorControler mainApp;
	public void setMainApp(ReferenceEditorControler mainApp, Stage stage, boolean edition) {
		this.mainApp=mainApp;
		this.stage=stage;
		this.edition=edition;
	}


	public ObservableList<CoefP> getData() {
	return this.coefTable.getItems();
	}
	public String getName() {
	
	return this.nameText.getText();
	}

	public void setList(ObservableList<CoefP>ol) {
		this.coefTable.setItems(ol);
	}
	public void setName(String s) {
		this.nameText.setText(s);
	}
	public void setK(String k) {
		
		labK.setText(k);
	}
	public void setVisible(boolean b) {
		labK.setVisible(b);
		coefTable.setVisible(b);
	}
		
		}

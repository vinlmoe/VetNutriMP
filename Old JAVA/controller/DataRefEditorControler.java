package controller;

import java.net.URL;
import java.util.ResourceBundle;

import DataStruct.BiblioP;
import DataStruct.NutrientP;
import DataStruct.NutrientRefP;
import DataStruct.UnitP;
import Enumerise.AAEnum;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import Enumerise.UnitEnum;
import Enumerise.UnitReqEnum;
import application.DataConnector;
import equation.Equation;
import graph.component.ComboEditingCellBiblio;
import graph.component.ComboEditingCellUnit;
import graph.component.ComboEditingCellUnitGen;
import graph.component.ComboEditingCellUnitReqGen;
import graph.component.FloatEditingCell;
import graph.component.FloatEditingCellNutrientRef;
import graph.component.StringEditingCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.BiblioRef;
import model.ReferenceEv;

public class DataRefEditorControler  implements Initializable {

private ObservableList<NutrientRefP>list=FXCollections.observableArrayList();
	private Stage stage;
private BiblioRef bib;
	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;
	//Définition graphique 
@FXML
private Label labText;

	@FXML
	private TableView<NutrientRefP> dataTable;
	@FXML
	private TableColumn<NutrientRefP, String> valueColumn;
	@FXML
	private TableColumn <NutrientRefP, String>nameColumn;
	@FXML
	private TableColumn <NutrientRefP, UnitP>unitColumn;
	@FXML
	private TableColumn <NutrientRefP, UnitReqEnum>byColumn;
	@FXML
	private TableColumn <NutrientRefP, BiblioRef>biblioColumn;
		@FXML
	private TextField nameText;
@FXML
private Button addButton;
@FXML
private void close() {
	stage.close();
}
@FXML
private void deleteCoef() {
	if(dataTable.getSelectionModel().getSelectedItem()!=null) {
	/*	DataConnector.DeleteEquation(dataTable.getSelectionModel().getSelectedItem().getEquation().getUUID(), null);
		 mRefList=mainApp.getmEquationList();

		Update();*/
	}
}

public void unselect() {
	dataTable.edit(-1,null);
	dataTable.getSelectionModel().clearSelection();
	
}

	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

		dataTable.setEditable(true);

		valueColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));
unitColumn.setCellValueFactory(new PropertyValueFactory<>("Unit"));
byColumn.setCellValueFactory(new PropertyValueFactory<>("UnitReq"));
biblioColumn.setCellValueFactory(new PropertyValueFactory<>("Biblio"));



	
	
		Callback<TableColumn<NutrientRefP, String>, TableCell<NutrientRefP, String>> floatCellFactory
		= (TableColumn<NutrientRefP, String> param) -> new FloatEditingCellNutrientRef();
		Callback<TableColumn<NutrientRefP, UnitP>, TableCell<NutrientRefP, UnitP>> comboUnitCellFactory
		= (TableColumn<NutrientRefP, UnitP> param) -> new ComboEditingCellUnitGen();
		Callback<TableColumn<NutrientRefP, UnitReqEnum>, TableCell<NutrientRefP, UnitReqEnum>> comboUnitReqCellFactory
		= (TableColumn<NutrientRefP, UnitReqEnum> param) -> new ComboEditingCellUnitReqGen();



		valueColumn.setCellFactory(floatCellFactory);
	unitColumn.setCellFactory(comboUnitCellFactory);
		byColumn.setCellFactory(comboUnitReqCellFactory);
	
	valueColumn .setOnEditCommit((CellEditEvent<NutrientRefP, String> event) -> {
			TablePosition<NutrientRefP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);	

	});
			
			unitColumn.setOnEditCommit((CellEditEvent<NutrientRefP, UnitP> event) -> {
				TablePosition<NutrientRefP, UnitP> pos = event.getTablePosition();
				UnitP value = event.getNewValue();
				int row = pos.getRow();
				event.getTableView().getItems().get(row).setUnit(value);          
				});

			byColumn.setOnEditCommit((CellEditEvent<NutrientRefP, UnitReqEnum> event) -> {
				TablePosition<NutrientRefP, UnitReqEnum> pos = event.getTablePosition();
				UnitReqEnum value = event.getNewValue();
				int row = pos.getRow();
				event.getTableView().getItems().get(row).setUnitReq(value);          
				});
		//random function tree
		biblioColumn.setOnEditCommit((CellEditEvent<NutrientRefP, BiblioRef> event) -> {
			TablePosition<NutrientRefP, BiblioRef> pos = event.getTablePosition();
			BiblioRef value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setBiblio(value);          
			});
	} 
	public void Update(ObservableList<NutrientRefP> listA)  {
		dataTable.setItems( listA);
	}
	ReferenceEditorControler mainApp;
	public void setMainApp(ReferenceEditorControler mainApp, Stage stage, boolean edition) {
		this.mainApp=mainApp;
		this.stage=stage;
		this.edition=edition;
	}
public void setBiblio(ObservableList<BiblioP>mbiblio) {
	Callback<TableColumn<NutrientRefP, BiblioRef>, TableCell<NutrientRefP, BiblioRef>> comboBiblioCellFactory
	= (TableColumn<NutrientRefP, BiblioRef> param) -> new ComboEditingCellBiblio<NutrientRefP>(mbiblio);
	biblioColumn.setCellFactory(comboBiblioCellFactory);
}
	public ObservableList<NutrientRefP> getData() {
	return this.dataTable.getItems();
	}
	public String getName() {
	return this.nameText.getText();
	}

	public void setList(ObservableList<NutrientRefP>ol) {
		this.dataTable.setItems(ol);
	}
	public ObservableList<NutrientRefP> getList(){
	return	this.dataTable.getItems();
	}


public void setDataNutrient(MainNutrientEnum en, ReferenceEv al, Reflevel relative)  {
	labText.setText(bundle.getString(relative.name()));
	ObservableList<NutrientRefP>	list= FXCollections.observableArrayList();
	if (al!=null) {
		switch(en) {
		case BASE:

			for (NutrientBase esp : NutrientBase.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative), UnitEnum.BUg,   UnitReqEnum.getById(al.getNutrientUnit(esp, relative)), al.getNutrientBib(esp, relative) ));
			}
			break;
		case MACRO:

			for (NutrientMacro esp : NutrientMacro.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
			}
			break;
		case MIN:

			for (NutrientMin esp : NutrientMin.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
			}
				break;
		case VITAM:

			for (NutrientVitam esp : NutrientVitam.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
			}
			break;

		case LIPID:

			for (NutrientLipid esp : NutrientLipid.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
		}
			break;

		case AMA:

			for (AAEnum esp : AAEnum.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
		}
			break;
		case OTHER:

			for (NutrientOther esp : NutrientOther.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  (esp.getUe()),    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
		}
			break;
		case ANA:

			for (NutrientAnalysis esp : NutrientAnalysis.values()){
				list.add(new NutrientRefP( en, bundle.getString(esp.getLabel()), esp.getCoef(), relative.getCoef(),al.isNutrient(esp, relative)?""+ al.getNutrient(esp, relative): "",al.isNutrient(esp, relative),  UnitEnum.NO,    UnitReqEnum.getById(al.getNutrientUnit(esp, relative)) , al.getNutrientBib(esp, relative) ));
		}
			break;
		}}
	this.dataTable.getItems().setAll(list);

}
public ObservableList<NutrientRefP> getDataNutrient(){
	return this.dataTable.getItems()
;}
public void setVisible(boolean b) {
	dataTable.setVisible(b);
}
		}

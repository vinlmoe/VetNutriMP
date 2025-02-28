package controller;

import java.net.URL;
import java.util.ResourceBundle;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.UnitReqEnum;
import application.DataConnector;
import equation.Equation;
import graph.component.ComboEditingCellUnitReqGen;
import graph.component.ComboEditingCellUnitReqTarg;
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
import model.AdjustSaveEv;
import model.Espece;
import model.Recette;
import model.TargetDefinition;
import model.TargetDefinitionEv;

public class TargetEditorControler  implements Initializable {

private ObservableList<TargetDefinitionEv>list=FXCollections.observableArrayList();
	
	
	private Stage stage;

MethodAdjustRationController parent;

	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;

AdjustSaveEv ASE=new AdjustSaveEv();
	//Définition graphique 

	@FXML
	private TableView<TargetDefinitionEv> coefTable;
	@FXML
	private TableColumn<TargetDefinitionEv, Float> valueColumn;

	@FXML
	private TableColumn <TargetDefinitionEv, String>nameColumn;
	@FXML
	private TableColumn <TargetDefinitionEv, UnitReqEnum>unitColumn;
	@FXML
	private TableColumn<TargetDefinitionEv, Float> percentColumn;
	@FXML
	private TableColumn<TargetDefinitionEv, Float> measureColumn;

	



@FXML
private Button addButton;

	@FXML 
	private  void up() {
		list=coefTable.getItems();
		int i=coefTable.getSelectionModel().getSelectedIndex();
		TargetDefinitionEv trans;
		if (i!=0&i>-1) {
			trans=list.get(i);
			list.set(i, list.get(i-1));
			list.set(i-1, trans);
		this.Update(list);
		coefTable.getSelectionModel().select(i-1);
		}else {
			
		}
	}
	
	@FXML 
	private  void down() {
		list=coefTable.getItems();
		int i=coefTable.getSelectionModel().getSelectedIndex();
		TargetDefinitionEv trans;
		if (i!=list.size()-1&i>-1) {
			trans=list.get(i);
			list.set(i, list.get(i+1));
			list.set(i+1, trans);
		this.Update(list);
		coefTable.getSelectionModel().select(i+1);
		}else {
			
		}
	}

@FXML
private void close() {
	stage.close();
}
@FXML
private void deleteCoef() {
	if(coefTable.getSelectionModel().getSelectedItem()!=null) {
	/*	DataConnector.DeleteEquation(coefTable.getSelectionModel().getSelectedItem().getEquation().getUUID(), null);
		 mRefList=mainApp.getmEquationList();

		Update();*/
	}
}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

		coefTable.setEditable(true);

		valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		percentColumn.setCellValueFactory(new PropertyValueFactory<>("percent"));
		measureColumn.setCellValueFactory(new PropertyValueFactory<>("pas"));
		unitColumn.setCellValueFactory(new PropertyValueFactory<>("ure"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));



	
		
		Callback<TableColumn<TargetDefinitionEv, Float>, TableCell<TargetDefinitionEv, Float>> floatCellFactory
		= (TableColumn<TargetDefinitionEv, Float> param) -> new FloatEditingCell<TargetDefinitionEv>();
		Callback<TableColumn<TargetDefinitionEv, UnitReqEnum>, TableCell<TargetDefinitionEv, UnitReqEnum>> ureCellFactory
		= (TableColumn<TargetDefinitionEv, UnitReqEnum> param) -> new ComboEditingCellUnitReqTarg();
		
		valueColumn.setCellFactory(floatCellFactory);
		measureColumn.setCellFactory(floatCellFactory);
		percentColumn.setCellFactory(floatCellFactory);
		
		
		unitColumn.setCellFactory(ureCellFactory);
		
		valueColumn .setOnEditCommit((CellEditEvent<TargetDefinitionEv, Float> event) -> {
			TablePosition<TargetDefinitionEv, Float> pos = event.getTablePosition();
		
			
			float value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setValue(value)	;
		
		
		});
		
		
		measureColumn .setOnEditCommit((CellEditEvent<TargetDefinitionEv, Float> event) -> {
			TablePosition<TargetDefinitionEv, Float> pos = event.getTablePosition();
			float value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setPas(value)	;});
		percentColumn .setOnEditCommit((CellEditEvent<TargetDefinitionEv, Float> event) -> {
			TablePosition<TargetDefinitionEv, Float> pos = event.getTablePosition();
			float value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setPercentCompletion(value);});
		
		unitColumn .setOnEditCommit((CellEditEvent<TargetDefinitionEv, UnitReqEnum> event) -> {
			TablePosition<TargetDefinitionEv, UnitReqEnum> pos = event.getTablePosition();
			UnitReqEnum value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUre(value)	;});
		
			coefTable.getItems().addAll(ASE.getList());
			
			
		nameColumn.setCellFactory(column->new TableCell<TargetDefinitionEv, String>() {
			    @Override
			    public void updateItem(String item, boolean empty) {
			        super.updateItem(item, empty);
			        if(item!=null) {
			        setText(bundle.getString(item));
			        }
			        else {setText("");}
			        
			    }

			});
	
	} 
	public void Update(ObservableList<TargetDefinitionEv> listA)  {
		coefTable.setItems( listA);
		
	}

	
	public void setMainApp(MethodEditorControler methodEditorControler, Stage stage, boolean edition) {
	
		this.stage=stage;
		this.edition=edition;
	}


	public ObservableList<TargetDefinitionEv> getData() {
		
		System.out.println(" Get "+ coefTable.getItems().get(0).getValue()	);
		
	return coefTable.getItems();
	}
	
	public void other(String e) {
		
		System.out.println(e+ coefTable.getItems().get(0).getValue()	);
	}

	public void setList(ObservableList<TargetDefinitionEv>ol) {
		
		coefTable.setItems(ol);
	}
	
	
	public void setVisible(boolean b) {
	
		coefTable.setVisible(b);
	}

	public void setMainApp(MethodAdjustRationController methodAdjustRationController, Stage stage2, boolean edition2) {
		// TODO Auto-generated method stub
		parent=methodAdjustRationController;
		System.out.println(parent.toString()+"/"+methodAdjustRationController.toString());
		this.stage=stage;
		this.edition=edition;
	}
	

		}

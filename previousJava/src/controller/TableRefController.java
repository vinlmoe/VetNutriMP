package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import application.DataConnector;
import equation.Equation;

import graph.component.FloatEditingCell;
import graph.component.StringCellbundle;
import graph.component.StringEditingCell;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
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
import model.ReferenceEv;
import model.Espece;
import model.Recette;

public class TableRefController  implements Initializable, TableInterface {

private ObservableList<ReferenceEv>mainList=FXCollections.observableArrayList();
	
	
	private Stage stage;
	boolean indeletion=false;


	boolean abort=false;
	private ResourceBundle bundle;
	private boolean analyzed=false;


	//Définition graphique 
	@FXML
	private TableView<ReferenceEv> refTable;
	@FXML
	private TableColumn<ReferenceEv, String> speciesColumn;

	@FXML
	private TableColumn <ReferenceEv, String>nameColumn;

	
	


	public  void add(ReferenceEv al) {
		if (al!=null) {
			if (!refTable.getItems().contains(al)) {
		refTable.getItems().add(al);		
		
		refTable.refresh();
		}
}
	
	}

public void add(ObservableList<ReferenceEv> ol) {
	for (ReferenceEv al:ol) {
	if (al!=null) {
		if (!refTable.getItems().contains(al)) {
	refTable.getItems().add(al);		
	
	}
}}
	
	refTable.refresh();

}

public void delete() {
	indeletion=true;
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
refTable.getItems().remove(refTable.getSelectionModel().getSelectedItem());

indeletion=false;
		refTable.refresh();
		
		
	}
}


public void deleteAll() {
	indeletion=true;
	refTable.getItems().clear();
	refTable.refresh();
	indeletion=false;

}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl
		

		refTable.setEditable(false);

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("species"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		Callback<TableColumn<ReferenceEv, String>, TableCell<ReferenceEv, String>> stringCellFactory
		= (TableColumn<ReferenceEv, String> param) -> new StringCellbundle<ReferenceEv>(bundle);
		
		speciesColumn.setCellFactory(stringCellFactory);



		

	}
	
	public void setItems(ObservableList<ReferenceEv> listA)  {
	refTable.setItems( listA);
	
	}
public TableView<ReferenceEv> getAlimTable() {
	return refTable;
}
	
	public void setMainApp( Stage stage, boolean analyzed) {
		
		this.stage=stage;
		this.analyzed=analyzed;
	
		
	}


	public ObservableList<ReferenceEv> getData() {
	return this.refTable.getItems();
	}
	
	public ReferenceEv getSelected() {
		return refTable.getSelectionModel().getSelectedItem();
	}
	
	public boolean isIndeletion() {
		return indeletion;
	}

	@Override
	public void search(String text, Espece es) {
		// TODO Auto-generated method stub
		ObservableList<ReferenceEv> sAlimList=FXCollections.observableArrayList();
		String textSearch= text;

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (ReferenceEv al:mainList) {
				if (	 			(	al.getSpecies().equals(es)|es.equals(Espece.CH))
						) {

					boolean prs=true;
					for (String word: words){
						if (al.getName().toLowerCase().indexOf(word.toLowerCase())!=-1){
							prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sAlimList.add(al);
					}					

				}
			}    		

		}else {
			for (ReferenceEv al:mainList) {
				if (	 			(	al.getSpecies().equals(es)|es.equals(Espece.CH))
						) {

			
						sAlimList.add(al);
								

				}
			}    		

		}


	}
		}

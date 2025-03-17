package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.PriceCateg;
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
import model.AdjustSaveEv;
import model.AlimIndic;
import model.Espece;
import model.GroupAlim;
import model.Recette;
import model.TypeAlim;

public class TableRecipeController  implements Initializable, TableInterface {
	ObservableList< Recette>mainList =FXCollections.observableArrayList();
	
	private Stage stage;
	boolean indeletion=false;


	boolean abort=false;
	private ResourceBundle bundle;
	private boolean analyzed=false;


	//Définition graphique 
	@FXML
	private TableView<Recette> refTable;
	@FXML
	private TableColumn<Recette, Espece> speciesColumn;

	@FXML
	private TableColumn <Recette, String>nameColumn;

	
	


	public  void add(Recette al) {
		if (al!=null) {
			if (!refTable.getItems().contains(al)) {
		refTable.getItems().add(al);		
		
		refTable.refresh();
		}
}
	
	}

public void add(ObservableList<Recette> ol) {
	for (Recette al:ol) {
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

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("espece"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));




		speciesColumn.setCellFactory(column->new TableCell<Recette, Espece>() {
		    @Override
		    public void updateItem(Espece item, boolean empty) {
		        super.updateItem(item, empty);
		        if(item!=null) {
		        setText(bundle.getString(item.getName()));
		        }
		        else {setText("");}
		        
		    }

		});

	}
	
	public void setItems(ObservableList<Recette> listA)  {
		this.mainList=listA;
	refTable.setItems( mainList);
	
	}
public TableView<Recette> getAlimTable() {
	return refTable;
}
	
	public void setMainApp( Stage stage, boolean analyzed) {
		
		this.stage=stage;
		this.analyzed=analyzed;
	
		
	}


	public ObservableList<Recette> getData() {
	return this.refTable.getItems();
	}
	
	public Recette getSelected() {
		return refTable.getSelectionModel().getSelectedItem();
	}
	
	public boolean isIndeletion() {
		return indeletion;
	}

	@Override
	public void search(String text, Espece es) {
		// TODO Auto-generated method stub
		ObservableList<Recette> sAlimList=FXCollections.observableArrayList();
		String textSearch= text;

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (Recette al:mainList) {
				if (	 			(	al.getEspece().equals(es)|es.equals(Espece.CH))
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
			for (Recette al:mainList) {
				if (	 			(	al.getEspece().equals(es)|es.equals(Espece.CH))
						) {

			
						sAlimList.add(al);
								

				}
			}    		

		}


	}
		
		}

package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.AnimP;
import DataStruct.AnimP;
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
import model.Espece;
import model.Recette;

public class TableAnimController  implements Initializable, TableInterface {

private ObservableList<AnimP>mainList=FXCollections.observableArrayList();
	
	
	private Stage stage;
	boolean indeletion=false;


	boolean abort=false;
	private ResourceBundle bundle;
	private boolean analyzed=false;


	//Définition graphique 
	   @FXML 
	    private TableView <AnimP> animTable;
	    @FXML 
	    private TableColumn <AnimP, String>specieColumn ; 
	    @FXML 
	    private TableColumn <AnimP, String>breedColumn ;     
	    @FXML 
	    private TableColumn <AnimP, String>animalNameColumn ; 
	    @FXML 
	    private TableColumn <AnimP, String> ownerNameColumn ;
	    @FXML 
	    private TableColumn <AnimP, String> idColumn ; 

	
	


	public  void add(AnimP al) {
		if (al!=null) {
			if (!animTable.getItems().contains(al)) {
		animTable.getItems().add(al);		
		
		animTable.refresh();
		}
}
	
	}

public void add(ObservableList<AnimP> ol) {
	for (AnimP al:ol) {
	if (al!=null) {
		if (!animTable.getItems().contains(al)) {
	animTable.getItems().add(al);		
	
	}
}}
	
	animTable.refresh();

}

public void delete() {
	indeletion=true;
	if(animTable.getSelectionModel().getSelectedItem()!=null) {
animTable.getItems().remove(animTable.getSelectionModel().getSelectedItem());

indeletion=false;
		animTable.refresh();
		
		
	}
}


public void deleteAll() {
	indeletion=true;
	animTable.getItems().clear();
	animTable.refresh();
	indeletion=false;

}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl
		

          animTable.setEditable(false);

         specieColumn.setCellValueFactory(new PropertyValueFactory<>("Specie"));
         breedColumn.setCellValueFactory(new PropertyValueFactory<>("Breed"));
         animalNameColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));
         ownerNameColumn.setCellValueFactory(new PropertyValueFactory<>("Owner"));
         idColumn.setCellValueFactory(new PropertyValueFactory<>("ID"));
    

	}
	
	public void setItems(ObservableList<AnimP> listA)  {
	animTable.setItems( listA);
	
	}
public TableView<AnimP> getAlimTable() {
	return animTable;
}
	
	public void setMainApp( Stage stage, boolean analyzed) {
		
		this.stage=stage;
		this.analyzed=analyzed;
		if (!analyzed) {
			animTable.getColumns().remove(idColumn);
		}
		
	}


	public ObservableList<AnimP> getData() {
	return this.animTable.getItems();
	}
	
	public AnimP getSelected() {
		return animTable.getSelectionModel().getSelectedItem();
	}
	
	public boolean isIndeletion() {
		return indeletion;
	}

	@Override
	public void search(String text, Espece es) {
		// TODO Auto-generated method stub
		ObservableList<AnimP> sAlimList=FXCollections.observableArrayList();
		String textSearch= text;

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (AnimP al:mainList) {
				if (			al.isSpecie(es.getUUID())) {
				    
					boolean prs=true;
					for (String word: words){
						if ((al.getNom().toLowerCase().indexOf(word.toLowerCase())!=-1)|
								(al.getBreed().toLowerCase().indexOf(word.toLowerCase())!=-1)|
						(al.getID().toLowerCase().indexOf(word.toLowerCase())!=-1)|
						(al.getOwner().toLowerCase().indexOf(word.toLowerCase())!=-1)){
									prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sAlimList.add(al);
					}					

				}
	    	}    		
	    		
	    	}else {
	    	 	for (AnimP al:mainList) {
	        		if (			al.isSpecie(es.getUUID())) {
	    		
	        			sAlimList.add(al);
	        		}
	    		
	    	}
	    	}
	    	


	}
		
		}

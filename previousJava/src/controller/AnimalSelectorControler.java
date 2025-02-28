package controller;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import DataStruct.AnimP;
import DataStruct.SpeciesConverter;
import application.VetNutri;
import graph.component.StringCellbundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.Espece;
import model.ReferenceEv;

public class AnimalSelectorControler  implements Initializable {
private String RatUUID;
private String ConsUUID;
private  VetNutri mainApp;
private ObservableList<AnimP> mAnimList;

private Stage stage;
private String[] _brandList={"Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola"};
private Set<String> brandList = new HashSet<>(Arrays.asList(_brandList));
private AutoCompletionBinding<String> autoCompletionBinding;

private ResourceBundle bundle;

@FXML 
private TextField FoodFamiliName;
@FXML
private ButtonBar baseButtonBar;
@FXML
private ComboBox<Espece> speciesCombo;
    //specifique du selecteur
    @FXML
    private Button openAnimButton;
    @FXML
    private TextField searchText;
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
    
    
    @FXML 
    private  void validate() {
    	mainApp.save();
   String UUIDtrans= animTable.getSelectionModel().getSelectedItem().getUUID();
   if (UUIDtrans!=null) {
mainApp.setAnimal(UUIDtrans);
	 stage.close();
   }else {
	 
	   stage.close();
   }

    }
    @FXML
    private void cancel() {
    	stage.close();
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
    
     	Callback<TableColumn<AnimP, String>, TableCell<AnimP, String>> stringCellFactory
		= (TableColumn<AnimP, String> param) -> new StringCellbundle<AnimP>(bundle);
		
		specieColumn.setCellFactory(stringCellFactory);
         speciesCombo.getItems().addAll(Espece.values());
      	speciesCombo.getSelectionModel().select(0);
      	speciesCombo.setConverter(new SpeciesConverter(bundle));
        
        
     	speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
     		 animTable.setItems( searchAlimList());
     	}); 
        searchText.textProperty().addListener((observable, oldValue, newValue) -> {
            animTable.setItems(searchAlimList());
        });
    }
          
          
      
    public void Update()  {
    	 animTable.setItems( searchAlimList() );
    }
    
   
    public void setMainApp(VetNutri mainApp, Stage stage, boolean edition) {
    	this.mainApp=mainApp;
    	this.mAnimList=mainApp.getmAnimList();
    	this.stage=stage;
    
        
       ObservableList<AnimP> listA=null;
       listA =  mainApp.getmAnimList();

			
			 animTable.setItems(searchAlimList());
	

   	  }
   
    
    private ObservableList<AnimP> searchAlimList()  {
    	ObservableList<AnimP> sAlimList=FXCollections.observableArrayList();
    	String textSearch= searchText.getText();
    	
    	if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
    	for (AnimP al:mAnimList) {
    		
    		if (			al.isSpecie(speciesCombo.getSelectionModel().getSelectedItem().getUUID())) {
    
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
    	 	for (AnimP al:mAnimList) {
        		if (			al.isSpecie(speciesCombo.getSelectionModel().getSelectedItem().getUUID())) {
        		
    		
        			sAlimList.add(al);
        		}
    		
    	}
    	}
    	
    	
    	return sAlimList;
    }
    
    
    	
    


    
    
    
}

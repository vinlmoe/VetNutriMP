package controller;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import DataStruct.SpeciesConverter;
import application.DataConnector;
import equation.Equation;
import graph.component.AutocompletionlTextField;
import graph.component.FloatEditingCell;
import graph.component.StringEditingCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import model.AnimalEv;
import model.Espece;
import model.Sex;
import model.targetAdjust;

public class NewAnimalController  implements Initializable {

private ObservableList<CoefP>list=FXCollections.observableArrayList();
	private AnimalEv anim;
	private Stage stage;
	boolean indeletion=false;
	boolean abort=false;
	private ResourceBundle bundle;
	private boolean analyzed=false;
	
	//Définition graphique 
	
	
	
private AutocompletionlTextField breedText= new AutocompletionlTextField();
@FXML
private TextField ownerNameText;
@FXML
private TextField animalNameText;
@FXML
private  TextField identText;
@FXML
private DatePicker birthDatePicker; 
@FXML
private ComboBox<Espece> speciesCombo;
@FXML
private ComboBox<Sex>sexCombo;
@FXML
private GridPane descoGridPane;

@FXML
public void cancel() {
	anim=null;
	stage.close();
}


@FXML
public void Validate() {
	boolean continuer =true;
	anim=new AnimalEv();
	if (birthDatePicker.getValue()!=null) {
	anim.setDateNaiss(birthDatePicker.getValue());}else {
	 continuer =false;
	}
	if (speciesCombo.getValue()!=null) {
	
	anim.setEspece(speciesCombo.getSelectionModel().getSelectedItem().getUUID());}else {
		 continuer =false;
	}
	if (sexCombo.getValue()!=null) {
	anim.setSex(sexCombo.getSelectionModel().getSelectedItem().getID());}else {
		 continuer =false;
	}
	boolean bResponse=true;


if (continuer) {
	if(!breedText.getText().isEmpty()) {
		boolean touch=breedText.getEntries().contains(breedText.getText());

		

		if (!touch) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle(bundle.getString("UnknownBreed"));
			alert.setHeaderText(bundle.getString("UnknownBreedBegin") + breedText.getText());
			alert.setContentText(bundle.getString("UnknownBreedAddQuestion"));

			// option != null.
			Optional<ButtonType> option = alert.showAndWait();

			if (option.get() == null) {
				bResponse=false;
			} else if (option.get() == ButtonType.OK) {
				
				anim.setRace( DataConnector.AddBreed(anim.getEspece(),  bundle.getLocale().getLanguage().toUpperCase(),breedText.getText(), null));
	
				
			} else if (option.get() == ButtonType.CANCEL) {
				bResponse=false;
				breedText.setText(DataConnector.BreedName(anim.getEspece(),  bundle.getLocale().getLanguage().toUpperCase(), anim.getRace(), null));

			} else {
				bResponse=false;
			}
		}else {
			anim.setRace(DataConnector.BreedID(anim.getEspece(), breedText.getText()));
		}
	}
	
	anim.setNom(animalNameText.getText());
	anim.setNomProprio(ownerNameText.getText());
	anim.setId(identText.getText());
	

	stage.close();}
}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl
	descoGridPane.add(breedText, 1, 4);
	sexCombo.getItems().addAll(Sex.values());
	
	sexCombo.setConverter(   new StringConverter<Sex>() {
        @Override
        public String toString(Sex ta) {
            if (ta == null) {
                return "";
            } else {
                return resources.getString(ta.getLabel());
            }
        }

        @Override
        public Sex fromString(String s) {
            try {
                return Sex.FEMELLE;
            } catch (NumberFormatException e) {
                return null;
            }
        }
    });	
	
for(Espece es:Espece.valuesExcept()) {
	 speciesCombo.getItems().add(es);
	 }
speciesCombo.setConverter(new SpeciesConverter(bundle));
	speciesCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
	if(newValue!=null) {
		breedText.getEntries().clear();
		breedText.getEntries().addAll(DataConnector.readListBreed(newValue.getUUID(), "FR"));}
	});
	

	birthDatePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
	    @Override
	    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
	        if (!newValue){
	            birthDatePicker.setValue(birthDatePicker.getConverter().fromString(birthDatePicker.getEditor().getText()));
	        }
	    }
	});
	}
 public AnimalEv getAnim() {
	return anim;
}
	
	public void setMainApp( Stage stage) {
		
		this.stage=stage;
		this.analyzed=analyzed;
    	this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
         cancel();
            }
        });        
		
	}



	
	public boolean isIndeletion() {
		return indeletion;
	}
		
		}

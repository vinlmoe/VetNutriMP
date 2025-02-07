package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;



import DataStruct.AlimP;
import DataStruct.BiblioP;
import DataStruct.CoefP;
import DataStruct.ConstrainP;
import DataStruct.ConsultP;
import DataStruct.DescP;
import DataStruct.EquationConsP;

import DataStruct.KindEnergyConverter;
import DataStruct.NutrientP;
import DataStruct.NutrientRefP;
import DataStruct.RationP;
import DataStruct.SpeciesConverter;
import DataStruct.SupplementalvariableP;
import DataStruct.ConditionConverter;
import DataStruct.TargetP;
import DataStruct.UnitP;
import DataStruct.WeightDateP;
import Enumerise.AAEnum;
import Enumerise.ConditionEnum;
import Enumerise.EquationKind;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import Enumerise.UnitConcEnum;
import Enumerise.UnitEnum;
import Enumerise.UnitReqEnum;
import Enumerise.VariableKind;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import graph.component.BooleanEditingCellRation;
import graph.component.ComboEditingCellAlim;
import graph.component.ComboEditingCellConstrainConstrain;
import graph.component.ComboEditingCellKindConstrain;
import graph.component.ComboEditingCellUnit;
import graph.component.ComboEditingCellUnitConstrain;
import graph.component.DateEditingCell;
import graph.component.EditingCell;
import graph.component.FloatEditingCell;
import graph.component.FloatEditingCellAlim;
import graph.component.FloatEditingCellNutrient;
import graph.component.StringCellbundle;

import graph.component.StringEditingCellRation;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import model.AdjustSaveEv;
import model.AlimIndic;
import model.AlimentRation;
import model.AlimentUnif;
import model.AnimalEv;
import model.BiblioRef;
import model.ConditionSelection;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.Ration;
import model.Reference;
import model.ReferenceEv;
import model.TypeAlim;

public class MethodEditorControler  implements Initializable {

	private AdjustSaveEv reference;


	private ObservableList<AdjustSaveEv> mRefList;
	private ObservableList<BiblioP> mBiblioList;
	private TargetEditorControler CEC= new TargetEditorControler();
;	

	
	private Stage stage;
	


	private boolean saved=true;

	boolean abort=false;
	private ResourceBundle bundle;
	private boolean dis=false;

@FXML
private Accordion accord;

@FXML
	private VBox vbModif;


	@FXML
	private TextField searchText;
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TableView<AdjustSaveEv> refTable;
	@FXML
	private TableColumn<AdjustSaveEv, String> speciesColumn;

	@FXML
	private TableColumn <AdjustSaveEv, String>nameColumn;

	@FXML
	private ComboBox<Espece> speciesDescCombo;
	@FXML
	private TextField nameText;

	@FXML
	private TextArea descriptionTextArea;






	@FXML 
	private  void addReference() {
		AdjustSaveEv nEqu=new AdjustSaveEv();
		 
		 String transUUID=nEqu.getUUID();
		
		 DataConnector.UpdateMethod(nEqu, null);
		
		
		 mRefList.add(nEqu);


		 Update();
		 refTable.refresh();
		 refTable.getSelectionModel().select(nEqu);
		 
	
	}

	@FXML
	private void duplicate() {
		save();
		if(reference!=null) {
			AdjustSaveEv nEqu;
			nEqu = new AdjustSaveEv( reference);
			String transUUID=nEqu.getUUID();
			 
			 DataConnector.UpdateMethod(nEqu, null);
			
			 mRefList.add(nEqu);


			 Update();
			 refTable.refresh();
			 refTable.getSelectionModel().select(nEqu);
			 
			 
		}
	}
	
@FXML
private void close() {
save();
	
		DataConnector.updateListMethod(mRefList);
	
	stage.close();
}
@FXML
private void delete() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		DataConnector.DeleteMethod(refTable.getSelectionModel().getSelectedItem().getUUID(), null);
		mRefList.remove(refTable.getSelectionModel().getSelectedItem());
		Update();
	}
}


	@FXML
	private boolean save() {

		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
	
if(reference==null) {
	error=true;
	errMess+= "\n "+bundle.getString("equation");
}

		if (error) {
			

		}else {

		
			//reference.setBib(biblioCombo.getValue());
			reference.setName(nameText.getText());
			reference.setDescription(descriptionTextArea.getText());
		
	
			
		
			reference.setEsp(speciesDescCombo.getSelectionModel().getSelectedItem());

reference.setList(CEC.getData());
			
				
				
			
					
		
		
			
			refTable.refresh();
		}
	
		return saved;


	}

	@Override 
	public void initialize(URL location, ResourceBundle resources) {
	
		bundle=resources;
		//AlimTabl

		refTable.setEditable(true);

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("espece"));
	
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));




		refTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualReference(newValue));

		Callback<TableColumn<AdjustSaveEv, String>, TableCell<AdjustSaveEv, String>> stringCellFactory
		= (TableColumn<AdjustSaveEv, String> param) -> new StringCellbundle<AdjustSaveEv>(bundle);
		
		speciesColumn.setCellFactory(stringCellFactory);

	



		 for(Espece ta:Espece.values()) {
       	  speciesCombo.getItems().add(ta);
         }

		 for(Espece ta:Espece.values()) {
	       	  speciesDescCombo.getItems().add(ta);
	         }
			
	
		CEC=(setCoefTable());

		speciesCombo.getSelectionModel().select(0);

		speciesCombo.setConverter(new SpeciesConverter(bundle));
		speciesDescCombo.setConverter(new SpeciesConverter(bundle));

		
		
		speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			refTable.setItems( searchReferenceList());


		}); 


		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			refTable.setItems(searchReferenceList());
		});
		refTable.getSelectionModel().selectedItemProperty().addListener(
				
				(observable, oldValue, newValue) ->{
				
					ActualReference(newValue);});
		
	
	
		       
	} 
	public void Update()  {
		refTable.setItems( searchReferenceList() );
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean dis) {
		this.mainApp=mainApp;
		this.mRefList=mainApp.getMethodList();
		this.stage=stage;
		this.dis=dis;

		
	this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        public void handle(WindowEvent we) {
            System.out.println("Stage is closing");
            close();
        }
    });        

		refTable.setItems(searchReferenceList());

	
	}


	private ObservableList<AdjustSaveEv> searchReferenceList()  {
		ObservableList<AdjustSaveEv> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();
		
		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (AdjustSaveEv al:mRefList) {
			
				if (		al.getEsp().equals(speciesCombo.getSelectionModel().getSelectedItem())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {

					boolean prs=true;
					for (String word: words){
						if (((al.getName().toLowerCase().indexOf(word.toLowerCase())!=-1))){
							prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sEquaList.add(al);
					}					

				}
			}    		
			
		}else {
			
			for (AdjustSaveEv al:mRefList) {
				if (		al.getEsp().equals(speciesCombo.getSelectionModel().getSelectedItem())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {	sEquaList.add(al);
				
				}
			}
		}


		return sEquaList;
	}

	public AdjustSaveEv getData() {
		return reference;
	}

	private void ActualReference(AdjustSaveEv al) {
		
		save();
if (al==null) {
	visibility(false);
}else {
	visibility(true);
}

		if (al!=null & !abort) {

			if (saved==true) { 
			
				reference=al;

				
				nameText.setText(reference.getName());
				descriptionTextArea.setText(reference.getDescription());
			
				speciesDescCombo.getSelectionModel().select((reference.getEsp()));
			
				
				CEC.setList(reference.getList());
				}}
		
		}
		
	private void visibility(boolean b) {
		nameText.setVisible(b);
		speciesDescCombo.setVisible(b);
		
		descriptionTextArea.setVisible(b);
	
			CEC.setVisible(b);
		
	
		
	
	}
private TargetEditorControler setCoefTable () {
    
    	TargetEditorControler coefwin;
   	 FXMLLoader loader = new FXMLLoader();
           loader.setResources(bundle);
           loader.setLocation(VetNutri.class.getResource("/view/methodTable.fxml"));
        
         
   	  try {
			vbModif.getChildren().add((AnchorPane)loader.load());
			  coefwin=loader.getController();
	            coefwin.setMainApp(this, stage, true);
	    return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
   }



}

		
		

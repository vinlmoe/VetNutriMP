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
import javafx.concurrent.Task;
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
import javafx.scene.control.ToggleButton;
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
import javafx.util.StringConverter;
import model.AdjustSaveEv;
import model.AlimIndic;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentUnif;
import model.AnimalEv;
import model.BiblioRef;
import model.ConditionSelection;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.Ration;
import model.Recette;
import model.Reference;
import model.ReferenceEv;
import model.TypeAlim;
import model.targetAdjust;

public class RecipeEditorControler  implements Initializable {

	private Recette recette;


	private ObservableList<Recette> mRefList=FXCollections.observableArrayList();
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
private Button selectButton;
@FXML
private Button addButton;
@FXML
private Button duplicateButton;
@FXML
private Button deleteButton;

@FXML
private Button addAlimButton;

@FXML
private Button deleteAlimButton;


	@FXML
	private TextField searchText;
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TableView<Recette> refTable;
	@FXML
	private TableColumn<Recette, Espece> speciesColumn;

	@FXML
	private TableColumn <Recette, String>nameColumn;

	
	@FXML 
	private TableView <AlimP> alimTable;
	@FXML 
	private TableColumn <AlimP,  String> brandAlimColumn;
	@FXML 
	private TableColumn <AlimP, String> nameAlimColumn;

	@FXML 
	private TableColumn <AlimP, TargetP> adjustAlimColumn;
	
	@FXML
	private ComboBox<Espece> speciesDescCombo;
	@FXML
	private TextField nameText;

	@FXML
	private TextArea descriptionTextArea;






	@FXML 
	private  void add() {
		Recette  nEqu=new Recette("New");
		 
		 String transUUID=nEqu.getUUID();
		
	
		
		 mRefList.add(nEqu);

	
			
		 Update();
		 refTable.refresh();
		 refTable.getSelectionModel().select(nEqu);
		 
	
	}

	@FXML
	private void duplicate() {
		save();
		if(recette!=null) {
			Recette nEqu;
			nEqu = new Recette( recette);
			String transUUID=nEqu.getUUID();	
			 mRefList.add(nEqu);
			 DataConnector.updateRecette(mRefList);
			 Update();
			 refTable.refresh();
			 refTable.getSelectionModel().select(nEqu);	 
		}
	}
	
@FXML
private void close() {
save();
recette=null; 
DataConnector.updateRecette(mRefList);
	stage.close();
}
@FXML
private void select() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		save();
	/*	Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws Exception {
				System.out.println("1B");

				DataConnector.updateRecette(mRefList);
				return null ;
			}
		};


		new Thread(task).run();*/
		System.out.println("2B");
		recette=refTable.getSelectionModel().getSelectedItem();
		System.out.println("3B");
		stage.close();
	}
}

@FXML
private void delete() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("You will delete the following Recipe:  "+refTable.getSelectionModel().getSelectedItem().getName());
		alert.setContentText("Are you sure you want it??");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			DataConnector.DeleteRation(refTable.getSelectionModel().getSelectedItem().getUUID(), null);
			mRefList.remove(refTable.getSelectionModel().getSelectedItem());
			Update();
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	
	
	}
}

@FXML
private void deleteAlim() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		if(alimTable.getSelectionModel().getSelectedItem()!=null) {
		DataConnector.DeleteFood(alimTable.getSelectionModel().getSelectedItem().getUUID(), null);
		alimTable.getItems().remove(alimTable.getSelectionModel().getSelectedItem());
		Update();}
	}
}
@FXML
private void addAlim() {
if (recette!=null& refTable.getSelectionModel().getSelectedItem()!=null) {
mainApp.selectAlimWindow(this);

}
}

public void adderAlim (AlimentEv al) {
	if (recette!=null& refTable.getSelectionModel().getSelectedItem()!=null) {if (al!=null) {
		alimTable.getItems().add(new AlimP(new AlimentRation(al)));
	}
	alimTable.refresh();}
}

	@FXML
	private boolean save() {

		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
	
if(recette==null) {
	error=true;
	errMess+= "\n "+bundle.getString("equation");
}

		if (error) {
			

		}else {

		
			//recette.setBib(biblioCombo.getValue());
			recette.setName(nameText.getText());
			recette.setDescription(descriptionTextArea.getText());
		
	
			
		
			recette.setEspece(speciesDescCombo.getSelectionModel().getSelectedItem());


					
				
				recette.setAlimList(alimTable.getItems());
			
					
		DataConnector.updateRecette(recette);
		
			
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

	



		 for(Espece ta:Espece.values()) {
       	  speciesCombo.getItems().add(ta);
         }

		 for(Espece ta:Espece.values()) {
	       	  speciesDescCombo.getItems().add(ta);
	         }
			
	
		

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
		
		//AlimTable
		Callback<TableColumn<AlimP, TargetP>, TableCell<AlimP, TargetP>> comboStringCellFactory
		= (TableColumn<AlimP, TargetP> param) -> new ComboEditingCellAlim();
		Callback<TableColumn<AlimP, Float>, TableCell<AlimP, Float>> quantityCellFactoryAlim
		= (TableColumn<AlimP, Float> param) -> new FloatEditingCellAlim();

		alimTable.setEditable(true);

		brandAlimColumn.setCellValueFactory(new PropertyValueFactory<>("Brand"));
		nameAlimColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));


		// ==== SUBJECTCOLUMN ===



		adjustAlimColumn.setCellValueFactory(new PropertyValueFactory<>("adjustOn"));

		adjustAlimColumn.setCellFactory( comboStringCellFactory);

		adjustAlimColumn.setOnEditCommit((CellEditEvent<AlimP, TargetP> event) -> {
			TablePosition<AlimP, TargetP> pos = event.getTablePosition();
			TargetP newProp = event.getNewValue();

			int row = pos.getRow();
			event.getTableView().getItems().get(row).setAdjustOn(newProp);

		
		
	
		});

	
		       
	} 
	public void Update()  {
		refTable.setItems( searchReferenceList() );
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean dis) {
		this.mainApp=mainApp;
	//	this.mRefList=mainApp.getmMethodList();
		this.stage=stage;
		this.dis=dis;
mRefList=mainApp.getmRecetteList();
		
	this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
        public void handle(WindowEvent we) {
            System.out.println("Stage is closing");
            close();
        }
    });        

	 if (dis) {
		 selectButton.setVisible(dis);
	 }
		refTable.setItems(searchReferenceList());

	
	}


	private ObservableList<Recette> searchReferenceList()  {
		ObservableList<Recette> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();
		
		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (Recette al:mRefList) {
			
				if (		al.getEspece().equals(speciesCombo.getSelectionModel().getSelectedItem())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
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
			
			for (Recette al:mRefList) {
				if (		al.getEspece().equals(speciesCombo.getSelectionModel().getSelectedItem())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {	sEquaList.add(al);
				
				}
			}
		}


		return sEquaList;
	}

	public Recette getData() {
		return recette;
	}

	private void ActualReference(Recette al) {
		
		save();
if (al==null) {
	visibility(false);
}else {
	visibility(true);
}

		if (al!=null & !abort) {

			if (saved==true) { 
			
				recette=al;

				
				nameText.setText(recette.getName());
				descriptionTextArea.setText(recette.getDescription());
			
				speciesDescCombo.getSelectionModel().select((recette.getEspece()));
			
				
			alimTable.setItems(recette.getAlimList());
				}}
		
		}
		public ObservableList<Recette> getmRefList() {
			return mRefList;
		}
	private void visibility(boolean b) {
		nameText.setVisible(b);
		speciesDescCombo.setVisible(b);
		
		descriptionTextArea.setVisible(b);
	
			alimTable.setVisible(b);
			deleteButton.setVisible(b)
			;
			addAlimButton.setVisible(b)
			;
			deleteAlimButton.setVisible(b);
			
			
		
	
		
	
	}




}

		
		

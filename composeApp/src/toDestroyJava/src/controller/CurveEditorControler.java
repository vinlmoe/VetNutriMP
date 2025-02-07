package controller;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import org.controlsfx.control.CheckListView;
import DataStruct.BiblioP;
import DataStruct.CurveP;
import DataStruct.CurveParamP;
import DataStruct.KindEnergyConverter;
import DataStruct.NutrientP;
import DataStruct.SpeciesConverter;
import DataStruct.UnitP;
import Enumerise.AllNutrient;
import Enumerise.NutrientBase;
import Enumerise.VariableKind;
import application.DataConnector;
import application.VetNutri;
import graph.component.ComboEditingCellUnit;
import graph.component.FloatEditingCellNutrient;
import graph.component.StringCellbundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AlimIndic;
import model.BiblioRef;
import model.Espece;

public class CurveEditorControler  implements Initializable {

	private CurveP equ;
	private ObservableList<CurveP> mEquList;

	private ObservableList<CurveParamP> ParamList;
private ObservableList<BiblioP> mBiblioList;
	private Stage stage;

	private boolean saved=true;

	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;


	//Définition graphique 

@FXML
private VBox vBoxEditor;

	@FXML
	private TextField searchText;
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TableView<CurveP> refTable;
	@FXML
	private TableColumn<CurveP, String> speciesColumn;
	@FXML
	private TableColumn<CurveP, String> refColumn;
	@FXML
	private TableColumn <CurveP, String>nameColumn;

	@FXML
	private ComboBox<Espece> speciesDescCombo;
	@FXML
	private TextField refName;
	@FXML
	private ComboBox<BiblioRef> biblioCombo; 
	
	@FXML
	private TableView<CurveParamP> tableParameter;
	@FXML
	private TableColumn<CurveParamP, String> maxColumn;
	@FXML
	private TableColumn<CurveParamP, String> halfColumn;
	@FXML
	private TableColumn <CurveParamP, String>slopeColumn;

	/*
	@FXML 
	private  void addCurveP() {
		save();
		CurveP nEqu=new CurveP();
		 
		 String transUUID=nEqu.getUUID();
		 
		 DataConnector.UpdateCurveP(nEqu, null);
		 
		 mEquList.add(nEqu);


		 Update();
		 
		 refTable.getSelectionModel().select(nEqu);
	}
/*
	@FXML 
	private  void duplicate() {
		save();
		if (equ!=null) {
			
		
		
		CurveP	 nEqu=(CurveP) equ.clone();
		nEqu.setName("(Dup)"+nEqu.getName());
		 String transUUID=nEqu.getUUID();
		 
		 
		 
		 DataConnector.UpdateCurveP(nEqu, null);
		 
		
		 mEquList.add(nEqu);


		 Update();
		 
		 refTable.getSelectionModel().select(nEqu);
		}
	}

	

@FXML
private void close() {
save();
	
		DataConnector.updateListCurveP(mEquList, null);
	
	stage.close();
}
@FXML
private void delete() {
	if(refTable.getSelectionModel().getSelectedItem()!=null) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Confirmation Dialog");
		alert.setHeaderText("You will delete the Selected rconsultation ");
		alert.setContentText("Are you sure you want it??");

		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK){
			DataConnector.DeleteCurveP(refTable.getSelectionModel().getSelectedItem().getUUID(), null);
			mEquList.remove(refTable.getSelectionModel().getSelectedItem());
			Update();
		} else {
		    // ... user chose CANCEL or closed the dialog
		}
	
	
	}
}


	@FXML
	private boolean save() {

		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
		if(refName.getText()=="") {
			error=true;
			errMess+= "\n "+bundle.getString("Name");
		}
	
if(equ==null) {
	error=true;
	errMess+= "\n "+bundle.getString("CurveP");
}

		if (error) {
			if (equ!=null) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Error");

			alert.setContentText(errMess);

			alert.show();
			}
		}else {


			//equ.setBib(biblioCombo.getValue());
			equ.setName(refName.getText());
			equ.setSpecie(speciesDescCombo.getSelectionModel().getSelectedItem());
				equ.setBib(biblioCombo.getSelectionModel().getSelectedItem());
		
			for (int i=0; i<mEquList.size();i++) {
				if (mEquList.get(i).getUUID().equals(equ.getUUID())) {
					mEquList.set(i, equ);
				
				}
			}
			
			refTable.refresh();
			saved=true;
		}
		return saved;

	}

	@Override 
	public void initialize(URL location, ResourceBundle resources) {
visibility(false);
		bundle=resources;
		//AlimTabl
		

		Callback<TableColumn<CurveParamP, UnitP>, TableCell<CurveParamP, UnitP>> comboStringCellFactory
		= (TableColumn<CurveParamP, UnitP> param) -> new ComboEditingCellUnit();
		Callback<TableColumn<CurveParamP, String>, TableCell<CurveParamP, String>> floatCellFactory
		= (TableColumn<CurveParamP, String> param) -> new FloatEditingCellNutrient();
		BaseNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_BaseNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_BaseNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_BaseNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_BaseNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_BaseNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_BaseNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);


		});
		AmountColumn_BaseNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_BaseNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		UnitColumn_BaseNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);           
			saved=false;});

		
		

		refTable.setEditable(true);

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("especeStr"));
		refColumn.setCellValueFactory(new PropertyValueFactory<>("ref"));
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));




		refTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualCurveP(newValue));

		Callback<TableColumn<CurveP, String>, TableCell<CurveP, String>> stringCellFactory
		= (TableColumn<CurveP, String> param) -> new StringCellbundle(bundle);
		
		speciesColumn.setCellFactory(stringCellFactory);


	
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

			refTable.setItems( searchCurvePList());


		}); 
		
		
		


		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			refTable.setItems(searchCurvePList());
		});
		refTable.getSelectionModel().selectedItemProperty().addListener(
				
				(observable, oldValue, newValue) -> ActualCurveP(newValue));

	
	} 
	public void Update()  {
		refTable.setItems( searchCurvePList() );
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition) {
		this.mainApp=mainApp;
		this.mEquList=mainApp.getmCurvePList();
		this.stage=stage;
		this.edition=edition;

		mBiblioList=mainApp.getmBiblioList();
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
	        public void handle(WindowEvent we) {
	            System.out.println("Stage is closing");
	            close();
	        }
	    });        
		for( BiblioP bp:mBiblioList) {
			biblioCombo.getItems().add(bp.getBiblio());
		}


		refTable.setItems(searchCurvePList());

	
	}


	private ObservableList<CurveP> searchCurvePList()  {
		ObservableList<CurveP> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (CurveP al:mEquList) {
				if (		al.getSpecie().getUUID().equals(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
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
			for (CurveP al:mEquList) {
				if (		al.getSpecie().getUUID().equals(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
						) {			sEquaList.add(al);
				}

			}
		}


		return sEquaList;
	}

	public CurveP getData() {
		return equ;
	}

	private void ActualCurveP(CurveP al) {
		if (al==null) {
			visibility(false);
		}else {
			visibility(true);
			ActualCurveP(al);
		}
				
			
			}
	private void visibility(boolean b) {
		refName.setVisible(b);
		speciesDescCombo.setVisible(b);
		tableParameter.setVisible(b);
		biblioCombo.setVisible(b);
		
	}*/


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		// TODO Auto-generated method stub
		
	}
		
		}

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

import org.controlsfx.control.CheckListView;

import DataStruct.AlimP;
import DataStruct.BiblioP;
import DataStruct.CoefP;
import DataStruct.ConstrainP;
import DataStruct.ConsultP;
import DataStruct.DescP;
import DataStruct.EquationConsP;
import DataStruct.ReferenceP;
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

public class ReferenceEditorControler  implements Initializable {

	private ReferenceEv reference;
	private ObservableList<EquationConsP> mEqList;

	private ObservableList<ReferenceP> uRefList= FXCollections.observableArrayList();
	private ObservableList<ReferenceP> mRefList;
	private ObservableList<BiblioP> mBiblioList;
	private ArrayList<CoefEditorControler> CEC=new ArrayList<CoefEditorControler> ()
			;	
	private ArrayList<DataRefEditorControler> mainDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> macroDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> minDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> vitamDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> lipDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> anaDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> amaDREC=new ArrayList<DataRefEditorControler>();
	private ArrayList<DataRefEditorControler> otherDREC=new ArrayList<DataRefEditorControler>();

	private Stage stage;
	@FXML
	private CheckListView<Equation> equNutCheck;

	private boolean saved=true;

	boolean abort=false;
	private ResourceBundle bundle;
	private boolean dis=false;

	@FXML
	private Accordion accord;
	@FXML
	private TitledPane modifPane;
	@FXML
	private TitledPane modelPane;
	//Définition graphique 
	@FXML
	private VBox mainGrid;
	@FXML
	private VBox macroGrid;
	@FXML
	private VBox minGrid;
	@FXML
	private VBox vitamGrid;
	@FXML
	private VBox lipGrid;
	@FXML
	private VBox amaGrid;
	@FXML
	private VBox anaGrid;
	@FXML
	private VBox otherGrid;

	@FXML
	private VBox vbModif;
	@FXML
	private VBox vBoxEditor;

	@FXML
	private TextField searchText;
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TableView<ReferenceP> refTable;
	@FXML
	private TableColumn<ReferenceP, String> speciesColumn;

	@FXML
	private TableColumn <ReferenceP, String>nameColumn;

	@FXML
	private ComboBox<Espece> speciesDescCombo;
	@FXML
	private TextField nameText;
	@FXML
	private TextField energyNameText;
	@FXML
	private ComboBox<Equation> MWCombo;
	@FXML
	private  ComboBox<Equation> ENCombo;
	@FXML
	private ComboBox<Equation> DERawCombo;
	@FXML
	private ComboBox<Equation> DEComCombo;
	@FXML
	private TextArea descriptionTextArea;






	@FXML 
	private  void addReference() {
		ReferenceEv nEqu=new ReferenceEv();

		String transUUID=nEqu.getUUID();
		nEqu.setDisease(dis);
		DataConnector.UpdateReference(nEqu, null);
		ReferenceP nEquP=new ReferenceP(nEqu);

		mRefList.add(nEquP);


		Update();
		refTable.refresh();
		refTable.getSelectionModel().select(nEquP);


	}

	@FXML
	private void duplicate() {
		if(refTable.getSelectionModel().getSelectedItem()!=null) {
			ReferenceEv nEqu;
			save();
			try {

				nEqu = reference.clone();
				String transUUID=nEqu.getUUID();

				DataConnector.UpdateReference(nEqu, null);
				ReferenceP nEquP=new ReferenceP(nEqu);
				mRefList.add(nEquP);


				Update();
				refTable.refresh();
				refTable.getSelectionModel().select(nEquP);
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


		}
	}

	@FXML
	private void close() {
		save();

		DataConnector.updateListReference(mRefList);

		stage.close();
	}
	@FXML
	private void delete() {
		if(refTable.getSelectionModel().getSelectedItem()!=null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("You will delete the Selected Need reference ");
			alert.setContentText("Are you sure you want it??");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				DataConnector.DeleteReference(refTable.getSelectionModel().getSelectedItem().getReference().getUUID(), null);
				mRefList.remove(refTable.getSelectionModel().getSelectedItem());
				
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

		if(reference==null) {
			error=true;
			errMess+= "\n "+bundle.getString("equation");
		}

		if (error) {


		}else {

			if ((nameText.getText().isBlank()|(
					energyNameText.getText().isBlank()|
					DEComCombo.getSelectionModel()==null|
					DERawCombo.getSelectionModel()==null|
					ENCombo.getSelectionModel()==null|
					MWCombo.getSelectionModel()==null)&(!dis)))
			{
				reference.setConsistent(0);
			}else
			{
				reference.setConsistent(1);
			}
			//reference.setBib(biblioCombo.getValue());
			reference.setName(nameText.getText());
			reference.setDescription(descriptionTextArea.getText());
			reference.setDisease(dis);
			reference.setSpecies(speciesDescCombo.getSelectionModel().getSelectedItem());
			if (!dis) 				
			{
				reference.setNameEnergy(energyNameText.getText());
				reference.setBEEqu(ENCombo.getSelectionModel().getSelectedItem());
				reference.setBWEqu(MWCombo.getSelectionModel().getSelectedItem());
				reference.setDEcomEqu(DEComCombo.getSelectionModel().getSelectedItem());
				reference.setDErawEqu(DERawCombo.getSelectionModel().getSelectedItem());
				reference.setNutEqu(equNutCheck.getCheckModel().getCheckedItems());	
			}

			for(DataRefEditorControler drec:mainDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:lipDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:macroDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:minDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:vitamDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:anaDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:amaDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}
			for(DataRefEditorControler drec:otherDREC) {
				for (NutrientRefP nrp:drec.getDataNutrient()) {
					reference.setNutrientRef(nrp);
				}
			}

			if (!dis) {
				for(int i=0;i<5;i++) {
					reference.setModk(i, CEC.get(i).getData());
					reference.setGroupName(i,CEC.get(i).getName());
				}}






			int a=0;
			for (int i=0; i<mRefList.size();i++) {
				if (mRefList.get(i).getReference().getUUID().equals(reference.getUUID())) {
					mRefList.get(i).Update(reference);

				}
			}



			refTable.refresh();
		}

		return saved;


	}

	@Override 
	public void initialize(URL location, ResourceBundle resources) {
		visibility(false);
		bundle=resources;
		//AlimTabl

		refTable.setEditable(true);

		speciesColumn.setCellValueFactory(new PropertyValueFactory<>("especeStr"));

		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		Callback<TableColumn<ReferenceP, String>, TableCell<ReferenceP, String>> stringCellFactory
		= (TableColumn<ReferenceP, String> param) -> new StringCellbundle<ReferenceP>(bundle);

		speciesColumn.setCellFactory(stringCellFactory);
		for(Espece ta:Espece.values()) {
			speciesCombo.getItems().add(ta);
		}

		for(Espece ta:Espece.values()) {
			speciesDescCombo.getItems().add(ta);
		}

		for  (int i=0;i<5;i++) {
			CEC.add(setCoefTable());
		}

		speciesCombo.getSelectionModel().select(0);

		speciesCombo.setConverter(new SpeciesConverter(bundle));
		speciesDescCombo.setConverter(new SpeciesConverter(bundle));
		speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			refTable.setItems( searchReferenceList());


		}); 
		speciesDescCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			if (!dis) {
				ENCombo.getItems().clear();
				DERawCombo.getItems().clear();
				DEComCombo.getItems().clear();
				MWCombo.getItems().clear();
				equNutCheck.getItems().clear();
				for(EquationConsP eq:mEqList) {
					if( speciesDescCombo.getSelectionModel().getSelectedItem()!=null) {
						if (eq.getEspeceStr().equals(Espece.CH.getName())|speciesDescCombo.getSelectionModel().getSelectedItem().getName().equals(eq.getEspeceStr())) {

							switch (eq.getEquation().getKind()) {
							case ENERGYNEED:
								ENCombo.getItems().add(eq.getEquation());
								break;
							case ENERGYDENSITY:
								DERawCombo.getItems().add(eq.getEquation());
								DEComCombo.getItems().add(eq.getEquation());
								break;
							case MW:

								MWCombo.getItems().add(eq.getEquation());
								break;
							case NEED:
								equNutCheck.getItems().add(eq.getEquation());
								break;

							case INDICATOR:
								break;
							default:
								break;
							}
						}}}}

		});

		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			refTable.setItems(searchReferenceList());
		});
	
		refTable.setRowFactory(new Callback<TableView<ReferenceP>, TableRow<ReferenceP>>() {
			@Override
			public TableRow<ReferenceP> call(TableView<ReferenceP> tableView) {
				final TableRow<ReferenceP> row = new TableRow<ReferenceP>() {
					@Override
					protected void updateItem(ReferenceP person, boolean empty){
						super.updateItem(person, empty);
						if(person!=null) {
							if (person.getReference().getConsistent()!=1) {
								setStyle("-fx-background-color: lightcoral; "
										);
							}else {
								setStyle("");
							}
						}}
					@Override
					public void updateSelected(boolean b) {
						if (b) {
							setStyle("-fx-background-color: turquoise; "
									);
						}
					}     
				};
				return row;
			}
		});



	} 
	public void Update()  {
		refTable.setItems( searchReferenceList() );
		refTable.refresh();
	}

	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean dis) {
		this.mainApp=mainApp;
		this.mRefList=mainApp.getmReferenceList();
		this.stage=stage;
		this.dis=dis;
		this.mEqList=mainApp.getmEquationList();
		mBiblioList=mainApp.getmBiblioList();
		if (dis) {
			accord.getPanes().remove(modelPane);
			accord.getPanes().remove(modifPane);
			for (int i=0; i<2;i++) {	
				mainDREC.add(setDataRefTable(mainGrid));
				macroDREC.add(setDataRefTable(macroGrid));
				minDREC.add(setDataRefTable(minGrid));
				vitamDREC.add(setDataRefTable(vitamGrid));
				lipDREC.add(setDataRefTable(lipGrid));
				amaDREC.add(setDataRefTable(amaGrid));
				anaDREC.add(setDataRefTable(anaGrid));
				otherDREC.add(setDataRefTable(otherGrid));

			}
		}else {
			for (int i=0; i<4;i++) {	
				mainDREC.add(setDataRefTable(mainGrid));
				macroDREC.add(setDataRefTable(macroGrid));
				minDREC.add(setDataRefTable(minGrid));
				vitamDREC.add(setDataRefTable(vitamGrid));
				lipDREC.add(setDataRefTable(lipGrid));
				amaDREC.add(setDataRefTable(amaGrid));
				anaDREC.add(setDataRefTable(anaGrid));
				otherDREC.add(setDataRefTable(otherGrid));

			}
		}
		refTable.getSelectionModel().selectedItemProperty().addListener(

				(observable, oldValue, newValue) ->{
					mainDREC.forEach(  n->n.unselect());
					macroDREC.forEach(  n->n.unselect());
					minDREC.forEach(  n->n.unselect());
					vitamDREC.forEach(  n->n.unselect());
					lipDREC.forEach(  n->n.unselect());
					amaDREC.forEach(  n->n.unselect());
					anaDREC.forEach(  n->n.unselect());
					otherDREC.forEach(  n->n.unselect());

					ActualReference(newValue);});
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				
				close();
			}
		});        

		refTable.setItems(searchReferenceList());


	}


	private ObservableList<ReferenceP> searchReferenceList()  {
		ObservableList<ReferenceP> sEquaList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();

		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (ReferenceP al:mRefList) {
				if (al.getReference().isDisease()==dis) {
					if (		al.getReference().getSpecies().equals(speciesCombo.getSelectionModel().getSelectedItem().getName())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
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
			}
		}else {

			for (ReferenceP al:mRefList) {
				if (al.getReference().isDisease()==dis) {
					if (		al.getReference().getSpecies().equals(speciesCombo.getSelectionModel().getSelectedItem().getName())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH)
							) {			sEquaList.add(al);
					}
				}
			}
		}


		return sEquaList;
	}

	public ReferenceEv getData() {
		return reference;
	}

	private void ActualReference(ReferenceEv al) {
boolean goo=false;
		save();
		
		if (al==null) {
			visibility(false);
		}else {
			visibility(true);
		}
		
		if (reference!=null) {
if (!reference.getUUID().equals(al.getUUID())) {
	goo=true;
}}else {goo=true;}
if (goo) {
		reference=al;


		nameText.setText(reference.getName());
		descriptionTextArea.setText(reference.getDescription());

		speciesDescCombo.getSelectionModel().select(Espece.getEnumFromString(reference.getSpecies()));
		if(!dis) {
			energyNameText.setText(al.getNameEnergy());

			for (int i =0;i<5;i++) {
				CEC.get(i).setK("k" + (i+1)+" : ");
				CEC.get(i).setList(al.getGroupk(i));
				CEC.get(i).setName(al.getGroupName(i));
			}}
		if (!dis) {
			ENCombo.getItems().clear();
			DERawCombo.getItems().clear();
			DEComCombo.getItems().clear();
			MWCombo.getItems().clear();}
		equNutCheck.getItems().clear();
		if (al!=null & !abort) {

			if (saved==true) { 
				if(!dis) {
					for(EquationConsP eq:mEqList) {
						if( speciesDescCombo.getSelectionModel().getSelectedItem()!=null) {
							if (eq.getEspeceStr().equals(Espece.CH.getName())|speciesDescCombo.getSelectionModel().getSelectedItem().getName().equals(eq.getEspeceStr())) {

								switch (eq.getEquation().getKind()) {
								case ENERGYNEED:
									ENCombo.getItems().add(eq.getEquation());
									break;
								case ENERGYDENSITY:
									DERawCombo.getItems().add(eq.getEquation());
									DEComCombo.getItems().add(eq.getEquation());
									break;
								case MW:

									MWCombo.getItems().add(eq.getEquation());
									break;

								case NEED:
									equNutCheck.getItems().add(eq.getEquation());
									break;
								case INDICATOR:
									break;
								default:
									break;

								}

							}
						}
					}

				
					MWCombo.getSelectionModel().select(reference.getBWEqu());					
					ENCombo.getSelectionModel().select(reference.getBEEqu());
					DERawCombo.getSelectionModel().select(reference.getDErawEqu());
					DEComCombo.getSelectionModel().select(reference.getDEcomEqu());	
				
			
					for (Equation eq:reference.getNutEqu()) {
						for(Equation ew :equNutCheck.getItems()) {
							if (ew.getUUID().equals(eq.getUUID())) {
								equNutCheck.getCheckModel().check(ew);
						}					
					}}
				}
				Reflevel r=Reflevel.MIN;
				mainDREC.get(r.getCoef()).setBiblio(mBiblioList);
				minDREC.get(r.getCoef()).setBiblio(mBiblioList);
				macroDREC.get(r.getCoef()).setBiblio(mBiblioList);
				amaDREC.get(r.getCoef()).setBiblio(mBiblioList);
				anaDREC.get(r.getCoef()).setBiblio(mBiblioList);
				vitamDREC.get(r.getCoef()).setBiblio(mBiblioList);
				otherDREC.get(r.getCoef()).setBiblio(mBiblioList);
				lipDREC.get(r.getCoef()).setBiblio(mBiblioList);
				mainDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.BASE, al,r);
				minDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MIN, al,r);
				macroDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MACRO, al,r);
				amaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.AMA, al,r);
				anaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.ANA, al,r);
				vitamDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.VITAM, al,r);
				otherDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.OTHER, al,r);
				lipDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.LIPID, al,r);
				r=Reflevel.MAX;
				mainDREC.get(r.getCoef()).setBiblio(mBiblioList);
				minDREC.get(r.getCoef()).setBiblio(mBiblioList);
				macroDREC.get(r.getCoef()).setBiblio(mBiblioList);
				amaDREC.get(r.getCoef()).setBiblio(mBiblioList);
				anaDREC.get(r.getCoef()).setBiblio(mBiblioList);
				vitamDREC.get(r.getCoef()).setBiblio(mBiblioList);
				otherDREC.get(r.getCoef()).setBiblio(mBiblioList);
				lipDREC.get(r.getCoef()).setBiblio(mBiblioList);
				mainDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.BASE, al,r);
				minDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MIN, al,r);
				macroDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MACRO, al,r);
				amaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.AMA, al,r);
				anaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.ANA, al,r);
				vitamDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.VITAM, al,r);
				otherDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.OTHER, al,r);
				lipDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.LIPID, al,r);
				if (!dis) {
					r=Reflevel.OPTIMIN;
					mainDREC.get(r.getCoef()).setBiblio(mBiblioList);
					minDREC.get(r.getCoef()).setBiblio(mBiblioList);
					macroDREC.get(r.getCoef()).setBiblio(mBiblioList);
					amaDREC.get(r.getCoef()).setBiblio(mBiblioList);
					anaDREC.get(r.getCoef()).setBiblio(mBiblioList);
					vitamDREC.get(r.getCoef()).setBiblio(mBiblioList);
					otherDREC.get(r.getCoef()).setBiblio(mBiblioList);
					lipDREC.get(r.getCoef()).setBiblio(mBiblioList);
					mainDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.BASE, al,r);
					minDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MIN, al,r);
					macroDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MACRO, al,r);
					amaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.AMA, al,r);
					anaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.ANA, al,r);
					vitamDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.VITAM, al,r);
					otherDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.OTHER, al,r);
					lipDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.LIPID, al,r);
					r=Reflevel.OPTIMAX;
					mainDREC.get(r.getCoef()).setBiblio(mBiblioList);
					minDREC.get(r.getCoef()).setBiblio(mBiblioList);
					macroDREC.get(r.getCoef()).setBiblio(mBiblioList);
					amaDREC.get(r.getCoef()).setBiblio(mBiblioList);
					anaDREC.get(r.getCoef()).setBiblio(mBiblioList);
					vitamDREC.get(r.getCoef()).setBiblio(mBiblioList);
					otherDREC.get(r.getCoef()).setBiblio(mBiblioList);
					lipDREC.get(r.getCoef()).setBiblio(mBiblioList);
					mainDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.BASE, al,r);
					minDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MIN, al,r);
					macroDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.MACRO, al,r);
					amaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.AMA, al,r);
					anaDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.ANA, al,r);
					vitamDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.VITAM, al,r);
					otherDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.OTHER, al,r);
					lipDREC.get(r.getCoef()).setDataNutrient(MainNutrientEnum.LIPID, al,r);
				}
			}
		}}
	}
	private void ActualReference(ReferenceP al) {
		if (al==null) {
			visibility(false);
		}else {
			visibility(true);
			ActualReference(al.getReference());
		}


	}
	private void visibility(boolean b) {
		nameText.setVisible(b);
		speciesDescCombo.setVisible(b);

		descriptionTextArea.setVisible(b);
		for(CoefEditorControler cec: CEC) {
			cec.setVisible(b);
		}



	}
	private CoefEditorControler setCoefTable () {

		CoefEditorControler coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/coefTable.fxml"));


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

	private DataRefEditorControler setDataRefTable (VBox vb) {

		DataRefEditorControler datawin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/dataNeedTable.fxml"));


		try {
			vb.getChildren().add((AnchorPane)loader.load());
			datawin=loader.getController();
			datawin.setMainApp(this, stage, true);
			return datawin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


}




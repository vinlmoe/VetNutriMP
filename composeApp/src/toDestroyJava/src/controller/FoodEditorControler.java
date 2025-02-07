package controller;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.CheckListView;
import org.controlsfx.control.ListSelectionView;
import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import DataStruct.AlimP;
import DataStruct.ConstrainP;
import DataStruct.ConsultP;
import DataStruct.NutrientP;
import DataStruct.RationP;
import DataStruct.SpeciesConverter;
import DataStruct.ConditionConverter;
import DataStruct.TargetP;
import DataStruct.UnitP;
import DataStruct.WeightDateP;
import Enumerise.AAEnum;
import Enumerise.ConditionEnum;
import Enumerise.ContEnum;
import Enumerise.FoodKind;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.PriceCateg;
import Enumerise.UnitConcEnum;
import Enumerise.UnitEnum;
import application.DataConnector;
import application.GFun;
import application.VetNutri;
import graph.component.AutocompletionlTextField;
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
import graph.component.StringEditingCell;
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
import javafx.scene.Scene;
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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
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
import javafx.scene.control.skin.TableViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AlimIndic;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentEv;
import model.AnimalEv;
import model.ConditionSelection;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.Ration;
import model.Reference;
import model.TypeAlim;
import model.Vet;
import model.alimDB;

public class FoodEditorControler  implements Initializable {
	private AlimP transit;
	private AlimentEv alimU;
	private ObservableList<AlimP> mAlimList;
	private AlimP alAc;
	private Stage stage;
	private Vet vet;

	private boolean saved=true;
	private ObservableList<ConstrainP> constrains= FXCollections.observableArrayList();
	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;
	@FXML
	private BorderPane mainPane; 
	@FXML 
	private VBox VBmodif;
	private FoodSearchController FSC;
	private TableFoodController TFC;
	@FXML
	private GridPane descGridPane;
	@FXML 
	private ComboBox<GroupAlim> foodFamilyCombo;
	@FXML
	private ComboBox<alimDB> comboDB;
	@FXML 
	private ComboBox<PriceCateg> priceCombo;
@FXML 
private CheckBox checkDeprecated;
@FXML
private Label DataLab;
@FXML
private Label AlimLab;
	@FXML
	private ButtonBar baseButtonBar;
	@FXML 
	private CheckListView<Espece> SpeciesCheck;
	@FXML
	private TabPane genTabPane;
	@FXML 
	private ComboBox<FoodKind> dbCombo; 
	@FXML 
	private ComboBox<FoodKind> foodKindCombo; 
	//specifique du selecteur
	@FXML
	private Button selectFoodButton;
	@FXML
	private Button selectFoodButton1;
	@FXML
	private Button selectFoodButton2;
	//Specifique de l'éditeur.
	@FXML
	private Button addFoodButton;

	@FXML
	private Button duplicateButton;
	@FXML
	private Button saveFoodButton;
	@FXML
	private Tab editCompositionTab;
	@FXML
	private Tab editGeneralTab;

	private AutocompletionlTextField FoodBrandName= new AutocompletionlTextField();
	@FXML
	private TextField FoodNameText;
	@FXML
	private TextField rangeText;
	@FXML
	private ComboBox BaseCombo;
	@FXML
	private ComboBox MacroCombo;
	@FXML
	private ComboBox VitamCombo;
	@FXML
	private ComboBox MinCombo;
	@FXML
	private ComboBox LipCombo;
	@FXML
	private ComboBox OtherCombo;
	@FXML
	private ComboBox AMACombo;
	@FXML 
	private ListSelectionView <AlimIndic>ListIndic;

@FXML
private Accordion AccordGen;
@FXML
private TitledPane GenInfo;
	@FXML
	private PieChart RawCompo; 
	@FXML
	private PieChart EnerCompo; 

@FXML
private TextField quantContText;
@FXML
private ComboBox<ContEnum> contenantCombo;


	@FXML
	private TableView<NutrientP>BaseNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_BaseNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_BaseNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_BaseNutrientTable;

	@FXML
	private TableView<NutrientP>MacroNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_MacroNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_MacroNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_MacroNutrientTable;




	@FXML
	private TableView<NutrientP>LipNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_LipNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_LipNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_LipNutrientTable;
	

	@FXML
	private TableView<NutrientP>OtherNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_OtherNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_OtherNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_OtherNutrientTable;

	@FXML
	private TableView<NutrientP>MinNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_MinNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_MinNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_MinNutrientTable;

	@FXML
	private TableView<NutrientP>VitamNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_VitamNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_VitamNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_VitamNutrientTable;

	@FXML
	private TableView<NutrientP>AmaNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> NutrientColumn_AmaNutrientTable;
	@FXML
	private TableColumn<NutrientP, String> AmountColumn_AmaNutrientTable;
	@FXML
	private TableColumn<NutrientP, UnitP> UnitColumn_AmaNutrientTable;
	@FXML
	private TextArea IngredientsTextArea;

	@FXML
	private AnchorPane anchorgraph;
	@FXML 
	private  void addFood() {
		AlimentEv nAlim=new AlimentEv();
		String transUUID=nAlim.getUUID();
		DataConnector.UpdateAlim(nAlim, null);
	AlimP al=	new AlimP(nAlim);
	mainApp.addAlim(al);
Update();
		TFC.getAlimTable().getSelectionModel().select(al);
		scrollSelect() ;
	}
	@FXML
	private void createDB() {
	
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/newDB.fxml"));
		try {
			VBox rt=(VBox)loader.load();
			//rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
			Scene scene = new Scene(rt);
		Stage	MethodStage=new Stage();
		createDBController coefwin = loader.getController();
			coefwin.setMainApp(MethodStage, this);

			MethodStage.setScene(scene);
			MethodStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window

			MethodStage.showAndWait();


			coefwin=loader.getController();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@FXML
	private void delete() {
		if(TFC.getAlimTable().getSelectionModel().getSelectedItem()!=null) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("You will delete the following food:  "+TFC.getAlimTable().getSelectionModel().getSelectedItem().getNom());
			alert.setContentText("Are you sure you want it??");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				DataConnector.DeleteFood(TFC.getAlimTable().getSelectionModel().getSelectedItem().getUUID(), null);
				mainApp.removeAlim(TFC.getAlimTable().getSelectionModel().getSelectedItem());
				ActualAlim(null,null);
				Update();
			} else {
			    // ... user chose CANCEL or closed the dialog
			}
		
		}
	}

	@FXML 
	private  void validate() {
		
		if (TFC.getSelected()!=null) {
			if(recipeCont!=null) {
				recipeCont.adderAlim(TFC.getSelected().getAlim());
			}else {
			mainApp.addAlimCons(TFC.getSelected().getAlim(), false);}}
		

	


	
	}
	@FXML 
	private  void validateNew() {
		
		if (TFC.getSelected()!=null) {
			if(recipeCont!=null) {
				recipeCont.adderAlim(TFC.getSelected().getAlim());
			}else {
			mainApp.addAlimCons(TFC.getSelected().getAlim(), true);}}
	}
	@FXML 
	private  void validateAndClose() {
		
		if (TFC.getSelected()!=null) {
			if(recipeCont!=null) {
				recipeCont.adderAlim(TFC.getSelected().getAlim());
			}else {
				mainApp.addAlimCons(TFC.getSelected().getAlim(), false);}
			
			stage.close();}

	
	}

	@FXML
	private void duplicate() {
		saveAlim();
		if (TFC.getAlimTable().getSelectionModel().getSelectedItem()!=null) {


			AlimentEv nAlim=alimU.clone();

			String transUUID=nAlim.getUUID();

			DataConnector.UpdateAlim(nAlim, null);
			AlimP al=	new AlimP(nAlim);
	mainApp.addAlim(al);
			Update();

			TFC.getAlimTable().getSelectionModel().select(al);
			scrollSelect();
		}
	}
	@FXML
	private void close() {

		if (edition) {
			this.saveAlim();
			stage.close();
		}else {
			alimU=null;
			stage.close();
		}

	}

	@FXML
	private boolean saveAlim() {
if (alimU!=null) {
		String errMess=bundle.getString("MissingValueErrorMessage");

		boolean error=false;
		if(getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.PROTEINE)=="") {
			error=true;
			errMess+= "\n "+bundle.getString(NutrientBase.PROTEINE.getLabel());
		}
		if(getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.HUMIDITE)=="") {
			error=true;
			errMess+= "\n "+bundle.getString(NutrientBase.HUMIDITE.getLabel());
		}
		if(getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.LIPIDE)=="") {
			error=true;
			errMess+= "\n "+bundle.getString(NutrientBase.LIPIDE.getLabel());
		}
	
		if((getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.CELLULOSE)=="")& getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.FIBRETOT)=="") {
			error=true;
			errMess+= "\n "+bundle.getString(NutrientBase.CELLULOSE.getLabel());
			errMess+= "\n "+bundle.getString(NutrientBase.FIBRETOT.getLabel());

		}
		if(FoodNameText.getText().equals("")) {
			error=true;
			errMess+= "\n "+bundle.getString("Name");
		}

		if (error) {
			
			alimU.setConsistent(0);
			

		}else {alimU.setConsistent(1);}


		updateNutrient(BaseNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)BaseCombo.getSelectionModel().getSelectedItem()), 
				0);
		updateNutrient(MacroNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)MacroCombo.getSelectionModel().getSelectedItem()), 
				0);
		updateNutrient(MinNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)MinCombo.getSelectionModel().getSelectedItem()), 
				0);
		updateNutrient(VitamNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)VitamCombo.getSelectionModel().getSelectedItem()), 
				0);
		updateNutrient(LipNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)LipCombo.getSelectionModel().getSelectedItem()), 
				0);
		updateNutrient(OtherNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)OtherCombo.getSelectionModel().getSelectedItem()), 
				0);
		if(getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.PROTEINE)!=""){
			updateNutrient(AmaNutrientTable.getItems(), UnitConcEnum.getConversionByName(bundle, (String)AMACombo.getSelectionModel().getSelectedItem()), 
					Float.parseFloat(getValOfNutrient(BaseNutrientTable.getItems(), NutrientBase.PROTEINE)));}
if (comboDB.getSelectionModel().getSelectedItem()!=null) {
	alimU.setDataB(comboDB.getSelectionModel().getSelectedItem().getUUID());
}else {
	alimU.setDataB("4");
}
		alimU.setNom(FoodNameText.getText());
		List<String> inds=new ArrayList<String>();
		for(AlimIndic ind:ListIndic.getTargetItems()) {
			inds.add(ind.nameToString());

		}
		ArrayList<String> esps=new ArrayList<String>();
		for(Espece esp:SpeciesCheck.getCheckModel().getCheckedItems()) {
			esps.add(esp.getUUID());

		}
		alimU.setIndicat(inds);

		alimU.setMarque(FoodBrandName.getText());
		if (priceCombo.getSelectionModel().getSelectedItem()!=null) {
			alimU.setCategoriePrix(priceCombo.getSelectionModel().getSelectedItem().getId());}

		if (foodFamilyCombo.getSelectionModel().getSelectedItem()!=null) {
			alimU.setGroup(foodFamilyCombo.getSelectionModel().getSelectedItem());}
		if (foodFamilyCombo.getSelectionModel().getSelectedItem()!=null) {
			alimU.setGroup(foodFamilyCombo.getSelectionModel().getSelectedItem());}
		if (foodKindCombo.getSelectionModel().getSelectedItem()!=null) {
			alimU.setTypeAliment (foodKindCombo.getSelectionModel().getSelectedItem());}
		alimU.setGamme(rangeText.getText());
		alimU.setEspeces(esps);
		alimU.setIngredients(IngredientsTextArea.getText());
		alimU.setDeprecated(checkDeprecated.isSelected());
		if(quantContText.getText().isBlank()) {
			alimU.setQuantInt(0);
		}else {
		alimU.setQuantInt(Float.parseFloat(GFun.noPoint(quantContText.getText())));}
		alimU.setCont(contenantCombo.getSelectionModel().getSelectedItem());
		alAc.setNom(alimU.getNom());
		alAc.setBrand(alimU.getFamillyBrand());
		alAc.setRange(alimU.getGamme());
		alAc.setAlim(alimU);

		TFC.getAlimTable().refresh();
		DataConnector.UpdateAlim(alimU,null);
		saved=true;

		return saved;
}else {
	return false;
}

	}

	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl


		descGridPane.add(FoodBrandName, 1,3);


		Callback<TableColumn<NutrientP, UnitP>, TableCell<NutrientP, UnitP>> comboStringCellFactory
		= (TableColumn<NutrientP, UnitP> param) -> new ComboEditingCellUnit();
		Callback<TableColumn<NutrientP, String>, TableCell<NutrientP, String>> floatCellFactory
		= (TableColumn<NutrientP, String> param) -> new FloatEditingCellNutrient();
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

		VitamNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_VitamNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_VitamNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_VitamNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_VitamNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_VitamNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_VitamNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});

		UnitColumn_VitamNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_VitamNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_VitamNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		MacroNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_MacroNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_MacroNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_MacroNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_MacroNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_MacroNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_MacroNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});

		UnitColumn_MacroNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_MacroNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_MacroNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		MinNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_MinNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_MinNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_MinNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_MinNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_MinNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_MinNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});

		UnitColumn_MinNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_MinNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_MinNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		LipNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_LipNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_LipNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_LipNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_LipNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_LipNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_LipNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});

		UnitColumn_LipNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_LipNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_LipNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		
		
		
		OtherNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_OtherNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_OtherNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_OtherNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_OtherNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_OtherNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_OtherNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});

		UnitColumn_OtherNutrientTable.setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_OtherNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_OtherNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});
		
		
		AmaNutrientTable.setEditable(true);
		//Nutrient Tables
		NutrientColumn_AmaNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Nom"));
		AmountColumn_AmaNutrientTable.setCellValueFactory(new PropertyValueFactory<>("quantity"));
		UnitColumn_AmaNutrientTable.setCellValueFactory(new PropertyValueFactory<>("Unit"));
		UnitColumn_AmaNutrientTable .setCellFactory( comboStringCellFactory);
		AmountColumn_AmaNutrientTable.setCellFactory(floatCellFactory);
		AmountColumn_AmaNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, String> event) -> {
			TablePosition<NutrientP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			saved=false;

		});
		UnitColumn_AmaNutrientTable .setOnEditCommit((CellEditEvent<NutrientP, UnitP> event) -> {
			TablePosition<NutrientP, UnitP> pos = event.getTablePosition();
			UnitP value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);          
			saved =false ;});
		AmountColumn_AmaNutrientTable.setOnEditStart((CellEditEvent<NutrientP, String> event) -> {saved=false;});
		UnitColumn_AmaNutrientTable.setOnEditStart((CellEditEvent<NutrientP, UnitP> event) -> {saved=false;});




		SpeciesCheck.getItems().addAll(Espece.valuesExcept());

contenantCombo.getItems().addAll(ContEnum.values());

contenantCombo.setConverter(new StringConverter<ContEnum>()
{
	
    // Method to convert a Person-Object to a String
@Override
public String toString(ContEnum person)
{
    return person == null? null :("g/"+ bundle.getString(person.getName()));
}

// Method to convert a String to a Person-Object
@Override
public ContEnum fromString(String string)
{

    return ContEnum.NO;
}
});


foodKindCombo.setConverter(new StringConverter<FoodKind>()
{
	
    // Method to convert a Person-Object to a String
@Override
public String toString(FoodKind person)
{
    return person == null? null :( bundle.getString(person.toString()));
}

// Method to convert a String to a Person-Object
@Override
public FoodKind fromString(String string)
{

    return FoodKind.ALL;
}
});
comboDB.setConverter(new StringConverter<alimDB>()
{
	
    
@Override
public String toString(alimDB person)
{
    return person == null? null :( person.getsNom());
}

// Method to convert a String to a Person-Object
@Override
public alimDB fromString(String string)
{

    return new alimDB();
}
});
		ObservableList<String> Refe    = FXCollections.observableArrayList();

		Refe.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refe.add( bundle.getString(UnitConcEnum.BUkg.getName()));
		ObservableList<String> Refeprot   = FXCollections.observableArrayList();
		Refeprot.add( bundle.getString(UnitConcEnum.Prot.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BUkg.getName()));

		BaseCombo.setItems(Refe);
		BaseCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		MacroCombo.setItems(Refe);
		MacroCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		MinCombo.setItems(Refe);
		MinCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		VitamCombo.setItems(Refe);
		VitamCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		LipCombo.setItems(Refe);
		LipCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		OtherCombo.setItems(Refe);
		OtherCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
		AMACombo.setItems(Refeprot);
		AMACombo.setValue(bundle.getString(UnitConcEnum.Prot.getName()));
		priceCombo.getItems().setAll(PriceCateg.values());
		foodFamilyCombo.getItems().setAll(GroupAlim.valuesExcept());
		foodKindCombo.getItems().setAll(FoodKind.valuesExcept());
		foodKindCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue!=null) {
				switch(newValue) {

				case BARF:
				case MEN:
					priceCombo.setVisible(false);
					foodFamilyCombo.setVisible(true);
					rangeText.setVisible(false);
					FoodBrandName.setVisible(false);
					break;
				case COMPLEMENTAIRE:
				case COMPLET:
					priceCombo.setVisible(true);
					foodFamilyCombo.setVisible(false);
					rangeText.setVisible(true);
					FoodBrandName.setVisible(true);
					break;
				default:
					break;
				}
			}
		});	
		ListIndic.setCellFactory(col -> {   return new ListCell<AlimIndic>() {
			@Override
			protected void updateItem(AlimIndic item, boolean empty) {
				super.updateItem(item, empty);

				if (item == null || empty) {
					setText(null);
				} else {
					setText(bundle.getString(item.name()));
				}
			}
		};
		});
		SpeciesCheck  .setCellFactory(listView -> new CheckBoxListCell<Espece>(  SpeciesCheck::getItemBooleanProperty) {
			@Override
			public void updateItem(Espece employee, boolean empty) {
				super.updateItem(employee, empty);
				setText(employee == null ? "" : resources.getString( employee.getName()));
			}
		});
		RawCompo.setLegendSide(Side.LEFT);
		RawCompo.setTitle(resources.getString("RawCompoTitle"));
		EnerCompo.setTitle(resources.getString("EnerCompoTitle"));
		AccordGen.setExpandedPane(GenInfo);	 
		setVisible(false);
	} 
	public void Update()  {
		TFC.getAlimTable().setItems( FSC.searchAlimList() );
	}

	VetNutri mainApp;
	
	MainWinController mainCont=null;
	RecipeEditorControler recipeCont=null;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, Vet vet) {
		this.mainApp=mainApp;
		this.mAlimList=mainApp.getmAlimList();
		this.stage=stage;
		this.edition=edition;
		this.vet=vet;

setter();
	}
	
	private void setter() {

		ObservableList<AlimP> listA=null;
		listA =  mainApp.getmAlimList();
		ArrayList<String> str=new ArrayList<String>();
		for(int i=0; i<listA.size(); i++) {
			if ( listA.get(i).getAlim().getTypeAliment().equals(FoodKind.COMPLEMENTAIRE)|listA.get(i).getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
				str.add(  listA.get(i).getBrand());}
		}
		TFC=setFoodTable(VBmodif, false);
		FSC=setSearchBand(mainPane, false);
		;		
		FoodBrandName.getEntries().clear();

		FoodBrandName.getEntries().addAll(str);
		comboDB.getItems().setAll(vet.getAlDBL().values());
		TFC.getAlimTable().setItems(FSC.searchAlimList());
		TFC.getAlimTable().getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) ->{
					BaseNutrientTable.edit(-1, null);
					MacroNutrientTable.edit(-1, null);
					MinNutrientTable.edit(-1, null);
					VitamNutrientTable.edit(-1, null);
					LipNutrientTable.edit(-1, null);
					OtherNutrientTable.edit(-1, null);
					AmaNutrientTable.edit(-1, null);
					ActualAlim(newValue,oldValue);
				});

		if (edition) {
			baseButtonBar.getButtons().remove(selectFoodButton);
			baseButtonBar.getButtons().remove(selectFoodButton1);
			baseButtonBar.getButtons().remove(selectFoodButton2);
		}else {
			genTabPane.getTabs().remove(editGeneralTab);
			genTabPane.getTabs().remove(editCompositionTab);
			baseButtonBar.getButtons().remove(addFoodButton);
			baseButtonBar.getButtons().remove(saveFoodButton);
			baseButtonBar.getButtons().remove(duplicateButton);
			if (recipeCont!=null) {
				baseButtonBar.getButtons().remove(selectFoodButton2);
			}
		}
	}

	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, Vet vet, MainWinController mwc) {
		this.mainApp=mainApp;
		this.mAlimList=mainApp.getmAlimList();
		this.stage=stage;
		this.edition=edition;
		this.vet=vet;
		this.mainCont=mwc;

	setter();

	}
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, Vet vet, RecipeEditorControler REC) {
		this.mainApp=mainApp;
		this.mAlimList=mainApp.getmAlimList();
		this.stage=stage;
		this.edition=edition;
		this.vet=vet;
		this.recipeCont=REC;

	setter();

	}



	public AlimentEv getData() {
		return alimU;
	}
	public void creatChart(AlimentEv alim) {

		RawCompo.getData().clear();
		PieChart.Data slice;
		List<String> colorList = new ArrayList<>();
		for(int i=0; i<6;i++) {
			NutrientBase enu=NutrientBase.getByCoef(i);
			slice= new PieChart.Data(bundle.getString(enu.getLabel()), alim.getNutrient(enu));
			RawCompo.getData().add(slice);
			slice.getNode().setStyle("-fx-pie-color: #" + 
					enu.getColr());
		}
		RawCompo.setLegendVisible(false);
		RawCompo.getData().forEach(data -> {
			String percentage = String.format("%.2f%%", (data.getPieValue()));
			Tooltip toolTip = new Tooltip(percentage);
			Tooltip.install(data.getNode(), toolTip);
		});
	}
	private ObservableList<NutrientP> getNutrientList(MainNutrientEnum en, AlimentEv al)  {
		ObservableList<NutrientP>	list= FXCollections.observableArrayList();
		if (al!=null) {
			switch(en) {
			case BASE:
				for (NutrientBase esp : NutrientBase.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(), al.isNutrient(esp)?""+ al.getNutrient(esp): "",al.isNutrient(esp), UnitEnum.BUg ));
				}
				break;
			case MACRO:
				for (NutrientMacro esp : NutrientMacro.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef() , al.isNutrient(esp)?""+ al.getNutrient(esp):"", al.isNutrient(esp) , new UnitP(esp.getUe()) ));
				}
				break;
			case MIN:

				for (NutrientMin esp : NutrientMin.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(),al.isNutrient(esp)? ""+ al.getNutrient(esp):"" ,al.isNutrient(esp),new UnitP(esp.getUe()) ));
				}
				break;
			case VITAM:
				for (NutrientVitam esp : NutrientVitam.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(),al.isNutrient(esp)?""+ al.getNutrient(esp):"", al.isNutrient(esp), new UnitP(esp.getUe()) ));
				}
				break;
			case LIPID:
				for (NutrientLipid esp : NutrientLipid.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(), al.isNutrient(esp)?""+ al.getNutrient(esp):"",al.isNutrient(esp) , new UnitP(esp.getUe()) ));
				}
				break;
			case OTHER:
				for (NutrientOther esp : NutrientOther.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(), al.isNutrient(esp)?""+ al.getNutrient(esp):"",al.isNutrient(esp) , new UnitP(esp.getUe()) ));
				}
				break;
				
			case AMA:
				for (AAEnum esp : AAEnum.values()){
					list.add(new NutrientP( en, bundle.getString(esp.getLabel()), esp.getCoef(), al.isNutrient(esp)?""+ al.getNutrient(esp):"",al.isNutrient(esp),new UnitP(esp.getUe()) ));
				}
				break;
			}}
		return list;

	}

	public void creatChartEner(AlimentEv alim) {

		PieChart.Data slice;
		EnerCompo.getData().clear();
		slice= new PieChart.Data(bundle.getString(NutrientBase.PROTEINE.getLabel()), alim.getProtEner());
		EnerCompo.getData().add(slice);
		slice.getNode().setStyle("-fx-pie-color: #" + 
				NutrientBase.PROTEINE.getColr());
		slice= new PieChart.Data(bundle.getString(NutrientBase.ENA.getLabel()), alim.getENAEner());
		EnerCompo.getData().add(slice);
		slice.getNode().setStyle("-fx-pie-color: #" + 
				NutrientBase.ENA.getColr());
		slice= new PieChart.Data(bundle.getString(NutrientBase.LIPIDE.getLabel()), alim.getLipEner());
		EnerCompo.getData().add(slice);
		slice.getNode().setStyle("-fx-pie-color: #" + 
				NutrientBase.LIPIDE.getColr());

		EnerCompo.setLegendVisible(false);


		EnerCompo.getData().forEach(data -> {
			String percentage = String.format("%.2f%%", (100*data.getPieValue()));
			Tooltip toolTip = new Tooltip(percentage);
			Tooltip.install(data.getNode(), toolTip);
		});


	}
	private void ActualAlim(AlimP al, AlimP old) {

		if (al!=null & !abort) {
			setVisible(true);
			if (old!=null) {
				saveAlim();}
			transit=al;

			alAc=al;
			alimU=al.getAlim();
			creatChart(alimU);

			creatChartEner(alimU);

			BaseNutrientTable.selectionModelProperty().get().clearSelection();;
			BaseNutrientTable.setItems(getNutrientList(MainNutrientEnum.BASE,alimU));
			BaseNutrientTable.setItems(getNutrientList(MainNutrientEnum.BASE,alimU));

			MacroNutrientTable.setItems(getNutrientList(MainNutrientEnum.MACRO,alimU));

			LipNutrientTable.setItems(getNutrientList(MainNutrientEnum.LIPID,alimU));
			OtherNutrientTable.setItems(getNutrientList(MainNutrientEnum.OTHER,alimU));
			MinNutrientTable.setItems(getNutrientList(MainNutrientEnum.MIN,alimU));

			VitamNutrientTable.setItems(getNutrientList(MainNutrientEnum.VITAM,alimU));

			AmaNutrientTable.setItems(getNutrientList(MainNutrientEnum.AMA,alimU));
DataLab.setText(vet.getAlDBL().get(alimU.getDataB()).getsNom());
AlimLab.setText((alimU.getFamillyBrand().isBlank()  ?"":(alimU.getFamillyBrand()+", "))+ (  alimU.getGamme().isBlank()? "":(alimU.getGamme()+", "))+alimU.getNom() + ((alimU.getTypeAliment()==FoodKind.COMPLET)? ((alimU.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +bundle.getString("wet")):(" " +bundle.getString("dry"))) :"")+ ((alimU.getTypeAliment()==FoodKind.COMPLET|alimU.getTypeAliment()==FoodKind.COMPLEMENTAIRE)&alimU.getEspeces().size()==1? " "+bundle.getString(Espece.getEnumFromStringId(alimU.getEspeces().getFirst()).nameToString()):"" )
	     );
			ObservableList<AlimIndic>Source=FXCollections.observableArrayList();
			ObservableList<AlimIndic>Target=FXCollections.observableArrayList();

			for(AlimIndic ind:AlimIndic.valuesExcept()) {
				boolean touch=false;
				for (String s:alimU.getIndicat()) {
					if (ind.nameToString().equals(s)) {
						touch=true;
						Target.add(ind);
					}
				}
				if(!touch) {
					Source.add(ind);
				}

			}
comboDB.getSelectionModel().select(vet.getAlDBL().get(alimU.getDataB()));
			ListIndic.getTargetItems().setAll(Target);
			ListIndic.getSourceItems().setAll(Source);
			ListIndic.getSourceItems().sort(new Comparator<AlimIndic>()
			{
			    public int compare(AlimIndic f1, AlimIndic f2)
			    {
			    	if (f1.equals(AlimIndic.ALL)) {
			    		return -1;
			    	}else if(f2.equals(AlimIndic.ALL)){
			    		return 1;
			    	}else if(f1.equals(AlimIndic.PHYS)){
			    		return -1;
			    	}
			    	else if(f2.equals(AlimIndic.PHYS)){
			    		return 1;
			    	}
			    	else if(f1.equals(AlimIndic.PED)){
			    		return -1;
			    	}
			    	else if(f2.equals(AlimIndic.PED)){
			    		return 1;
			    	}
			    
			        return bundle.getString(f1.toString()).compareTo( bundle.getString(f2.toString()));
			    }        
			});
			
			foodKindCombo.getSelectionModel().select(alimU.getTypeAliment());
			FoodNameText.setText(alimU.getNom());
			IngredientsTextArea.setText(alimU.getIngredients());
			contenantCombo.getSelectionModel().select(alimU.getCont());
			quantContText.setText(""+alimU.getQuantInt());
			checkDeprecated.setSelected(alimU.isDeprecated());
			
			if (alimU.getTypeAliment().equals(FoodKind.COMPLET)|alimU.getTypeAliment().equals(FoodKind.COMPLEMENTAIRE)) {
				FoodBrandName.setText(alimU.getMarque());
				rangeText.setText(alimU.getGamme());
				priceCombo.getSelectionModel().select(PriceCateg.getById(alimU.getCategoriePrix()));
				rangeText.setVisible(true);
				FoodBrandName.setVisible(true);
				foodFamilyCombo.setVisible(false);
				priceCombo.setVisible(true);

			}else {
				FoodBrandName.setText("");

				rangeText.setText("");
				priceCombo.getSelectionModel().select(PriceCateg.NO);
				foodFamilyCombo.getSelectionModel().select(alimU.getGroup());
				rangeText.setVisible(false);
				FoodBrandName.setVisible(false);
				foodFamilyCombo.setVisible(true);
				priceCombo.setVisible(false);
			}
			for (Espece es:SpeciesCheck.getItems()) {
				if (alimU.isEspece(es.getUUID())) {
					SpeciesCheck.getCheckModel().check(es);

				}else {
					SpeciesCheck.getCheckModel().clearCheck(es);
				}
			}

			BaseCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			MinCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			MacroCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			VitamCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			LipCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			OtherCombo.setValue(bundle.getString(UnitConcEnum.BU100g.getName()));
			AMACombo.setValue(bundle.getString(UnitConcEnum.Prot.getName()));
			/*	}else {

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle(bundle.getString("NotSaved"));
				alert.setHeaderText(bundle.getString("NotSaved"));
				alert.setContentText(bundle.getString("WantSave"));
				ButtonType yesButton = new ButtonType("Yes");
				ButtonType noButton = new ButtonType("No");
				ButtonType cancelButton = new ButtonType("Cancel");

				alert.getButtonTypes().setAll(yesButton, noButton, cancelButton);

				// option != null.
				Optional<ButtonType> option = alert.showAndWait();

				if (option.get() == null) {

				} else if (option.get() == yesButton) {
					boolean	sav=saveAlim();
					if (sav) {
						ActualAlim(al,old);
					}else {
						abort=true;
						TFC.getAlimTable().getSelectionModel().select(old);
						System.out.println("cancel");
					}

				} else if (option.get() == cancelButton) {

					abort=true;

TFC.getAlimTable().getSelectionModel().clearSelection();
					TFC.getAlimTable().getSelectionModel().select(old);
					System.out.println("cancel");
					saved=false;
				} else {
					saved=true;
					ActualAlim(al, old);
				}
			}*/

		}else {
			setVisible(false);
		}
		abort=false;
	}




	private FoodSearchController setSearchBand (BorderPane p, boolean anal) {
		FoodSearchController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/searchBar.fxml"));
		try {
			Accordion rt=(Accordion)loader.load();
			//rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.setTop(rt);

			coefwin=loader.getController();
			coefwin.setMainApp(mainApp, stage, anal,TFC.getAlimTable(), vet);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}


	private TableFoodController setFoodTable (VBox p, boolean anal) {

		TableFoodController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/foodTableView.fxml"));


		try {
			VBox rt=(VBox)loader.load();
			//rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			p.getChildren().add(rt);

			coefwin=loader.getController();
			coefwin.setMainApp( stage, anal);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	private String  getValOfNutrient(ObservableList<NutrientP> ol, NutrientBase en) {
		for (int i=0; i< ol.size();i++) {
			if (ol.get(i).getKind().get()==en.getCoef()) {
				return ol.get(i).getQuantity()
						;    		}
		}
		return "";
	}
	private void  updateNutrient(ObservableList<NutrientP> ol, float unitF, float prot) {
		for (int i=0; i< ol.size();i++) {
			alimU.updateByNutrient(ol.get(i), unitF, prot);
		}

	}
public void scrollSelect() {
	 int selected = TFC.getAlimTable().getSelectionModel().getSelectedIndex();
	 System.out.println(selected);
     if (selected == -1) return ;
     TableViewSkin<?> skin = (TableViewSkin<?>) TFC.getAlimTable().getSkin();
     skin.getChildren().stream()
             .filter(VirtualFlow.class::isInstance)
             .map(VirtualFlow.class::cast)
             .findAny()
             .ifPresent(vf -> {
                 vf.scrollToTop(selected);
                 vf.layout();
                vf.scrollPixels(vf.getHeight());
             });
}
public void setVisible(boolean b) {
	BaseNutrientTable.setVisible(b);
	MacroNutrientTable.setVisible(b);
comboDB.setVisible(b);
SpeciesCheck.setVisible(b);
	LipNutrientTable.setVisible(b);
	OtherNutrientTable.setVisible(b);
	MinNutrientTable.setVisible(b);
	VitamNutrientTable.setVisible(b);
	AmaNutrientTable.setVisible(b);
	ListIndic.setVisible(b);
	foodKindCombo.setVisible(b);
	FoodNameText.setVisible(b);
	IngredientsTextArea.setVisible(b);
	contenantCombo.setVisible(b);
	quantContText.setVisible(b);
		FoodBrandName.setVisible(b);
		rangeText.setVisible(b);
		priceCombo.setVisible(b);
		rangeText.setVisible(b);
		FoodBrandName.setVisible(b);
		foodFamilyCombo.setVisible(b);
		priceCombo.setVisible(b);
}
public void updateDB(alimDB db) {
	System.out.println("je trigeur "+db.getsNom()+" "+vet.getAlDBL().values().size());
	
	this.vet.getAlDBL().add(db);
	mainApp.setVet(vet);
	comboDB.getItems().clear();
	comboDB.getItems().setAll(vet.getAlDBL().values());
	System.out.println("je trigeur "+db.getsNom()+" "+vet.getAlDBL().values().size());
	if(alimU!=null) {
		comboDB.getSelectionModel().select(vet.getAlDBL().get(alimU.getDataB()));
	}
	FSC.updateVet(vet);
}
}

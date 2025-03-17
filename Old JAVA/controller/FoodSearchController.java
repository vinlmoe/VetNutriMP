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
import org.jfree.chart.fx.ChartViewer;

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
import Enumerise.FoodKind;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientVitam;
import Enumerise.PriceCateg;
import Enumerise.UnitConcEnum;
import Enumerise.UnitEnum;
import application.DataConnector;
import application.VetNutri;
import graph.component.BooleanEditingCellRation;
import graph.component.Chart;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import model.AlimIndic;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentUnif;
import model.AnimalEv;
import model.ConditionSelection;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.Ration;
import model.Reference;
import model.ReferenceEv;
import model.TypeAlim;
import model.Vet;
import model.alimDB;

public class FoodSearchController  implements Initializable {
	private AlimP transit;
	private AlimentUnif alimU;
	private ObservableList<AlimP> mAlimList;
	private AlimP alAc;
	private Stage stage;
	private String[] _brandList={"Hey", "Hello", "Hello World", "Apple", "Cool", "Costa", "Cola", "Coca Cola"};
	private Set<String> brandList = new HashSet<>(Arrays.asList(_brandList));
	private AutoCompletionBinding<String> autoCompletionBinding;
	private boolean saved=true;
	private ObservableList<ConstrainP> constrains= FXCollections.observableArrayList();
	boolean abort=false;
	private ResourceBundle bundle;
	private boolean edition=false;
	
	
	@FXML
	private Accordion mainAccord;
	@FXML
	private TitledPane basicTitled; 
	
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TabPane genTabPane;
@FXML
private CheckBox checkIncDeprecated;
@FXML
private ComboBox<alimDB>comboDB;
	@FXML
	private  ComboBox<FoodKind> dataCombo;
	@FXML
	private  ComboBox<GroupAlim> typeCombo;
	@FXML
	private  ComboBox<PriceCateg> priceCombo;
	@FXML
	private  ComboBox<AlimIndic> indicCombo;

	@FXML
	private TextField searchText;



	@FXML
	private TableView<ConstrainP>constrainTable;
	@FXML
	private TableColumn<ConstrainP, String> kindColumn_constrainTable;
	@FXML
	private TableColumn<ConstrainP, ConditionEnum> conditionColumn_constrainTable;
	@FXML
	private TableColumn<ConstrainP, String> valueColumn_constrainTable;
	@FXML
	private TableColumn<ConstrainP, KindData> unitColumn_constrainTable;

	@FXML
	private void addConstrain() {
		constrains.add(new ConstrainP("PROT", "", ConditionEnum.LESS, KindData.FDESC));
	}
	@FXML
	private void removeConstrain() {
		if (constrainTable.getSelectionModel().getSelectedItem()!=null) {
	constrains.remove(constrainTable.getSelectionModel().getSelectedItem());
		}
	}


	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

		Callback<TableColumn<ConstrainP, String>, TableCell<ConstrainP, String>> comboCellFactory
		= (TableColumn<ConstrainP, String> param) -> new ComboEditingCellKindConstrain(bundle);
		kindColumn_constrainTable.setCellValueFactory(new PropertyValueFactory<>("Name")); 

		kindColumn_constrainTable.setCellFactory(comboCellFactory);
		kindColumn_constrainTable    .setOnEditCommit((CellEditEvent<ConstrainP, String> event) -> {
			TablePosition<ConstrainP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setName(value);
			constrainTable.refresh();

			searchAlimList();
		});
		Callback<TableColumn<ConstrainP, ConditionEnum>, TableCell<ConstrainP, ConditionEnum>> comboConstrainCellFactory
		= (TableColumn<ConstrainP, ConditionEnum> param) -> new ComboEditingCellConstrainConstrain(bundle);
		conditionColumn_constrainTable.setCellValueFactory(new PropertyValueFactory<>("CE")); 

		conditionColumn_constrainTable.setCellFactory(comboConstrainCellFactory);
		conditionColumn_constrainTable    .setOnEditCommit((CellEditEvent<ConstrainP, ConditionEnum> event) -> {
			TablePosition<ConstrainP, ConditionEnum> pos = event.getTablePosition();
			ConditionEnum value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setCE(value);
			alimTable.setItems(searchAlimList())
;
		});

		Callback<TableColumn<ConstrainP, String>, TableCell<ConstrainP, String>> stringCellFactory
		= (TableColumn<ConstrainP, String> param) -> new StringEditingCell<ConstrainP>();
		valueColumn_constrainTable.setCellValueFactory(new PropertyValueFactory<>("Quantity")); 

		valueColumn_constrainTable.setCellFactory(stringCellFactory);
		valueColumn_constrainTable    .setOnEditCommit((CellEditEvent<ConstrainP, String> event) -> {
			TablePosition<ConstrainP, String> pos = event.getTablePosition();
			String value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setQuantity(value);
			alimTable.setItems(searchAlimList());
			constrainTable.refresh();

		});

		Callback<TableColumn<ConstrainP, KindData>, TableCell<ConstrainP, KindData>> comboUnitCellFactory
		= (TableColumn<ConstrainP, KindData> param) -> new ComboEditingCellUnitConstrain(bundle);
		unitColumn_constrainTable.setCellValueFactory(new PropertyValueFactory<>("Unit")); 

		unitColumn_constrainTable.setCellFactory(comboUnitCellFactory);
		unitColumn_constrainTable    .setOnEditCommit((CellEditEvent<ConstrainP, KindData> event) -> {
			TablePosition<ConstrainP, KindData> pos = event.getTablePosition();
			KindData value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setUnit(value);

			alimTable.setItems(searchAlimList());
		});
		constrainTable.setItems(constrains);
		constrainTable.setEditable(true);

		speciesCombo.getItems().addAll(Espece.values());

priceCombo.getItems().addAll(PriceCateg.values());
		for(FoodKind ta:FoodKind.values()) {
			dataCombo.getItems().add(ta);
		}
		for(AlimIndic ta:AlimIndic.values()) {
			indicCombo.getItems().add(ta);
		}
	
		for(GroupAlim ta:GroupAlim.values()) {
			typeCombo.getItems().add(ta);
		}
priceCombo.getSelectionModel().select(PriceCateg.ALL);

		ObservableList<String> Refe    = FXCollections.observableArrayList();

		Refe.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refe.add( bundle.getString(UnitConcEnum.BUkg.getName()));
		ObservableList<String> Refeprot   = FXCollections.observableArrayList();
		Refeprot.add( bundle.getString(UnitConcEnum.Prot.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BUkg.getName()));

		
		dataCombo.setConverter(new StringConverter<FoodKind>()
		{
			
			    // Method to convert a Person-Object to a String
		    @Override
		    public String toString(FoodKind person)
		    {
		        return person == null? null : bundle.getString(person.toString());
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
    return person == null? null:(person.getUUID()=="NO"? bundle.getString( person.getsNom()):person.getsNom());
}

// Method to convert a String to a Person-Object
@Override
public alimDB fromString(String string)
{

    return new alimDB();
}
});

		dataCombo.getSelectionModel().select(0);
		indicCombo.getSelectionModel().select(0);
		typeCombo.getSelectionModel().select(0);
		speciesCombo.getSelectionModel().select(0);
		
		speciesCombo.setConverter(new SpeciesConverter(bundle));

		dataCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			alimTable.setItems( searchAlimList());
			dataCombo.getEditor().textProperty().set(newValue.nameToString());
		}); 
		comboDB.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			alimTable.setItems( searchAlimList());
			
		}); 
		indicCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			alimTable.setItems( searchAlimList());
		
		}); 
		priceCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			alimTable.setItems( searchAlimList());
		
		}); 
		typeCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
			alimTable.setItems( searchAlimList());
			
		}); 
		

		speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

			alimTable.setItems( searchAlimList());


		}); 
		checkIncDeprecated .selectedProperty().addListener((options, oldValue, newValue) -> {

			alimTable.setItems( searchAlimList());


		}); 

		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			alimTable.setItems(searchAlimList());
		});

		indicCombo.setConverter(new StringConverter<AlimIndic>()
{
	
	    // Method to convert a Person-Object to a String
    @Override
    public String toString(AlimIndic person)
    {
        return person == null? null : bundle.getString(person.toString());
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public AlimIndic fromString(String string)
    {
    
        return AlimIndic.ALL;
    }
});


	} 
	public void Update()  {
		alimTable.setItems( searchAlimList() );
	}

	VetNutri mainApp;
	TableView<AlimP> alimTable; 
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, TableView<AlimP> alimTable, Vet vet) {
		this.mainApp=mainApp;
		this.mAlimList=mainApp.getmAlimList();
		this.stage=stage;
		this.edition=edition;
	this.alimTable=alimTable;

		ObservableList<AlimP> listA=null;
		listA =  mainApp.getmAlimList();
		String[] str=new String[listA.size()];
		for(int i=0; i<listA.size(); i++) {
			str[i]=listA.get(i).getBrand();
		}
		brandList = new HashSet<>(Arrays.asList(str));
	comboDB.getItems().add(new alimDB("NO","All", "All"));
		comboDB.getItems().addAll(vet.getAlDBL().values());

		comboDB.getSelectionModel().select(0);
		mainAccord.setExpandedPane(basicTitled);
		
		indicCombo.getItems().sort(new Comparator<AlimIndic>()
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
	
	}


	public ObservableList<AlimP> searchAlimList()  {
		ObservableList<AlimP> sAlimList=FXCollections.observableArrayList();
		String textSearch= searchText.getText();
		this.mAlimList=mainApp.getmAlimList();
		if (comboDB.getSelectionModel().getSelectedItem()!=null) {
		if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
			for (AlimP al:mAlimList) {
				if (		(al.getAlim().getTypeAliment().getCoef()==dataCombo.getSelectionModel().getSelectedItem().getCoef()|dataCombo.getSelectionModel().getSelectedItem().equals(FoodKind.ALL))
						&    			(	al.getAlim().getDataB().equals(comboDB.getSelectionModel().getSelectedItem().getUUID())|comboDB.getSelectionModel().getSelectedItem().getUUID()=="NO")
						&    			(	al.getAlim().isEspece(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH))
						&    			(	al.getAlim().getGroup().equals(typeCombo.getSelectionModel().getSelectedItem())|typeCombo.getSelectionModel().getSelectedItem().equals(GroupAlim.ALL))
						&    			(	al.getAlim().getCategoriePrix().equals(priceCombo.getSelectionModel().getSelectedItem().getId())|priceCombo.getSelectionModel().getSelectedItem().equals(PriceCateg.ALL))
						& (al.getAlim().getIndicat().contains(indicCombo.getSelectionModel().getSelectedItem().nameToString())|indicCombo.getSelectionModel().getSelectedItem().equals(AlimIndic.ALL))
						&(!al.getAlim().isDeprecated()|checkIncDeprecated.isSelected())
					& isConstrained(al.getAlim())) {

					boolean prs=true;
					for (String word: words){
						if (((al.getAlim().getNom().toLowerCase().indexOf(word.toLowerCase())!=-1))|(al.getAlim().getGamme().toLowerCase().indexOf(word.toLowerCase())!=-1)|(al.getAlim().getMarque().toLowerCase().indexOf(word.toLowerCase())!=-1)){
							prs=prs && true;}
						else {prs=false;}}
					if (prs){
						sAlimList.add(al);
					}					

				}
			}    		

		}else {
			for (AlimP al:mAlimList) {
				if (
						(al.getAlim().getTypeAliment().getCoef()==dataCombo.getSelectionModel().getSelectedItem().getCoef()|dataCombo.getSelectionModel().getSelectedItem().equals(FoodKind.ALL))
						&    			(	al.getAlim().getDataB().equals(comboDB.getSelectionModel().getSelectedItem().getUUID())|comboDB.getSelectionModel().getSelectedItem().getUUID()=="NO")
						&    			(	al.getAlim().isEspece(speciesCombo.getSelectionModel().getSelectedItem().getUUID())|speciesCombo.getSelectionModel().getSelectedItem().equals(Espece.CH))
						& (al.getAlim().getIndicat().contains(indicCombo.getSelectionModel().getSelectedItem().nameToString())|indicCombo.getSelectionModel().getSelectedItem().equals(AlimIndic.ALL))
						&    			(	al.getAlim().getGroup().equals(typeCombo.getSelectionModel().getSelectedItem())|typeCombo.getSelectionModel().getSelectedItem().equals(GroupAlim.ALL))
						&    			(	al.getAlim().getCategoriePrix().equals(priceCombo.getSelectionModel().getSelectedItem().getId())|priceCombo.getSelectionModel().getSelectedItem().equals(PriceCateg.ALL))
						&(!al.getAlim().isDeprecated()|checkIncDeprecated.isSelected())
					& isConstrained(al.getAlim())) {
					sAlimList.add(al);
				}

			}
		}
		}

		return sAlimList;
	}

	public AlimentUnif getData() {
		return alimU;
	}
	public void creatChart(AlimentUnif alim) {

	


	}


public void updateVet(Vet vet) {
	alimDB old=comboDB.getSelectionModel().getSelectedItem();
	comboDB.getItems().clear();
	comboDB.getItems().add(new alimDB("NO","All", "All"));
	comboDB.getItems().addAll(vet.getAlDBL().values());
if (old.getUUID().equals("NO")) {
	comboDB.getSelectionModel().select(0);
}else {
	comboDB.getSelectionModel().select(vet.getAlDBL().get(old.getUUID()));
}

}

	private boolean isConstrained(AlimentEv alimentEv) {
		boolean respose = true;
		ConditionSelection cs;
		for (ConstrainP cp:constrainTable.getItems()) {
			switch(cp.getMNE()) {
			case INGREDIENT:

				cs=new ConditionSelection(cp.getName(),cp.getCE(), cp.getQuantity());
				respose=respose& cs.isCondition(alimentEv);
				break;
			default:
				cs=new ConditionSelection(cp.getName(), cp.getUnit(),cp.getCE(), Float.parseFloat(cp.getQuantity()));
				respose=respose& cs.isCondition(alimentEv);
				break;
			}


		}

		return respose;
	}

	
	
}

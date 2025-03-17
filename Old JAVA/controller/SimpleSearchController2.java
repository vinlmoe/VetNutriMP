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
import model.AlimentRation;
import model.AlimentUnif;
import model.AnimalEv;
import model.ConditionSelection;
import model.ConsultationEv;
import model.Espece;
import model.GroupAlim;
import model.Ration;
import model.Reference;
import model.TypeAlim;

public class SimpleSearchController2  implements Initializable {
	
	boolean abort=false;
	private ResourceBundle bundle;
	
	
	
	@FXML
	private Accordion mainAccord;
	@FXML
	private TitledPane basicTitled; 
	
	@FXML
	private ComboBox<Espece> speciesCombo;

	@FXML
	private TabPane genTabPane;
	

	

	@FXML
	private TextField searchText;






	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl

	
		
		speciesCombo.getItems().addAll(Espece.values());



		speciesCombo.getSelectionModel().select(0);
		
		speciesCombo.setConverter(new SpeciesConverter(bundle));

		

		speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

		Update();


		}); 

		searchText.textProperty().addListener((observable, oldValue, newValue) -> {
			Update();
		});

	

	} 
	public void Update()  {
		if (searchText.getText()!=null &speciesCombo.getSelectionModel().getSelectedItem()!=null)
		alimTable.search(searchText.getText(), speciesCombo.getSelectionModel().getSelectedItem());
	}

	VetNutri mainApp;
	TableInterface alimTable; 
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, TableInterface alimTable) {
		this.mainApp=mainApp;
	
	this.alimTable=alimTable;

	
		

		
		mainAccord.setExpandedPane(basicTitled);
	
	}



	public void creatChart(AlimentUnif alim) {

	


	}




	
	
}

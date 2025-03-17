package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.ResourceBundle;
import org.jfree.chart.fx.ChartViewer;

import DataStruct.AlimP;
import DataStruct.ConstrainP;
import DataStruct.EquationConsP;
import Enumerise.ConditionEnum;
import Enumerise.KindData;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.UnitConcEnum;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import graph.component.Chart;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.AlimentUnif;
import model.Vet;

public class FoodAnalyzerController  implements Initializable {
	  private AlimentUnif alimU;
	private ObservableList<AlimP> mAlimList;
	private Stage stage;
	private ObservableList<ConstrainP> constrains= FXCollections.observableArrayList();
	boolean abort=false;
	private ResourceBundle bundle;
	private Vet vet;
	private TableFoodController TFC;
	private TableFoodController TFCa;
	private FoodSearchController FSC;
	@FXML
	private ComboBox<Equation> equationCombo;
	@FXML
	private GridPane gridoComp;
	@FXML
	private GridPane grido;
	@FXML
	private GridPane gridoDE;
	@FXML
	private GridPane gridoProtPhos;
	@FXML
	private GridPane gridoCalPhos;
	@FXML
	private GridPane gridoKNa;
	@FXML
	private GridPane gridoPhosOs;
	@FXML
	private GridPane gridoO6O3;
	@FXML
	private GridPane gridoEPADHA;
	@FXML
	private BorderPane mainPane;

	@FXML
	private VBox VBmod;
	@FXML 
	private VBox VBmodif;
	
	@FXML
	private AnchorPane AnchorGraph1;

	@FXML
	private TabPane genTabPane;
	@FXML 
	private ComboBox dbCombo; 



@FXML
private Accordion foodAccord;
@FXML
private Accordion graphAccord; 
@FXML 
private TitledPane MainGraphTitled;
@FXML
private TitledPane mainFoodTitled;






	@FXML
	private AnchorPane AnchorGraph;
	@FXML 
	private  void addFood() {
		if (TFC.getSelected()!=null) {
			TFCa.addAlim(TFC.getSelected());
		}
		createChart() ;
	}
	
	@FXML 
	private  void addAllFood() {
	if(TFC.getData()!=null) {
		if (TFC.getData().size()>1000) {
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Oups");
			alert.setHeaderText(bundle.getString("TooManyFood"));
			alert.setContentText("");

			alert.show();
	}else {
			TFCa.addAlim(TFC.getData());
			createChart() ;}
	}
	}
	
	@FXML
	private void addConstrain() {
		constrains.add(new ConstrainP("PROT", "", ConditionEnum.MORE, KindData.FDESC));
	}
	

@FXML
private void removeFood() {
	TFCa.deleteAlim();
	createChart() ;
}

@FXML
public void addFoodInOld() {
	if (TFCa.getSelected()!=null) {
	mainApp.addAlimCons(TFCa.getSelected().getAlim(), false);}
}
@FXML
public void addFoodInNew() {
	if (TFCa.getSelected()!=null) {
	mainApp.addAlimCons(TFCa.getSelected().getAlim(), true);}
}
@FXML
private void removeAllFood() {
	TFCa.deleteAllAlim();
	createChart() ;
}

	@FXML
	private void close() {
		System.out.println("prep sort");
		stage.close();
	}
	private ChartViewer EnerView=new ChartViewer();
	private ChartViewer DEView=new ChartViewer();
	private ChartViewer CalPhosView=new ChartViewer();
	private ChartViewer KNaView=new ChartViewer();
	private ChartViewer O6O3View=new ChartViewer();
	private ChartViewer PhosOsView=new ChartViewer();
	private ChartViewer EPADHAView=new ChartViewer();
	private ChartViewer PPView=new ChartViewer();
	private ChartViewer ProtCompView=new ChartViewer();
	private ChartViewer LipCompView=new ChartViewer();
	private ChartViewer NFECompView=new ChartViewer();
	private ChartViewer FiberCompView=new ChartViewer();
	private ChartViewer CACompView=new ChartViewer();
	private ChartViewer PHOSCompView=new ChartViewer();
private Chart cc;

	@Override 
	public void initialize(URL location, ResourceBundle resources) {

		bundle=resources;
		//AlimTabl
grido.add(EnerView,0,0);
gridoDE.add(DEView,0,0);
gridoProtPhos.add(PPView,0,0);
gridoCalPhos.add(CalPhosView,0,0);
gridoKNa.add(KNaView,0,0);
gridoO6O3.add(O6O3View,0,0);
gridoEPADHA.add(EPADHAView,0,0);
gridoPhosOs.add(PhosOsView,0,0);;
gridoComp.add(ProtCompView, 0, 0);
gridoComp.add(LipCompView, 1, 0);
gridoComp.add(NFECompView, 0,1);
gridoComp.add(FiberCompView, 1, 1);
gridoComp.add(CACompView, 0,2);
gridoComp.add(PHOSCompView,1, 2);

		ObservableList<String> Refe    = FXCollections.observableArrayList();

		Refe.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refe.add( bundle.getString(UnitConcEnum.BUkg.getName()));
		ObservableList<String> Refeprot   = FXCollections.observableArrayList();
		Refeprot.add( bundle.getString(UnitConcEnum.Prot.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BU100g.getName()));
		Refeprot.add( bundle.getString(UnitConcEnum.BUkg.getName()));

		equationCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
if (newValue!=null){
	Task<Void> task = new Task<Void>() {
		@Override
		public Void call() throws Exception {
			System.out.println("1B");

			mAlimList=newValue.runAlim(mAlimList);
			
			TFCa.setItems(newValue.runAlim(TFCa.getData()));
		
			return null ;
		}
	};


	new Thread(task).run();
	
		FSC.searchAlimList();
		createChart();
		}

		}); 


		graphAccord.setExpandedPane(MainGraphTitled);	 

	} 


	VetNutri mainApp;
	public void setMainApp(VetNutri mainApp, Stage stage, boolean edition, Vet vet) {
		this.mainApp=mainApp;
		this.mAlimList=mainApp.getmAlimList();
		this.stage=stage;
		
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
			
				stage.setFullScreen(false);
				stage.close();
			}
		});        
		
		this.vet=vet;
		cc=new Chart(mainApp.getBun());
		ObservableList<AlimP> listA=null;
		listA =  mainApp.getmAlimList();
		String[] str=new String[listA.size()];
		for(int i=0; i<listA.size(); i++) {
			str[i]=listA.get(i).getBrand();
		}
		new HashSet<>(Arrays.asList(str));
TFC= setFoodTable(VBmod, false);
TFC.getAlimTable().getItems().addListener(new ListChangeListener() {
		@Override
		public void onChanged(Change c) {
			// TODO Auto-generated method stub
		
			TFCa.setID();
			createChart();
		}
		
		}); 
TFCa=setFoodTable(VBmodif,true);
TFC.getAlimTable().itemsProperty().addListener(	(observable, oldValue, newValue) ->{
	
	createChart();
		});
TFCa.getAlimTable().getItems().addListener(new ListChangeListener() {
	@Override
	public void onChanged(Change c) {
		// TODO Auto-generated method stub
		TFCa.setID();
		createChart();
	}
	
	}); 
TFCa.getAlimTable().getSelectionModel().selectedItemProperty().addListener(

		(observable, oldValue, newValue) ->{
		if (!TFCa.isIndeletion()) {
	createChart();}
		});
		FSC=setSearchBand(mainPane, false, TFC.getAlimTable());
		TFC.setItems(FSC.searchAlimList());
		graphAccord.setExpandedPane(MainGraphTitled);
		 for(EquationConsP eq:mainApp.getmEquationList()) {
			 switch (eq.getEquation().getKind()) {
		
			 case ENERGYDENSITY:
				 equationCombo.getItems().add(eq.getEquation());
			break;
			default:
				break;
				
			 }
			 equationCombo.getSelectionModel().selectFirst();
		 }


	}


	public AlimentUnif getData() {
		return alimU;
	}
	public void creatChart(AlimentUnif alim) {

	


	}








public void  createChart() {


	if (TFCa!=null) {
		if (TFCa.getData()!=null) {
			
	EnerView.setChart(cc.EnerOriChart(TFCa.getData(), TFCa.getSelected()));
	DEView.setChart(cc.DensEnerChart(TFCa.getData(), TFCa.getSelected()));
	PPView.setChart(cc.versusChart(TFCa.getData(), TFCa.getSelected(), NutrientMacro.PHOS, NutrientBase.PROTEINE,25, 35));
	CalPhosView.setChart(cc.versusChart(TFCa.getData(), TFCa.getSelected(), NutrientMacro.PHOS, NutrientMacro.CAL,1, 2));
	KNaView.setChart(cc.versusChart(TFCa.getData(), TFCa.getSelected(), NutrientMacro.NA, NutrientMacro.K,1, 10));
	O6O3View.setChart(cc.versusChart(TFCa.getData(), TFCa.getSelected(), NutrientLipid.O3, NutrientLipid.O6,2, 5));
EPADHAView.setChart(cc.EPADHAChart(TFCa.getData(), TFCa.getSelected()));
//ProtCompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientBase.PROTEINE));
PhosOsView.setChart(cc.versusChart(TFCa.getData(), TFCa.getSelected(), NutrientAnalysis.nonOsProt, NutrientAnalysis.nonOsPhos, 0, 0));
//NFECompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientBase.ENA));
//LipCompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientBase.LIPIDE));
//FiberCompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientBase.CELLULOSE));
//CACompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientMacro.CAL));
//PHOSCompView.setChart(cc.comparechart(TFC.getData(), TFCa.getData(), NutrientMacro.PHOS));

		}
	}
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
	            coefwin.setMainApp(stage, anal);
	    return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
   }
	
	private FoodSearchController setSearchBand (BorderPane p, boolean anal, TableView<AlimP> alimTable) {
	    
    	FoodSearchController coefwin;
   	 FXMLLoader loader = new FXMLLoader();
           loader.setResources(bundle);
           loader.setLocation(VetNutri.class.getResource("/view/searchBar.fxml"));
        
         
   	  try {
   		  Accordion rt=(Accordion)loader.load();
   		  //rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
   	
			p.setTop(rt);
			
			  coefwin=loader.getController();
	            coefwin.setMainApp(mainApp, stage, anal, alimTable, vet);
	    return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	return null;
   }
	
	

	
}

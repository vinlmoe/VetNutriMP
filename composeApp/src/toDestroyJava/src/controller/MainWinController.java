package controller;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

import org.controlsfx.control.CheckListView;
import org.jfree.chart.fx.ChartViewer;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import javafx.util.StringConverter;
import DataStruct.AlimP;
import DataStruct.AnimP;
import DataStruct.CoefP;
import DataStruct.ConsultP;
import DataStruct.RationP;
import DataStruct.SupplementalvariableP;
import DataStruct.TargetP;
import DataStruct.WeightDateP;
import Enumerise.AAEnum;
import Enumerise.KindData;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientEnergy;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.VariableKind;
import application.DataConnector;
import application.VetNutri;
import equation.RequirementAnalyzer;
import equation.listNutrientRef;
import eu.hansolo.medusa.Gauge;
import eu.hansolo.medusa.GaugeBuilder;
import eu.hansolo.medusa.skins.SimpleSectionSkin;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Callback;
import model.AdjustSaveEv;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentUnif;
import model.AnimalEv;
import model.ConditionScore;
import model.ConsultationEv;
import model.DataAccess;
import model.Espece;
import model.PdfExport;
import model.Ration;
import model.RationCalculator;
import model.Recette;
import model.RefValues;
import model.ReferenceEv;
import model.RemplirForm;
import model.Sex;
import model.TextAr;
import model.Vet;
import model.WeightDate;
import graph.component.*;

public class MainWinController implements Initializable { 
	private static volatile Instrumentation globalInstrumentation;
	Comparator<Ration> rationComparator = Comparator.comparing(Ration::isActual).thenComparing(Ration::getNom);
	Comparator<RationP> rationPComparator = Comparator.comparing(RationP::getProposed).thenComparing(RationP::getNom);

	
	BarChartIntake BCI=new BarChartIntake();		
	boolean listen=true;
	private boolean createNewB=false;
	private String placeExt="";
	private ResourceBundle bundle;
	private ObservableList<ReferenceEv>refList;
	private ObservableList<ReferenceEv>disList;
	private ObservableList<ReferenceEv>disListAcrive;
	private ReferenceEv ref;
	private RequirementAnalyzer  ra =new RequirementAnalyzer();
	private LabelData[] NeedEner;
	private LabelData[] NutrientEner;
	private GraphWeightController GWC;
@FXML
private Button addFormulaButton;

@FXML
private Button addFoodButton;
@FXML
private Button addRecipeButton;
	
	DecimalFormat twoDForm = new DecimalFormat("#.##");
	private RationCalculator calc=new RationCalculator();
	@FXML
	private FlowPane EnerBox;
	@FXML
	private Accordion rationAccord;
	private ChartViewer EnerView=new ChartViewer();
	private ChartViewer CompoView=new ChartViewer();
	private ChartViewer MinView=new ChartViewer();
	private ChartViewer MainView=new ChartViewer();
	private ChartViewer MacroView=new ChartViewer();
	private ChartViewer VitaView=new ChartViewer();
	private ChartViewer PhoProtView=new ChartViewer();
	private ChartViewer O6O3View=new ChartViewer();
	private ChartViewer CalPhosView=new ChartViewer();

	private Chart chartCreator;
	IntakeTabPaneController tabIntake;
	@FXML
	private VBox vbModif;
	@FXML
	private AnchorPane textAreaAnchor;
	private MethodAdjustRationController MARC;

	private TextAr areaAnam=new TextAr();

	@FXML
	private GridPane descGridPane;

	@FXML
	private TitledPane rationTitled;
	@FXML
	private VBox NeefBox;
	@FXML
	private VBox VBintake;
	@FXML  
	private TextField ownerNameText;


	private AutocompletionlTextField breedText= new AutocompletionlTextField();

	@FXML 
	private TextField animalNameText;

	@FXML
	private PieChart RawCompo;

	@FXML
	private PieChart EnerCompo;

	@FXML 
	private Label especeLabel;

	@FXML 
	private TextField identText;

	@FXML 
	private SplitPane mainPane;
	@FXML
	private AnchorPane AnchWeight;
	@FXML
	private GridPane MGridPane;
	@FXML 
	private TextField bodyWeightText;
	@FXML 
	private TextField leanBWText;


	@FXML 
	private DatePicker birthDatePicker;

	@FXML
	private TextField k1Coef;
	@FXML
	private TextField k2Coef;
	@FXML
	private TextField k3Coef;
	@FXML
	private TextField k4Coef;
	@FXML
	private TextField k5Coef;
	@FXML
	private ComboBox <CoefP> k1Combo;
	@FXML
	private ComboBox <CoefP>k2Combo;
	@FXML
	private ComboBox<CoefP> k3Combo;
	@FXML
	private ToggleButton GlobAnal;
	@FXML
	private ComboBox<CoefP> k4Combo;
	@FXML
	private ComboBox <CoefP>k5Combo;
	@FXML
	private ComboBox<Sex>sexCombo;
	@FXML
	private ComboBox<ReferenceEv> physioStatusCombo; 
	@FXML
	private ComboBox<ConditionScore> bcsCombo;
	@FXML
	private ComboBox<ConditionScore> mcsCombo;
	@FXML 
	private TableView <ConsultP> consTable;


	@FXML
	private void estIdeal() {
		if (!bodyWeightText.getText().isBlank() & bcsCombo.getSelectionModel().getSelectedItem()!=null) {
			leanBWText.setText(""+(Float.parseFloat( noPoint(bodyWeightText.getText()))*(100-bcsCombo.getSelectionModel().getSelectedItem().getValue())/80));
			SaveData();
		}
	}

	@FXML 
	private TableColumn <ConsultP, LocalDate> dateConsColumn;



	@FXML 
	private TableColumn <ConsultP, String> subjectConsColumn;

	@FXML 
	private TableView <WeightDateP> weightDateTable;
	@FXML 
	private TableColumn <WeightDateP, LocalDate> dateColumn_weightDateTable;

	@FXML 
	private TableColumn <WeightDateP, Float> weightColumn_weightDateTable;
	@FXML
	private CheckListView<ReferenceEv> listDisease;
	@FXML 
	private TableView <RationP> ratTable;
	@FXML 
	private TableColumn <RationP,  String> idRatColumn;
	@FXML 
	private TableColumn <RationP, Float> coefRatColumn;
	@FXML 
	private TableColumn <RationP, Boolean> proposedRatColumn;
	@FXML 
	private TableView <AlimP> alimTable;
	@FXML 
	private TableColumn <AlimP,  String> brandAlimColumn;
	@FXML 
	private TableColumn <AlimP, String> nameAlimColumn;
	@FXML 
	private TableColumn <AlimP, Float> quantityAlimColumn;
	@FXML 
	private TableColumn <AlimP, TargetP> adjustAlimColumn;



	@FXML 
	private TableView <SupplementalvariableP> variableTable;
	@FXML 
	private TableColumn <SupplementalvariableP, VariableKind> variableColumn;



	@FXML 
	private TableColumn <SupplementalvariableP, Float> valueVarColumn;


	/*@FXML
	private GridPane DescGrid;*/

	private VetNutri mainApp;
	private String ConsUUID=null;
	private String RatUUID=null;
	private AnimalEv anim=new AnimalEv();
	private IntakeTabPaneController ITPC;

	@FXML
	private void duplicateCons() {	
		if (consTable.getSelectionModel().getSelectedItem()!=null) {
			ConsultationEv consu=mainApp.getCurrentAnimal().getList().getConsult(ConsUUID).clone();
			mainApp.getCurrentAnimal().addConsult(consu);
			this.ConsUUID=consu.getUUID();
			if (consu.getRationList().get(0)!=null) {
				this.RatUUID=consu.getRationList().get(0).getUUID();   	 }else {
					this.RatUUID=null;
				}
			UpdateLists();
		}
	}
	@FXML
	private void duplicateRat() {
		if (RatUUID!=null & ConsUUID!=null) {
			Ration consu=mainApp.getCurrentAnimal().getList().getConsult(ConsUUID).getRationByUUID(RatUUID).clone();
			mainApp.getCurrentAnimal().getList().getConsult(ConsUUID).addNewRation(consu);
			this.RatUUID=consu.getUUID();
			UpdateLists();
		}
	}
	
	
	@FXML
	public void adjust() {

		this.SaveData();
		if(ref!=null) {
			MARC.other("Post Save");
			ra=getAnalyzer(calc,FXCollections.observableList( mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getSuppVarp()), calc.getRation());


			if (RatUUID!=null & ConsUUID!=null) {
				if(	mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID)!=null) {
					mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).setRationByUUID(RatUUID,
							calc.rationCalcEvolve(
									mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID),
									new AdjustSaveEv(MARC.getData()), 
									MARC.getPerEner(),
									MARC.getMeasure(),
									MARC.getLastTarget(),
									ra));
					alimTable.setItems(getAlimList());
				}
			}

			this.SaveData();}
	}

	@FXML
	private void handleTextField(ActionEvent event) {   
	}
	@FXML
	private void addRecette() {
		Recette recette=mainApp.selectRecetteWindow();
		System.out.println("1A");
		if (recette!=null& ConsUUID!=null &RatUUID!=null) {
			
				mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).addRecette(recette);
			
			alimTable.setItems(getAlimList());
			
			alimTable.refresh();
			
			this.SaveData();
		}
	}
	

	@FXML
	private void addFormula() {
		this.SaveData();
		if(calc!=null & ref!= null) {
		Recette recette=mainApp.selectFormulaWindow(calc, ref);
		if (recette!=null& ConsUUID!=null &RatUUID!=null) {
			for (AlimentRation alr:recette.getAlimentList()) {
				mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).addAliment(alr);
			}
			alimTable.setItems(getAlimList());
			alimTable.refresh();
			this.SaveData();
			}
		}
	}
	@FXML 
	private void newAlimAdd() { 
		mainApp.selectAlimWindow(this);

	} 

	public void newAlimInOldRat(AlimentEv alimentEv) { 
		if (alimentEv!=null & ConsUUID!=null &RatUUID!=null) {
			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).addAliment(alimentEv);}
		alimTable.setItems(getAlimList());
		alimTable.refresh();
	} 

	public void newAlimInNewRat(AlimentEv alimentEv) { 
		Ration consu=new Ration();
		
		if (alimentEv!=null ) {
		consu.setNom(alimentEv.getFamillyBrand());}
		if (ConsUUID!=null) {
			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).addNewRation(consu);
		}
		UpdateLists();
		for(RationP c: ratTable.getItems()) {
			if (c.getUUID().equals(consu.getUUID())) {
				ratTable.getSelectionModel().select(c);

			}


		}


		if (alimentEv!=null & ConsUUID!=null &RatUUID!=null) {
			
			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).setNom(alimentEv.getFamillyBrand());;
			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).addAliment(alimentEv);}
		alimTable.setItems(getAlimList());
		alimTable.refresh();
	} 


	@FXML 
	private void newWeightAdd() { 

		WeightDate wd=new WeightDate(LocalDate.now(), 0.0F);

		mainApp.getCurrentAnimal().addWeight(wd);
		UpdateLists();
		createGraphWeight();
	} 


	@FXML 
	private void handleNewAnimalItem() { 

	} 
	@FXML
	private void handleDeletePerson() {

	}
	public void  SaveData() {

		ConsultationEv trancon=new ConsultationEv(ConsUUID);
		mainApp.getCurrentAnimal().setResume(areaAnam.getText());
			// TODO Auto-generated method stub
		if(ConsUUID!=null) {
			if (!bodyWeightText.getText().isEmpty() 
					& !k1Coef.getText().isEmpty()
					& !k2Coef.getText().isEmpty()
					& !k3Coef.getText().isEmpty()
					& !k4Coef.getText().isEmpty()
					& !k5Coef.getText().isEmpty()) {

				trancon=	mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID);

				trancon.setPoids(Float.parseFloat(noPoint(  bodyWeightText.getText())));
				trancon.setK1value(Float.parseFloat(noPoint(  k1Coef.getText())));
				trancon.setK2value(Float.parseFloat(noPoint(  k2Coef.getText())));
				trancon.setK3value(Float.parseFloat(noPoint(  k3Coef.getText())));
				trancon.setK4value(Float.parseFloat(noPoint(  k4Coef.getText())));
				trancon.setK5value(Float.parseFloat(noPoint(  k5Coef.getText())));

				if (physioStatusCombo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setRefString(physioStatusCombo.getSelectionModel().getSelectedItem().getUUID());
					ref=physioStatusCombo.getSelectionModel().getSelectedItem();}else {
						for(LabelData l:NeedEner) {l.SetNoSys(bundle);
						};
						for(LabelData l:NutrientEner) {l.SetNoSys(bundle);
						};
						
					}
				if(k1Combo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setK1d(k1Combo.getSelectionModel().getSelectedItem().getUUID());
				}
				if(k2Combo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setK2d(k2Combo.getSelectionModel().getSelectedItem().getUUID());
				}
				if(k3Combo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setK3d(k3Combo.getSelectionModel().getSelectedItem().getUUID());
				}
				if(k4Combo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setK4d(k4Combo.getSelectionModel().getSelectedItem().getUUID());
				}
				if(k5Combo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setK5d(k5Combo.getSelectionModel().getSelectedItem().getUUID());}
				if(mcsCombo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setMCS(mcsCombo.getSelectionModel().getSelectedIndex());
				}
				if(bcsCombo.getSelectionModel().getSelectedItem()!=null) {
					trancon.setNewBCS(bcsCombo.getSelectionModel().getSelectedIndex());
				}
				trancon.setSuppVarp(variableTable.getItems());
				trancon.setDiseaseRef(listDisease.getCheckModel().getCheckedItems())	;

				if (!leanBWText.getText().isBlank()) {
					trancon.setPoidsIdeal(Float.parseFloat(noPoint(leanBWText.getText())));
					trancon.setPoidsIdeal(true);
				}else {
					trancon.setPoidsIdeal(0);
					trancon.setPoidsIdeal(false);
				}


				for(ReferenceEv s: listDisease.getCheckModel().getCheckedItems()){

				}
				mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).update(trancon);
				mainApp.getCurrentAnimal().setDateNaiss(birthDatePicker.getValue());

				mainApp.getCurrentAnimal().setSex(sexCombo.getSelectionModel().getSelectedItem().getID());
				mainApp.getCurrentAnimal().setNom(animalNameText.getText());
				mainApp.getCurrentAnimal().setNomProprio(ownerNameText.getText());
				mainApp.getCurrentAnimal().setId(identText.getText());
				calc.setConsult(mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID));
				//System.out.println("2BEE"+calc.getBEE());

				long a=System.currentTimeMillis();
				long b=System.currentTimeMillis();
				if (ref!=null & createNewB!=true) {
					if (tabIntake!=null & anim.getList().getRation(ConsUUID, RatUUID)!=null& ref.getConsistent()==1) {

						Ration rat=new Ration();
						rat=anim.getList().getRation(ConsUUID, RatUUID);
						calc.setRation(rat);
						calc.setRationList(anim.getList().getConsult(ConsUUID).getRationList());
						calc.setRef(ref);
						b=System.currentTimeMillis();
						System.out.println("Before calculate "+(b-a));
						a=b;
						calc.calculate(GlobAnal.isSelected());
						b=System.currentTimeMillis();
						System.out.println("after Calculate "+(b-a));
						a=b;
						ra=getAnalyzer(calc, FXCollections.observableList(mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getSuppVarp()), rat);

						for(int j=0; j<NutrientEner.length;j++) {
							NutrientEner[j].UpdateValue(calc, ra.getReferences(	NutrientEner[j].getMainEnum().getCoef(), NutrientEner[j].getCoef()), bundle, GlobAnal.isSelected());
						}
						for(int j=0; j<NeedEner.length;j++) {

							NeedEner[j].UpdateValue(calc, ra.getReferences(	NeedEner[j].getMainEnum().getCoef(), NeedEner[j].getCoef()), bundle, GlobAnal.isSelected());
							
						}


						tabIntake.updateValues(calc, getAnalyzer(calc, FXCollections.observableList(mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getSuppVarp()), rat), GlobAnal.isSelected());

						b=System.currentTimeMillis();
						System.out.println("after label éditing "+(b-a));
						a=b;
						creatChart(anim.getList().getRation(ConsUUID, RatUUID), ra, calc, ref);
						b=System.currentTimeMillis();
						System.out.println("after graph "+(b-a));
						a=b;
						//	creatChartEner(anim.getList().getRation(ConsUUID, RatUUID));
						//	creatHisto(anim.getList().getRation(ConsUUID, RatUUID),anim.getList().getConsultByUUID(ConsUUID),ref) ;
					}else {


					}

				}
				else {
					tabIntake.updateValues(null, null, GlobAnal.isSelected());

				}
				createGraphWeight();
			}else {
				tabIntake.updateValues(null, null, GlobAnal.isSelected());
			}
		}

		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() throws Exception {

				try {
					System.out.println("saver");
					DataAccess.writeAnimal(anim, (DataConnector.isWindows()?"":"../")+"tempAnimSaver.vbra");

				}
				catch(IOException e) {
					e.printStackTrace();
				}
				return null ;
			}
		};


		new Thread(task).run();


	}



	private ObservableList<ConsultP> getConsultList() {
		ObservableList<ConsultP>	list= FXCollections.observableArrayList();
		ArrayList<ConsultationEv>l=(ArrayList<ConsultationEv>) anim.getList().getListConsult();
		l.sort(null);
		for(ConsultationEv cons:l) {
			list.add(new ConsultP(cons));
		}

		return list;
	}

	private ObservableList<WeightDateP> getWeightList() {
		ObservableList<WeightDateP>	list= FXCollections.observableArrayList();
		Comparator<WeightDateP> wdComparator = Comparator.comparing(WeightDateP::getDate);
		for(WeightDate cons:anim.getListWeight()) {
			list.add(new WeightDateP(cons));
		}
		FXCollections.sort(list, wdComparator);
		return list;
	}

	private ObservableList<RationP> getRationList() {
		ObservableList<RationP>	list= FXCollections.observableArrayList();
		if (ConsUUID!=null) {
			for(Ration cons:anim.getList().getConsultByUUID(ConsUUID).getRationList()) {
				list.add(new RationP(cons));
			}}
		 FXCollections.sort(list, rationPComparator);
		return list;
	}
	private ObservableList<SupplementalvariableP> getVariableList() {
		ObservableList<SupplementalvariableP>	list= FXCollections.observableArrayList();
		if (ConsUUID!=null) {

			for(SupplementalvariableP cons:anim.getList().getConsultByUUID(ConsUUID).getSuppVarp()) {
				if(ref!=null) {
					if(ref.getBEEqu()!=null & ref.getBWEqu()!=null&ref.getDEcomEqu()!=null&ref.getDErawEqu()!=null) {

						if (ref.getBEEqu().getVar().contains(cons.getVariable())|
								ref.getBWEqu().getVar().contains(cons.getVariable())|
								ref.getDEcomEqu().getVar().contains(cons.getVariable())|
								ref.getDErawEqu().getVar().contains(cons.getVariable())) {

							list.add((cons));
						}
					}
				}


			}}

		return list;
	}

	private ObservableList<AlimP> getAlimList() {
		ObservableList<AlimP>	list= FXCollections.observableArrayList();
		if (ConsUUID!=null& RatUUID!=null) {

			for(AlimentRation cons:anim.getList().getRation(ConsUUID, RatUUID).getAlimentList()) {
				list.add(new AlimP(cons));
			}}

		return list;
	}

	private void ActualConsultation(ConsultP consup) {
		listen=false;

		ConsultationEv cons=null;

		if(consup!=null) {
			if (!consup.getUUID().equals(ConsUUID)) {

				ConsUUID=consup.getUUID();
				cons=mainApp.getConsult(ConsUUID);
				if(cons!=null) {
					ratTable.setItems(getRationList());
					ratTable.getSelectionModel().selectFirst();

					boolean touch=false;
					if(refList!=null& cons!=null) {
						if(refList.size()>0) {
							if(cons.getRefString()!=null) {

								for (ReferenceEv r:refList) {
									if (r.getUUID().equals(cons.getRefString())) {
										physioStatusCombo.setValue(r);
										ref=r;
										touch=true;
									}
								}
							}}
						if (!touch) {

							mainApp.getConsult(ConsUUID).setRefString("000");
							ref=physioStatusCombo.getSelectionModel().getSelectedItem();
						}


						if (ref!=null) {
							k1Combo.setItems(ref.getModk1());
							k1Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k2Combo.setItems(ref.getModk2());
							k3Combo.setItems(ref.getModk3());
							k4Combo.setItems(ref.getModk4());
							k5Combo.setItems(ref.getModk5());
							k2Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k3Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k4Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k5Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});


						}
					}


					k1Combo.getSelectionModel().select(isInListCoef(k1Combo.getItems(), cons.getK1d()));
					k2Combo.getSelectionModel().select(isInListCoef(k2Combo.getItems(), cons.getK3d()));
					k3Combo.getSelectionModel().select(isInListCoef(k3Combo.getItems(), cons.getK3d()));
					k4Combo.getSelectionModel().select(isInListCoef(k4Combo.getItems(), cons.getK4d()));
					k5Combo.getSelectionModel().select(isInListCoef(k5Combo.getItems(), cons.getK5d()));
					bcsCombo.getItems().clear();
					bcsCombo.getItems().add(new ConditionScore("1/9", 0, 0));
					bcsCombo.getItems().add(new ConditionScore("2/9", 5, 1));
					bcsCombo.getItems().add(new ConditionScore("3/9", 10, 2));
					bcsCombo.getItems().add(new ConditionScore("4/9", 15, 3));
					bcsCombo.getItems().add(new ConditionScore("5/9", 20, 4));
					bcsCombo.getItems().add(new ConditionScore("6/9", 25, 5));
					bcsCombo.getItems().add(new ConditionScore("7/9", 30, 6));
					bcsCombo.getItems().add(new ConditionScore("8/9", 35, 7));
					bcsCombo.getItems().add(new ConditionScore("9/9", 40, 8));
					mcsCombo.getItems().clear();
					mcsCombo.getItems().add(new ConditionScore("1/3", 0, 0));
					mcsCombo.getItems().add(new ConditionScore("2/3", 5, 1));
					mcsCombo.getItems().add(new ConditionScore("3/3", 10, 2));

					bcsCombo.getSelectionModel().select(cons.getNewBCS());
					mcsCombo.getSelectionModel().select(cons.getMCS());
					ObservableList<ReferenceEv>ol=isInListRef(listDisease.getItems(), cons.getDiseaseRef() );

					listDisease.getCheckModel().clearChecks();

					for (ReferenceEv r:ol) {
						listDisease.getCheckModel().check(r);
					}
					bodyWeightText.setText( String.valueOf(cons.getPoids()));

					k1Coef.setText(String.valueOf(cons.getK1value()));
					k2Coef.setText(String.valueOf(cons.getK2value()));
					k3Coef.setText(String.valueOf(cons.getK3value()));
					k4Coef.setText(String.valueOf(cons.getK4value()));
					k5Coef.setText(String.valueOf(cons.getK5value()));
					if(cons.getPoidsIdeal()!=0.0) {
						leanBWText.setText(String.valueOf(cons.getPoidsIdeal()));}else {
							leanBWText.clear();
						}
					variableTable.setItems(getVariableList());

					SaveData();
					fun();
				}
			}
		}else if (ConsUUID!=null) {
			for(ConsultP c: consTable.getItems()) {
				if (c.getUUID().equals(ConsUUID)) {

					consTable.getSelectionModel().select(c);

					SaveData();
					fun();
				}
			}
		}
		;


		listen=true;}

	private void ActualRat(RationP rat) {
		if(rat!=null) {

			if (!rat.getUUID().equals(RatUUID)) {
				RatUUID=rat.getUUID();

				alimTable.setItems(getAlimList());
				if (listen) {
					SaveData();}
			}
		}else if (RatUUID!=null) {
			for(RationP c: ratTable.getItems()) {
				if (c.getUUID().equals(RatUUID)) {

					ratTable.getSelectionModel().select(c);
					if (listen) {	
						SaveData();
					}
				}
			}
		}
	}

	private String noPoint(String s){
		String ReP="";
		String t=".";
		String[] st = s.split(",");
		if(st.length==1){
			ReP = st[0];
		}else{
			ReP = st[0]+t+st[1];}
		try {
			Float.parseFloat(ReP);
		}catch (NumberFormatException e) {
			if (ReP!="") {
				ReP="0.0";}
		}
		return ReP;
	}




	@Override 
	public void initialize(URL location, ResourceBundle resources) { 

GlobAnal.setTooltip(new Tooltip(resources.getString("toolGlobAnal")));

addFoodButton.setTooltip(new Tooltip(addFoodButton.getText()));

addRecipeButton.setTooltip(new Tooltip(addRecipeButton.getText()));
		textAreaAnchor.getChildren().add(areaAnam);
		AnchorPane.setTopAnchor(areaAnam, 0.0);
		AnchorPane.setBottomAnchor(areaAnam, 0.0);
		AnchorPane.setLeftAnchor(areaAnam, 0.0);
		AnchorPane.setRightAnchor(areaAnam, 0.0);

		bundle=resources;
		chartCreator=new Chart(bundle);

		NutrientEner=new LabelData[5];
		NutrientEner[0]=new LabelData(NutrientEnergy.TOT, KindData.NO);
		NutrientEner[1]=new LabelData(NutrientEnergy.PERC, KindData.NO);
		NutrientEner[2]=new LabelData(NutrientEnergy.K, KindData.NO);
		NutrientEner[3]=new LabelData(NutrientEnergy.DE, KindData.NO);
		NutrientEner[4]=new LabelData(NutrientEnergy.DEDM, KindData.NO);
		this.EnerBox.getChildren().add( NutrientEner[0]);
		this.EnerBox.getChildren().add( NutrientEner[1]);
		this.EnerBox.getChildren().add( NutrientEner[2]);
		this.EnerBox.getChildren().add( NutrientEner[3]);
		this.EnerBox.getChildren().add( NutrientEner[4]);
		NeedEner=new LabelData[4];
		NeedEner[0]=new LabelData(NutrientEnergy.MW, KindData.NO);
		NeedEner[1]=new LabelData(NutrientEnergy.BEE, KindData.NO);
		NeedEner[2]=new LabelData(NutrientEnergy.BE, KindData.NO);
		NeedEner[3]=new LabelData(NutrientEnergy.KPRED, KindData.NO);

		this.NeefBox.getChildren().add( NeedEner[0]);
		this.NeefBox.getChildren().add( NeedEner[1]);
		this.NeefBox.getChildren().add( NeedEner[2]);
		this.NeefBox.getChildren().add( NeedEner[3]);

		GWC=createGraphWeightController();

		descGridPane.add(breedText, 1,4);

		Gauge Mgauge = GaugeBuilder.create()                    .title("Title")                          .subTitle("SubTitle")                          .unit("Unit")                           .build();
		Mgauge.setSkin( new SimpleSectionSkin(Mgauge)   );
		Mgauge.setThreshold(35);
		Mgauge.setMaxValue(200);
		Mgauge.setMinValue(0);
		Mgauge.setThresholdColor(javafx.scene.paint.Color.BLUEVIOLET);
		Mgauge.setValue(60);


		//MGridPane.add(PhoProtView, 0, 2);
		MGridPane.add(EnerView,0, 0);
		MGridPane.add(CompoView,1, 0);
		MGridPane.add(MinView,1, 1);
		MGridPane.add(MainView,2,0);
		MGridPane.add(MacroView,0,1);
		MGridPane.add(VitaView,2, 1);
		//MGridPane.add(O6O3View,2, 2);
		//MGridPane.add(CalPhosView,1, 2);

		EnerView.setChart (new Chart(resources).EnerPie(30, 30, 40, false,1));

		//ConsultTable 
		Callback<TableColumn<ConsultP, String>, TableCell<ConsultP, String>> cellFactory
		= (TableColumn<ConsultP, String> param) -> new EditingCell();
		Callback<TableColumn<ConsultP, LocalDate>, TableCell<ConsultP, LocalDate>> dateCellFactory
		= (TableColumn<ConsultP, LocalDate> param) -> new DateEditingCell();
		consTable.setEditable(true);



		// ==== SUBJECTCOLUMN ===
		subjectConsColumn.setCellValueFactory(new PropertyValueFactory<>("subject"));

		subjectConsColumn.setCellFactory(cellFactory);

		subjectConsColumn.setOnEditCommit((CellEditEvent<ConsultP, String> event) -> {
			TablePosition<ConsultP, String> pos = event.getTablePosition();
			String newSubject = event.getNewValue();

			int row = pos.getRow();
			ConsultP consultp = event.getTableView().getItems().get(row);

			consultp.setSubject(newSubject);
			mainApp.getCurrentAnimal().getList() .getConsult( event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setObjet(newSubject);

			SaveData();

		});


		// date column 




		dateConsColumn.setCellValueFactory(cellData -> cellData.getValue().getDateConsProp());
		dateConsColumn.setCellFactory(dateCellFactory);
		dateConsColumn.setOnEditCommit(
				(TableColumn.CellEditEvent<ConsultP, LocalDate> t) -> {
					mainApp.getCurrentAnimal().getList() .getConsult( t.getTableView().getItems().get(t.getTablePosition().getRow()).getUUID()).setDate(t.getNewValue());
					((ConsultP) t.getTableView().getItems()
							.get(t.getTablePosition().getRow()))
					.setDate(t.getNewValue());
					SaveData();

				});


		ObservableList<ConsultP> list = getConsultList();
		consTable.setItems(list);
		dateConsColumn.setSortType(TableColumn.SortType.DESCENDING);
		consTable.getSortOrder().add(dateConsColumn);
		consTable.sort();
		consTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualConsultation(newValue));



		//Supp Variable Table 

		variableColumn.setCellValueFactory(new PropertyValueFactory<>("variable"));
		valueVarColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
		variableColumn.setCellFactory(listView -> new TableCell<SupplementalvariableP, VariableKind>()  {
			@Override
			public void updateItem(VariableKind employee, boolean empty) {
				super.updateItem(employee, empty);
				setText(employee == null ? "" : bundle.getString( employee.getName()));
			}
		});

		Callback<TableColumn<SupplementalvariableP, Float>, TableCell<SupplementalvariableP, Float>> floatVarCellFactory
		= (TableColumn<SupplementalvariableP, Float> param) -> new FloatEditingCell();
		valueVarColumn.setCellFactory(floatVarCellFactory);
		variableTable.setEditable(true);
		valueVarColumn.setEditable(true);

		valueVarColumn .setOnEditCommit((CellEditEvent<SupplementalvariableP, Float> event) -> {
			TablePosition<SupplementalvariableP, Float> pos = event.getTablePosition();
			float value = event.getNewValue();
			int row = pos.getRow();
			event.getTableView().getItems().get(row).setValue(value)	;});


		//RationTable
		Callback<TableColumn<RationP, String>, TableCell<RationP, String>> iDCellFactory
		= (TableColumn<RationP, String> param) -> new StringEditingCellRation();
		Callback<TableColumn<RationP, Float>, TableCell<RationP, Float>> floatCellFactoryRation
		= (TableColumn<RationP, Float> param) -> new FloatEditingCell();
		Callback<TableColumn<RationP, Boolean>, TableCell<RationP, Boolean>> booleanCellFactoryRation
		= (TableColumn<RationP, Boolean> param) -> new BooleanEditingCellRation();
		ratTable.setEditable(true);



		// ==== SUBJECTCOLUMN ===
		idRatColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));

		idRatColumn.setCellFactory(iDCellFactory);

		idRatColumn.setOnEditCommit((CellEditEvent<RationP, String> event) -> {
			TablePosition<RationP, String> pos = event.getTablePosition();
			String newId = event.getNewValue();
			int row = pos.getRow();
			RationP rationp = event.getTableView().getItems().get(row);
			rationp.setNom(newId);
			mainApp.getCurrentAnimal().getList().getRation(ConsUUID,event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setNom(newId);
UpdateLists();

		});
		coefRatColumn.setCellValueFactory(new PropertyValueFactory<>("coef"));
		coefRatColumn.setCellFactory(floatCellFactoryRation);
		coefRatColumn.setOnEditCommit((CellEditEvent<RationP, Float> event) -> {
			TablePosition<RationP, Float> pos = event.getTablePosition();
			Float newCoef = event.getNewValue();

			int row = pos.getRow();
			RationP rationp = event.getTableView().getItems().get(row);

			rationp.setCoef(newCoef);
			mainApp.getCurrentAnimal().getList() .getRation(ConsUUID,event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setCoef(newCoef);

			SaveData();
			fun();
		});
		proposedRatColumn.setCellValueFactory(new PropertyValueFactory<>("proposed"));

		proposedRatColumn.setCellFactory(booleanCellFactoryRation);

		proposedRatColumn.setOnEditCommit((CellEditEvent<RationP, Boolean> event) -> {
proposedRatColumn.setSortType(TableColumn.SortType.DESCENDING);
			//System.out.print("event");
			TablePosition<RationP, Boolean> pos = event.getTablePosition();
			Boolean newProp = event.getNewValue();

			int row = pos.getRow();
			RationP rationp = event.getTableView().getItems().get(row);

			rationp.setProposed(newProp);
			mainApp.getCurrentAnimal().getList() .getRation(ConsUUID,event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setActual(newProp);

			UpdateLists();
		});
		// date column 



		birthDatePicker.focusedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue){

					birthDatePicker.setValue(birthDatePicker.getConverter().fromString(birthDatePicker.getEditor().getText()));}


			}
		});


		ObservableList<RationP> listR = getRationList();

		ratTable.setItems(listR);
		ratTable.getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> ActualRat(newValue));


		//AlimTable
		Callback<TableColumn<AlimP, TargetP>, TableCell<AlimP, TargetP>> comboStringCellFactory
		= (TableColumn<AlimP, TargetP> param) -> new ComboEditingCellAlim();
		Callback<TableColumn<AlimP, Float>, TableCell<AlimP, Float>> quantityCellFactoryAlim
		= (TableColumn<AlimP, Float> param) -> new FloatEditingCellAlim();

		alimTable.setEditable(true);

		alimTable.setRowFactory(tv -> new TableRow<AlimP>() {
			private Tooltip tooltip = new Tooltip();
			@Override
			public void updateItem(AlimP person, boolean empty) {
				super.updateItem(person, empty);
				if (person == null) {
					setTooltip(null);
				} else {


					WebView  web = new WebView();
					WebEngine webEngine = web.getEngine();
					webEngine.loadContent
					(
							new AlimentRation(person.getAlim()).Resume(bundle, person.getQuantity())
							);
					web.setPrefHeight(400);
					web.setPrefWidth(300);
					web.setFontScale(0.8);
					Tooltip  tip = new Tooltip();
					tip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
					tip.setGraphic(web);
					tip.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
					setTooltip(tip);

				}
			}
		});

		brandAlimColumn.setCellValueFactory(new PropertyValueFactory<>("Brand"));
		nameAlimColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));


		// ==== SUBJECTCOLUMN ===


				quantityAlimColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));

				quantityAlimColumn.setCellFactory(quantityCellFactoryAlim);

				quantityAlimColumn.setOnEditCommit((CellEditEvent<AlimP, Float> event) -> {
					TablePosition<AlimP, Float> pos = event.getTablePosition();
					Float newCoef = event.getNewValue();

					int row = pos.getRow();

					try {
						AlimP alimp = event.getTableView().getItems().get(row);
						event.getTableView().getItems().get(row).setQuantity(newCoef);
						alimp.setQuantity(newCoef);
						event.getTableView().refresh();
						mainApp.getCurrentAnimal().getList() .getAliment(ConsUUID,RatUUID, event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setQuantite(newCoef);
						SaveData();}
					catch(Exception e) {
						SaveData();

					}
					event.consume();

				});
				adjustAlimColumn.setCellValueFactory(new PropertyValueFactory<>("adjustOn"));

				adjustAlimColumn.setCellFactory( comboStringCellFactory);

				adjustAlimColumn.setOnEditCommit((CellEditEvent<AlimP, TargetP> event) -> {
					TablePosition<AlimP, TargetP> pos = event.getTablePosition();
					TargetP newProp = event.getNewValue();

					int row = pos.getRow();
					AlimP rationp = event.getTableView().getItems().get(row);

					rationp.setAdjustOn(newProp);
					mainApp.getCurrentAnimal().getList() .getAliment(ConsUUID,RatUUID, event.getTableView().getItems().get(event.getTablePosition().getRow()).getUUID()).setTarget(newProp.getTarget());

					SaveData();
					fun();
				});


				//Weights column 
				Callback<TableColumn<WeightDateP, Float>, TableCell<WeightDateP, Float>> valueCellFactorywd
				= (TableColumn<WeightDateP, Float> param) -> new FloatEditingCellWeight();
				Callback<TableColumn<WeightDateP, LocalDate>, TableCell<WeightDateP, LocalDate>> dateCellFactorywd
				= (TableColumn<WeightDateP, LocalDate> param) -> new DateEditingCellWeight();
				weightDateTable.setEditable(true);



				// ==== SUBJECTCOLUMN ===
				weightColumn_weightDateTable.setCellValueFactory(new PropertyValueFactory<>("weight"));

				weightColumn_weightDateTable.setCellFactory(valueCellFactorywd);

				weightColumn_weightDateTable.setOnEditCommit((CellEditEvent<WeightDateP, Float> event) -> {
					TablePosition<WeightDateP, Float> pos = event.getTablePosition();
					float value = event.getNewValue();

					int row = pos.getRow();
					WeightDateP wd= event.getTableView().getItems().get(row);

					wd.setWeight(value);
					mainApp.getCurrentAnimal().updateWeight(wd.getUUID(), wd.getDate(), wd.getWeight());
					SaveData();
					fun();
				});


				// date column 




				dateColumn_weightDateTable.setCellValueFactory(cellData -> cellData.getValue().getDateProp());
				dateColumn_weightDateTable.setCellFactory(dateCellFactorywd);
				dateColumn_weightDateTable.setOnEditCommit(
						(TableColumn.CellEditEvent<WeightDateP, LocalDate> t) -> {
							mainApp.getCurrentAnimal()
							.updateWeight( t.getTableView().getItems().get(t.getTablePosition().getRow()).getUUID(),
									t.getNewValue(), 
									t.getTableView().getItems().get(t.getTablePosition().getRow()).getWeight());

							t.getTableView().getItems()
							.get(t.getTablePosition().getRow())
							.setDate(t.getNewValue());
							SaveData();
							fun();
						});


				ObservableList<WeightDateP> listwd = getWeightList();
				weightDateTable.setItems(listwd);
				dateColumn_weightDateTable.setSortType(TableColumn.SortType.ASCENDING);

				/*   weightDateTable.getSelectionModel().selectedItemProperty().addListener(
	          (observable, oldValue, newValue) -> ActualConsultation(newValue));*/


				valueVarColumn.setOnEditCommit((CellEditEvent<SupplementalvariableP, Float> event) -> {
					TablePosition<SupplementalvariableP, Float> pos = event.getTablePosition();
					float newProp = event.getNewValue();

					int row = pos.getRow();
					event.getTableView().getItems().get(row).setValue(newProp);


					mainApp.getConsult(ConsUUID).setSuppVarp(event.getTableView().getItems());
					SaveData();
					fun();
				});



				ObservableList<AlimP> listA = getAlimList();

				alimTable.setItems(listA);


				physioStatusCombo.valueProperty().addListener((observable, oldValue, newValue) -> {

					if (newValue!=null&listen) {
						if (!newValue.equals(oldValue)) {
							k1Combo.setItems(newValue.getModk1());
							k1Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k2Combo.setItems(newValue.getModk2());
							k3Combo.setItems(newValue.getModk3());
							k4Combo.setItems(newValue.getModk4());
							k5Combo.setItems(newValue.getModk5());
							k2Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k3Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k4Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});
							k5Combo.getItems().sort(new Comparator<CoefP>()
							{
								public int compare(CoefP f1, CoefP f2)
								{
									if (f2.getDescription().equals("Normal")) {
										return 1;
									}else 	if (f1.getDescription().equals("Normal")) {
										return -1;
									}
									return f1.getDescription().compareTo(f2.getDescription());
								}        
							});

							anim.getList().getConsultByUUID(ConsUUID).setSuppVarp(variableTable.getItems());
							ref=newValue;
							variableTable.setItems(getVariableList());
							SaveData();}
					}
				});
				listDisease.checkModelProperty().addListener((observable, oldValue, newValue) -> {
					if (listen) {

						SaveData();

					}
				});
				k1Combo.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {

						k1Coef.setText(""+newValue.getCoef());
						SaveData();

					}
				});
				k2Combo.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {

						k2Coef.setText(""+newValue.getCoef());
						SaveData();

					}
				});
				k3Combo.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {

						k3Coef.setText(""+newValue.getCoef());
						SaveData();

					}
				});
				k4Combo.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {

						k4Coef.setText(""+newValue.getCoef());
						SaveData();

					}
				});
				k5Combo.valueProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {

						k5Coef.setText(""+newValue.getCoef());
						SaveData();

					}
				});

				k1Coef.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				k2Coef.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				k3Coef.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				k4Coef.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				k5Coef.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				bodyWeightText.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				leanBWText.focusedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue==false&listen) {
						SaveData();

					}
				});
				GlobAnal.selectedProperty().addListener((observable, oldValue, newValue) -> {
					if (newValue!=null&listen) {
						SaveData();
					}
				});
				listDisease.setCellFactory(listView -> new CheckBoxListCell<ReferenceEv>(  listDisease::getItemBooleanProperty) {
					@Override
					public void updateItem(ReferenceEv employee, boolean empty) {
						super.updateItem(employee, empty);
						setText(employee == null ? "" : ( employee.getName()));
					}
				});
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

	}

	@FXML
	public void DeleteWeight(ActionEvent event) {
		event.consume();
		if(weightDateTable.getSelectionModel().getSelectedItem()!=null) {
			DataConnector.DeleteWeight(weightDateTable.getSelectionModel().getSelectedItem().getUUID(), null);
			mainApp.getCurrentAnimal().removeWeight(weightDateTable.getSelectionModel().getSelectedItem().getUUID());
			UpdateLists();
			createGraphWeight();

		}else {

		}


	}

	public void setOnClick() {

	}
	public void setMainApp(VetNutri mainApp) {
		this.mainApp=mainApp;
		MARC=setCoefTable();

addFormulaButton.setVisible(false);	
//creatChart(anim.getList().getRation(ConsUUID, RatUUID));
		//	creatChartEner(anim.getList().getRation(ConsUUID, RatUUID));
		rationAccord.setExpandedPane(rationTitled);	 

		IntakeTabPaneController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/intakeTabPane.fxml"));


		try {
			TabPane rt=(TabPane)loader.load();
			//rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);

			VBintake.getChildren().add(rt);

			coefwin=loader.getController();
			ObservableList<KindData>kd=FXCollections.observableArrayList();
			kd.add(KindData.SER);
			kd.add(KindData.BW);
			kd.add(KindData.MW);
			kd.add(KindData.FDESC);
			kd.add(KindData.DM);
			kd.add(KindData.FENER);
			kd.add(KindData.AMINO);
			kd.add(KindData.PP);
			kd.add(KindData.LIP);
			coefwin.setMainApp(kd, this);
			this.tabIntake=coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public void setAnimal() {

		ConsUUID=null;
		RatUUID=null;	
		
		UpdateAnimal();
		MARC.Update();

	}

	public void UpdateAnimal() {

		this.anim=mainApp.getCurrentAnimal();

		GWC.updateGrowth();
		areaAnam.setText(anim.getResume());




		ownerNameText.setText(anim.getNomProprio());
		animalNameText.setText(anim.getNom());
		especeLabel.setText (bundle.getString(Espece.getEnumFromStringId(anim.getEspece()).getName()) );
		identText.setText(anim.getId());

		breedText.setText(DataConnector.BreedName(anim.getEspece(), "FR", anim.getRace(), null));
		breedText.getEntries().clear();
		breedText.getEntries().addAll(DataConnector.readListBreed(anim.getEspece(), "FR"));
		birthDatePicker.setValue(anim.getDateNaiss());

		sexCombo.getSelectionModel().select( Sex.byID(anim.getSex()));





		refList=  	VetNutri.getMainListRef(Espece.getEnumFromStringId(anim.getEspece()).getName(), false);
		if (refList!=null) {
			if(refList.size()>0) {

				physioStatusCombo.setItems(refList);
				physioStatusCombo.getItems().sort(new Comparator<ReferenceEv>()
				{
					public int compare(ReferenceEv f1, ReferenceEv f2)
					{

						return f1.getName().compareTo(f2.getName());
					}        
				});

			}
		}else {

		}
		physioStatusCombo.getSelectionModel().clearSelection();
		disList=  	VetNutri.getMainListRef(Espece.getEnumFromStringId(anim.getEspece()).getName(), true);
		if (disList!=null) {


			listDisease.setItems(disList);
			listDisease.getItems().sort(new Comparator<ReferenceEv>()
			{
				public int compare(ReferenceEv f1, ReferenceEv f2)
				{

					return f1.getName().compareTo(f2.getName());
				}        
			});
		}else {

		}
		for(ReferenceEv r:listDisease.getItems()) {
			listDisease.getItemBooleanProperty(r).addListener((observable, oldValue, newValue) -> {
				if (listen) {
					SaveData();	
				}
			});
		}
		UpdateLists();
		createGraphWeight();
	}



	public void UpdateLists() {
		createNewB=true;
		weightDateTable.setItems(getWeightList());
		consTable.setItems(getConsultList());   
		consTable.sort();
		variableTable.setItems(getVariableList());
		ratTable.setItems(getRationList());
		variableTable.refresh();
		ratTable.refresh();
		ratTable.sort();
		consTable.refresh();
		weightDateTable.refresh();
		createNewB=false;
		boolean touch=false;
		for(ConsultP c: consTable.getItems()) {
			if (c.getUUID().equals(ConsUUID)) {
				consTable.getSelectionModel().select(c);
				touch=true;}}
		if (touch) {
			touch=false;
		}else {  consTable.getSelectionModel().selectLast();}
		for(RationP c: ratTable.getItems()) {
			if (c.getUUID().equals(RatUUID)) {
				ratTable.getSelectionModel().select(c);
				touch=true;
			}

		}

		if (touch) {
			touch=false;
		}else {  ratTable.getSelectionModel().selectLast();}
		ObservableList<AlimP> listA = getAlimList();

		alimTable.setItems(listA);
		SaveData();
	}



	@FXML
	public void DeleteAlim(ActionEvent event) {
		event.consume();
		if(alimTable.getSelectionModel().getSelectedItem()!=null) {
			DataConnector.DeleteFood(alimTable.getSelectionModel().getSelectedItem().getUUID(), null);
			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID).removeAliment(alimTable.getSelectionModel().getSelectedItem().getUUID());;
			alimTable.setItems(getAlimList());
			alimTable.refresh();
			this.SaveData();
		}else {

		}


	}

	@FXML
	public void DeleteConsultation(ActionEvent event) {
		event.consume();
		if(consTable.getItems().size()!=1&ConsUUID!=null) {

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("You will delete the Selected rconsultation ");
			alert.setContentText("Are you sure you want it??");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				DataConnector.DeleteConsultation(ConsUUID, null);
				mainApp.getCurrentAnimal().removeConsult(ConsUUID);
				ConsUUID=null;
				UpdateLists();
				createGraphWeight();
				consTable.getSelectionModel().selectLast();
			} else {
				// ... user chose CANCEL or closed the dialog
			}

		}else {
			consTable.getSelectionModel().selectLast();
		}
		this.SaveData();

	}

	@FXML
	public void NewConsultation(ActionEvent event) {
		if( event!=null) {
			event.consume();}


		ConsultationEv consu=new ConsultationEv();
		mainApp.getCurrentAnimal().addConsult(consu);
		this.ConsUUID=consu.getUUID();
		this.RatUUID=consu.getRationList().get(0).getUUID();   	 

		UpdateLists();


	}

	@FXML
	public void NewRation(ActionEvent event) {
		if( event!=null) {
			event.consume();}
		Ration consu=new Ration();
		if (ConsUUID!=null) {

			mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).addNewRation(consu);
			RatUUID=consu.getUUID();

		}
		UpdateLists();

	}

	@FXML
	public void DeleteRation(ActionEvent event) {
		event.consume();

		if(ratTable.getItems().size()!=1 & RatUUID!=null) {

			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Confirmation Dialog");
			alert.setHeaderText("You will delete the Selected ration ");
			alert.setContentText("Are you sure you want it??");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				DataConnector.DeleteRation(RatUUID, null);
				mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).removeRationByUUID(RatUUID);
				UpdateLists();
				createGraphWeight();
				ratTable.getSelectionModel().selectLast();
			} else {
				// ... user chose CANCEL or closed the dialog
			}



		}else {
			ratTable.getSelectionModel().selectLast();
		}
	}

	@FXML
	public void save(ActionEvent event) {

		SaveData();
		event.consume();
	}

	public void creatChart(Ration rat, RequirementAnalyzer ra, RationCalculator calc, ReferenceEv ref) {
		if (rat!=null&calc.getBEE()!=0) {
			if (rat.getAlimentList().size()!=0&!GlobAnal.isSelected()) {
				EnerView.setVisible(true);
				CompoView.setVisible(true);
				MinView.setVisible(true);
				MacroView.setVisible(true);
				VitaView.setVisible(true);
				MainView.setVisible(true);
				PhoProtView.setVisible(true);
				CalPhosView.setVisible(true);
				O6O3View.setVisible(true);


				float EnerAtwatModif= (float) (rat.getNutrient(NutrientBase.ENA)*3.5+rat.getNutrient(NutrientBase.PROTEINE)*3.5+rat.getNutrient(NutrientBase.LIPIDE)*8.5);
				EnerView.setChart(chartCreator.EnerPie(350*rat.getNutrient(NutrientBase.ENA)/EnerAtwatModif, 350*rat.getNutrient(NutrientBase.PROTEINE)/EnerAtwatModif, 850*rat.getNutrient(NutrientBase.LIPIDE)/EnerAtwatModif, false,1));
				CompoView.setChart(chartCreator.CompoPie(rat.getNutrient(NutrientBase.HUMIDITE), rat.getNutrient(NutrientBase.PROTEINE), rat.getNutrient(NutrientBase.LIPIDE), rat.getNutrient(NutrientBase.ENA), rat.getNutrient(NutrientBase.CELLULOSE), rat.getNutrient(NutrientBase.CENDRE),false,1));
				MinView.setChart(chartCreator.MinPlot(rat, calc, ra, ref, false,1));
				MainView.setChart(chartCreator.BasePlot(rat, calc, ra, ref, false,1));
				MacroView.setChart(chartCreator.MacroPlot(rat, calc, ra, ref, false,1));
				VitaView.setChart(chartCreator.VitaPlot(rat, calc, ra, ref, false,1));
				O6O3View.setChart(chartCreator.O6O3Plot(rat));
				CalPhosView.setChart(chartCreator.CaPPlot(rat));
				PhoProtView.setChart(chartCreator.ProphosPlot(rat));
			}else {
				EnerView.setVisible(false);
				CompoView.setVisible(false);
				MinView.setVisible(false);
				MacroView.setVisible(false);
				VitaView.setVisible(false);
				MainView.setVisible(false);
				PhoProtView.setVisible(false);
				CalPhosView.setVisible(false);
				O6O3View.setVisible(false);
			}}
	}
	public boolean prepaSave() {

		boolean bResponse=true;
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
					mainApp.getCurrentAnimal().setRace( DataConnector.AddBreed(mainApp.getCurrentAnimal().getEspece(),  bundle.getLocale().getLanguage().toUpperCase(),breedText.getText(), null));

					SaveData();
				} else if (option.get() == ButtonType.CANCEL) {
					bResponse=false;
					breedText.setText(DataConnector.BreedName(anim.getEspece(),  bundle.getLocale().getLanguage().toUpperCase(), anim.getRace(), null));

				} else {
					bResponse=false;
				}
			}else {
				mainApp.getCurrentAnimal().setRace(DataConnector.BreedID(anim.getEspece(), breedText.getText()));
			}

		}
		return bResponse;}

	void fun() {

		// execute func1
	}
	private CoefP isInListCoef(ObservableList<CoefP>ol, String uuid) {
		for(CoefP c:ol) {
			if (c.getUUID().equals(uuid)) {
				return c;
			}

		}
		return null;
	}
	private ObservableList<ReferenceEv> isInListRef(ObservableList<ReferenceEv>ol, ArrayList<String> uuid) {
		ObservableList<ReferenceEv> resp=FXCollections.observableArrayList();

		for(ReferenceEv c:ol) {
			if (uuid.contains(c.getUUID())) {
				resp.add(c);
			}

		}
		return resp;
	}
	private RequirementAnalyzer getAnalyzer( RationCalculator calc, ObservableList<SupplementalvariableP>svp, Ration rat) {
		ra=new RequirementAnalyzer();
		ra.setBEE(calc.getBEE());
		ra.setBW(calc.getOptiPoids());
		ra.setMW(calc.getPMOpti());
		ra.addReference(ref, calc, svp, rat);
		for(ReferenceEv s: listDisease.getCheckModel().getCheckedItems()){
			ra.addReference(s, calc, svp, rat);

		}
		return ra;
	}

	private MethodAdjustRationController setCoefTable () {

		MethodAdjustRationController coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/methodAdjust.fxml"));


		try {
			vbModif.getChildren().add((AnchorPane)loader.load());
			coefwin=loader.getController();
			coefwin.setMainApp(mainApp,  true);

			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	@FXML
	public void exportPP() {
		SaveData();
		new RemplirForm().ClipText(anim.getList().getConsultByUUID(ConsUUID), anim, new Vet(), calc.getBE(), calc.getOptiPoids());
		new PdfExport().createAnimalPdf(anim,anim.getList().getConsultByUUID(ConsUUID), calc, bundle, mainApp.getVet());
	}
	@FXML
	public void exportRat() {
		SaveData();
		ra=getAnalyzer(calc, FXCollections.observableList(mainApp.getCurrentAnimal().getList().getConsultByUUID(ConsUUID).getSuppVarp()),anim.getList().getRation(ConsUUID, RatUUID));


		new PdfExport().createRationPdf(anim,anim.getList().getConsultByUUID(ConsUUID),anim.getList().getConsultByUUID(ConsUUID).getRationByUUID(RatUUID), calc, 
				ref,
				ra,
				tabIntake.getNut(KindData.SER), tabIntake.getNut(KindData.BW),tabIntake.getNut(KindData.MW),
				NeedEner,
				NutrientEner
				,bundle, mainApp.getVet());
	}


	private void  createGraphWeight() {
		if (anim!=null) {
			
			GWC.update();}
	}

	private GraphWeightController createGraphWeightController() {
		GraphWeightController  coefwin;
		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bundle);
		loader.setLocation(VetNutri.class.getResource("/view/LineGraph.fxml"));


		try {
			TabPane rt=(TabPane)loader.load();

			//rt.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
			AnchWeight.getChildren().add(rt);


			AnchorPane.setTopAnchor(rt,(double) 0);
			AnchorPane.setLeftAnchor(rt,(double) 0);
			AnchorPane.setRightAnchor(rt,(double) 0);
			AnchorPane.setBottomAnchor(rt,(double) 0);
			coefwin=loader.getController();
			coefwin.setMainApp(this);
			return coefwin;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();


		}

		return null;
	}
	public ConsultationEv getConsultation(){
		if (anim!=null & ConsUUID!=null) {
			return anim.getList().getConsultByUUID(ConsUUID);
		}
		return null;
	}
	public AnimalEv getAnim() {
		return anim;
	}
}


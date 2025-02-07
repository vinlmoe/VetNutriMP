package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.ListSelectionView;

import DataStruct.AlimP;
import DataStruct.ConstrainP;
import DataStruct.DataBaseP;
import DataStruct.SpeciesConverter;
import Enumerise.ConditionEnum;
import Enumerise.KindData;
import Enumerise.NutrientBase;
import application.DataConnector;
import application.VetNutri;
import graph.component.ComboEditingCellConstrainConstrain;
import graph.component.ComboEditingCellKindConstrain;
import graph.component.ComboEditingCellUnitConstrain;
import graph.component.StringEditingCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Callback;
import model.AlimIndic;
import model.AlimentEv;
import model.AlimentUnif;
import model.ConditionSelection;
import model.Espece;
import model.TypeAlim;

public class FoodSelectorControler  implements Initializable {
private String RatUUID;
private String ConsUUID;
private AlimentUnif alimU;
private ObservableList<AlimP> mAlimList;
private Stage stage;
private ResourceBundle bundle;
private ObservableList<ConstrainP> constrains= FXCollections.observableArrayList();

@FXML
private ComboBox<Espece> speciesCombo;
    @FXML 
    private ComboBox dbCombo; 
    @FXML 
    private TableView <AlimP> alimTable;
    @FXML 
    private TableColumn <AlimP, String>brandColumn ; 
    @FXML 
    private TableColumn <AlimP, String> rangeColumn ; 
    @FXML
    private PieChart RawCompo; 
    @FXML
    private PieChart EnerCompo; 
    @FXML 
    private TableColumn <AlimP, String>nameColumn ; 
    @FXML
    private AnchorPane anchorgraph;
    @FXML
    private TableView<DataBaseP> BaseNutrientTable;

    @FXML
    private TextField searchText;
    @FXML 
    private ListSelectionView <AlimIndic>ListIndic;
    @FXML
    private  ComboBox<TypeAlim> dataCombo;
    
    
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
    private  void validate() {
   String UUIDtrans= alimTable.getSelectionModel().getSelectedItem().getUUID();
   if (UUIDtrans!=null) {
	// this.alimU= mainApp.get
	 stage.close();
   }else {
	   alimU=null;
	   stage.close();
   }

    }
    
    
  
   
    
    @Override 
    public void initialize(URL location, ResourceBundle resources) { 
bundle=resources;
        //AlimTabl
       
          alimTable.setEditable(true);

          brandColumn.setCellValueFactory(new PropertyValueFactory<>("Brand"));
          nameColumn.setCellValueFactory(new PropertyValueFactory<>("Nom"));
          rangeColumn.setCellValueFactory(new PropertyValueFactory<>("range"));
           
          ObservableList<AlimP> listA=null;
	
          
          alimTable.setItems(listA);
          alimTable.getSelectionModel().selectedItemProperty().addListener(
                  (observable, oldValue, newValue) -> ActualAlim(newValue));
          
         
          
          RawCompo.setLegendSide(Side.LEFT);
RawCompo.setTitle(resources.getString("RawCompoTitle"));
EnerCompo.setTitle(resources.getString("EnerCompoTitle"));





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
alimTable.setItems(searchAlimList());

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

for(TypeAlim ta:TypeAlim.values()) {
	  dataCombo.getItems().add(ta);
}

dataCombo.setCellFactory(col -> {   return new ListCell<TypeAlim>() {
    @Override
    protected void updateItem(TypeAlim item, boolean empty) {
        super.updateItem(item, empty);

        if (item == null || empty) {
            setText(null);
        } else {
            setText(item.nameToString());
        }
    }
    
    
};
});
	dataCombo.getSelectionModel().select(0);
	speciesCombo.getSelectionModel().select(0);
	dataCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
		 alimTable.setItems( searchAlimList());
		 dataCombo.getEditor().textProperty().set(newValue.nameToString());
	}); 
speciesCombo.setConverter(new SpeciesConverter(bundle));


	speciesCombo.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {

		 alimTable.setItems( searchAlimList());
		
	
	}); 

searchText.textProperty().addListener((observable, oldValue, newValue) -> {
    alimTable.setItems(searchAlimList());
});

    } 
    public void Update()  {
    	 alimTable.setItems(getAlimList());
    }
    
    VetNutri mainApp;
    public void setMainApp(VetNutri mainApp, Stage stage) {
    	this.mainApp=mainApp;
    	this.stage=stage;
    	this.mAlimList=mainApp.getmAlimList();

   	  }
   
    private ObservableList<AlimP> getAlimList()  {
    	return mainApp.getmAlimList();

    }
    
    
   
    
    public AlimentUnif getData() {
    	return alimU;
    }
    public void creatChart(AlimentEv alimentEv) {
    
		RawCompo.getData().clear();
    	PieChart.Data slice;
    	List<String> colorList = new ArrayList<>();
       for(int i=0; i<6;i++) {
    	   NutrientBase enu=NutrientBase.getByCoef(i);
    	   slice= new PieChart.Data(bundle.getString(enu.getLabel()), alimentEv.getNutrient(enu));
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
    

    
    private ObservableList<AlimP> searchAlimList()  {
    	ObservableList<AlimP> sAlimList=FXCollections.observableArrayList();
    	String textSearch= searchText.getText();
    	
    	if (!textSearch.equals("")){
			String [] words=textSearch.split(" ");
    	for (AlimP al:mAlimList) {
    		if (al.getAlim().getTypeAliment().getCoef()==dataCombo.getSelectionModel().getSelectedItem().getCoef()
    				&    				al.getAlim().isEspece(speciesCombo.getSelectionModel().getSelectedItem().getUUID())
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
        		if (al.getAlim().getTypeAliment().getCoef()==dataCombo.getSelectionModel().getSelectedItem().getCoef()
        			&    				al.getAlim().isEspece(speciesCombo.getSelectionModel().getSelectedItem().getUUID())
        			& isConstrained(al.getAlim())) {
        			sAlimList.add(al);
        		}
    		
    	}
    	}
    	
    	
    	return sAlimList;
    }
    
   public void creatChartEner(AlimentEv alimentEv) {
     	
    	PieChart.Data slice;
    	   EnerCompo.getData().clear();
    	   slice= new PieChart.Data(bundle.getString(NutrientBase.PROTEINE.getLabel()), alimentEv.getProtEner());
    	   EnerCompo.getData().add(slice);
    		slice.getNode().setStyle("-fx-pie-color: #" + 
                    NutrientBase.PROTEINE.getColr());
    	   slice= new PieChart.Data(bundle.getString(NutrientBase.ENA.getLabel()), alimentEv.getENAEner());
    	   EnerCompo.getData().add(slice);
    		slice.getNode().setStyle("-fx-pie-color: #" + 
                    NutrientBase.ENA.getColr());
    	   slice= new PieChart.Data(bundle.getString(NutrientBase.LIPIDE.getLabel()), alimentEv.getLipEner());
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
    private void ActualAlim(AlimP al) {
    	if (al!=null) {
    		creatChart(DataConnector.readAlim(al.getUUID(), null));
    
creatChartEner(DataConnector.readAlim(al.getUUID(), null));

    	    
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

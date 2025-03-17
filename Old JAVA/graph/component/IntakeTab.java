package graph.component;

import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.AAEnum;
import Enumerise.KindData;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import application.DataConnector;
import equation.Equation;
import equation.RequirementAnalyzer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import model.AlimentRation;
import model.RationCalculator;

public class IntakeTab extends Tab {

private ObservableList<CoefP>list=FXCollections.observableArrayList();
	
	


private Stage stage;
boolean indeletion=false;

private LabelData[] NutrientBEE;
boolean abort=false;
private ResourceBundle bundle;
private boolean analyzed=false;

private GridPane MainGrid=new GridPane();
private VBox vb=new VBox();
private Pane p=new Pane();

public IntakeTab(KindData kind, ResourceBundle bundle) {
	this.bundle=bundle;
	
	NutrientBEE=setLabel(NutrientBEE, MainGrid, kind, bundle);
	p.setMinHeight(10);
	MainGrid.setMinWidth(Control.USE_COMPUTED_SIZE);
	MainGrid.setMinHeight(Control.USE_COMPUTED_SIZE);
	
	
	
	this.setContent(vb);
	vb.getChildren().add(MainGrid);
	vb.getChildren().add(p);
	
	this.setText((kind.nameToString()));
}



public void updateValues(RationCalculator calc, RequirementAnalyzer ra, boolean glob) {
	for (int i=0; i<NutrientBEE.length;i++){

		NutrientBEE[i].UpdateValue(calc,  ra.getReferences(NutrientBEE[i].getMainEnum().getCoef(), NutrientBEE[i].getCoef()), bundle, glob);
		/**/
	
	}
}


private LabelData[] setLabel(LabelData[] Nutrient, GridPane Grid, KindData kinds,ResourceBundle resources) {
	Nutrient=new LabelData[NutrientBase.size()+NutrientMacro.size()+6+6+NutrientVitam.size()+2+NutrientMin.size()];
	int k=0;

	for(int i=0; i<Nutrient.length; i++){
		if (i==NutrientBase.size()) {
			k=0;
		}
		if (i==NutrientBase.size()+NutrientMacro.size()+6+6) {
			k=0;
		}
		if (i==NutrientBase.size()+NutrientMacro.size()+6+6+NutrientVitam.size()+2 ) {
			k=0;
		}

		if (i<NutrientBase.size()) {
			Nutrient[i]=new LabelData(NutrientBase.getByCoef(k), kinds);}
		else if (i>=NutrientBase.size() &i<NutrientBase.size() ) {
			Nutrient[i]=new LabelData();}
		else if (i>=NutrientBase.size() &i<NutrientBase.size()+NutrientMacro.size() ) {
			Nutrient[i]=new LabelData(NutrientMacro.getByCoef(k), kinds);}

		else if (i==NutrientBase.size()+NutrientMacro.size() ) {
			Nutrient[i]=new LabelData(NutrientLipid.O3, kinds);
			Nutrient[i+1]=new LabelData(NutrientLipid.O6, kinds);
			Nutrient[i+2]=new LabelData(NutrientLipid.AG204, kinds);
			Nutrient[i+3]=new LabelData(NutrientLipid.EPADHA, kinds);
			Nutrient[i+4]=new LabelData(NutrientLipid.AG205, kinds);
			Nutrient[i+5]=new LabelData(NutrientLipid.AG226, kinds);}

		else if (i==NutrientBase.size()+NutrientMacro.size()+6) {
			Nutrient[i]=new LabelData(NutrientAnalysis.PCa, kinds);
			Nutrient[i+1]=new LabelData(NutrientAnalysis.NaK, kinds);
			Nutrient[i+2]=new LabelData(NutrientAnalysis.ZnCu, kinds);
			Nutrient[i+3]=new LabelData(NutrientAnalysis.o6o3, kinds);
			Nutrient[i+4]=new LabelData(NutrientAnalysis.PhosphProt, kinds);
			Nutrient[i+5]=new LabelData();}
		else if (i>=NutrientBase.size()+NutrientMacro.size()+6+6&i<NutrientBase.size()+NutrientMacro.size()+6+6+NutrientVitam.size() ) {
			Nutrient[i]=new LabelData(NutrientVitam.getByCoef(k), kinds);}
		else if (i==NutrientBase.size()+NutrientMacro.size()+6+6+NutrientVitam.size() ) {
			Nutrient[i]=new LabelData(NutrientOther.TAURINE, kinds);
			Nutrient[i+1]=new LabelData(NutrientOther.CARNITINE, kinds);
		}
		else if (i>=NutrientBase.size()+NutrientMacro.size()+6+6+NutrientVitam.size() +2) {
			Nutrient[i]=new LabelData(NutrientMin.getByCoef(k), kinds);}

		k++;
		Grid.add(Nutrient[i],   Math.floorDiv(i, 12), Math.subtractExact(Math.round((float)i), 12*Math.floorDiv(i, 12)));

		Nutrient[i].SetVoid(bundle);

	}
	return Nutrient;
}

private LabelData[] setLabelAA(LabelData[] Nutrient, GridPane Grid, ResourceBundle resources) {
	Nutrient=new LabelData[12*5];
	int k=0;
	KindData[] kad= {
			KindData.SER,
			KindData.BW,
			KindData.MW,
			KindData.DM,
			KindData.FENER
	};
	Nutrient[] nutrients= {AAEnum.LYSINE,
			AAEnum.METHIONINE,
			NutrientAnalysis.MethCys,
			AAEnum.TRYPTOPHANE,
			AAEnum.THREONINE,
			AAEnum.ARGININE,
			AAEnum.ISOLEUCINE,
			AAEnum.VALINE,
			AAEnum.LEUCINE,
			AAEnum.HISTIDINE,
			AAEnum.PHENYLALANINE,
			NutrientAnalysis.PhenTyr};
			
	
for (KindData kda:kad) {


for (Nutrient n:nutrients) {
Nutrient[k]=new LabelData(n, kda);
Grid.add(Nutrient[k],   Math.floorDiv(k, 12), Math.subtractExact(Math.round((float)k), 12*Math.floorDiv(k, 12)));
k++;
}}
	
		

		

	
	return Nutrient;
}

public void returnNALabel() {
	for (LabelData l:NutrientBEE) {
		l.SetVoid(bundle);
	}
}

public boolean isIndeletion() {
	return indeletion;
}
		
		}

package controller;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;

import DataStruct.AlimP;
import DataStruct.CoefP;
import DataStruct.NutrientRefP;
import Enumerise.AAEnum;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import application.DataConnector;
import application.VetNutri;
import equation.Equation;
import equation.MiniReqAnalyszer;
import equation.RequirementAnalyzer;
import graph.component.FloatEditingCell;
import graph.component.IntakeTab;
import graph.component.LabelData;
import graph.component.StringEditingCell;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.PopupWindow.AnchorLocation;
import javafx.util.Callback;
import model.AlimentRation;
import model.RationCalculator;

public class IntakeTabController  implements Initializable {


private Stage stage;
boolean indeletion=false;
RationCalculator calc;
RequirementAnalyzer ra;
private LabelData[] NutrientBEE;
boolean abort=false;
private ResourceBundle bundle;
private boolean analyzed=false;


//Définition graphique 
@FXML 
private  GridPane MainGrid;

@FXML 
private  Tab mainTab;
private KindData kindData;



@Override 
public void initialize(URL location, ResourceBundle resources) {

	bundle=resources;
	//AlimTabl


      
}


public void setTab(KindData kd) {
	kindData=kd;
	

	switch(kd) {
	case BW:
		mainTab.setText(bundle.getString("intakeBW"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		break;
	case DM:
		mainTab.getStyleClass().add("diet");
		mainTab.setText(bundle.getString("foodDry"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		break;
	case FDESC:
		mainTab.setText(bundle.getString("foodDesc"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		mainTab.getStyleClass().add("diet");
		break;
	case FENER:
		mainTab.setText(bundle.getString("foodEner"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		mainTab.getStyleClass().add("diet");
		break;
	case INDICAT:
		break;
	case INGRED:
		break;
	case MW:
		mainTab.setText(bundle.getString("intakeMW"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		break;
	case NO:
		break;
	case SER:
		mainTab.setText(bundle.getString("intakeSER"));
		NutrientBEE=setLabel(NutrientBEE, MainGrid, kd, bundle);
		break;
	case AMINO:
		mainTab.setText(bundle.getString("AminoAcids"));
		NutrientBEE=setLabelAA(NutrientBEE, MainGrid,  bundle);
		mainTab.getStyleClass().add("aminoac");
		break;
	case PP:
		mainTab.setText(bundle.getString("SuppData"));
		NutrientBEE=setLabelProtPhos(NutrientBEE, MainGrid,  bundle);
		mainTab.getStyleClass().add("aminoac");
		break;
	case LIP:
		mainTab.setText(bundle.getString("SuppLip"));
		NutrientBEE=setLabelAG(NutrientBEE, MainGrid,  bundle);
		mainTab.getStyleClass().add("aminoac");
		break;
	default:
		break;
	
	}
}


public void updateValues(RationCalculator calc, MiniReqAnalyszer ra, Boolean glob) {

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

private LabelData[] setLabelAG(LabelData[] Nutrient, GridPane Grid, ResourceBundle resources) {
	Nutrient=new LabelData[12*5];
	int k=0;
	KindData[] kad= {
			KindData.SER,
			KindData.BW,
			KindData.MW,
			KindData.DM,
			KindData.FENER
	};
	Nutrient[] nutrients= {NutrientLipid.AG40,
			NutrientLipid.AG60,
			NutrientLipid.AG80,
			NutrientLipid.AG100,
			NutrientLipid.AG120,
			NutrientLipid.AG140,
			NutrientLipid.AG160,
			NutrientLipid.AG180,
			NutrientLipid.AG181,
			NutrientLipid.AG182,
			NutrientLipid.AG183,
			NutrientLipid.CHOL};
	System.out.println(Grid.getRowCount()+"/"+nutrients.length);
		
	
for (KindData kda:kad) {


for (Nutrient n:nutrients) {
Nutrient[k]=new LabelData(n, kda);

Grid.add(Nutrient[k],   Math.floorDiv(k, Grid.getRowCount()), Math.subtractExact(Math.round((float)k), 12*Math.floorDiv(k, Grid.getRowCount())));
k++;
}}
	
		

		

	
	return Nutrient;
}


private LabelData[] setLabelProtPhos(LabelData[] Nutrient, GridPane Grid, ResourceBundle resources) {
	Nutrient=new LabelData[7];
	int k=0;
	KindData[] kad= {
		
			KindData.DM,

	};
	Nutrient[] nutrients= {NutrientBase.PROTEINE, 
			NutrientMacro.CAL,
			NutrientMacro.PHOS, 
			NutrientAnalysis.PhosphProt,
			NutrientAnalysis.nonOsProt,
			NutrientAnalysis.nonOsPhos,
			NutrientAnalysis.nonOsPP
			
			
	
			
			
	};
			
	
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
		
		public LabelData[] getNutrientBEE() {
			return NutrientBEE;
		}
		public KindData getKindData() {
			return kindData;
		}






		}

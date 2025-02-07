package graph.component;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import Aliments.pourcentPart;
import DataStruct.NutrientRef;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientEnergy;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import equation.Equation;
import javafx.collections.ObservableList;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.PopupWindow.AnchorLocation;
import model.AnimalEv;
import model.ConsultationEv;
import model.Ration;
import model.RationCalculator;
import model.RefValues;
import model.Reference;
import model.ReferenceEv;

public class LabelData extends Label{
	private Nutrient nut=NutrientAnalysis.NaK;

	private MainNutrientEnum mainEnum= MainNutrientEnum.NO;
	private KindData kd;
	private float value;
	private float divider;
	private boolean isval;
	private String equat="";
	private String paricip="";

	private RefValues refv= new RefValues();

	private String val=new String("NA");
	public LabelData(Nutrient n, KindData kd) {
		this.nut=n;
	
		mainEnum= n.getMNE();
		this.kd=kd;

	}
	public String getEquat() {
		return equat;
	}
public void setEquat(String equat) {
	this.equat = equat;
}
	public LabelData() {
		this.kd=KindData.NO;
		this.mainEnum=MainNutrientEnum.NO;
	}


	public void SetVoid(ResourceBundle resources) {
		this.getStyleClass().clear();
		this.getStyleClass().add("label");
		if(mainEnum.equals(MainNutrientEnum.NO)) {
			this.setText("");
		}else if (mainEnum.equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)){
			this.setText(resources.getString(getLabel())+ ": NA " +getUnit());
			this.getStyleClass().add("absent");
		}else {

			this.setText(resources.getString(getLabel())+ ": NA " +getUnit()+"/"+ kd.getUnit());
			this.getStyleClass().add("absent");
		}
	}
	public void SetNoSys(ResourceBundle resources) {
		this.getStyleClass().clear();
		this.getStyleClass().add("label");
		if(mainEnum.equals(MainNutrientEnum.NO)) {
			this.setText("");
		}else if (mainEnum.equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)){
			this.setText(resources.getString("NoSys"));
			this.getStyleClass().add("dangerous");
		}else {

			this.setText(resources.getString("NoSys"));
			this.getStyleClass().add("dangerous");
			}
	}
	/*public void Update(  LabelData l, float divider, ResourceBundle resources) {

		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
		value=l.getVal()*l.getDivider()/divider;
		this.getStyleClass().clear();
		this.getStyleClass().add("label");
		if ((value)<1) { twoDForm = new DecimalFormat("#.###");}
		else	if ((value)<10) { twoDForm = new DecimalFormat("#.##");}
		
		this.setText(resources.getString(getLabel())+ ": "+twoDForm.format(value) +" " +getUnit());
		
		if(mainEnum.equals(MainNutrientEnum.NO)) {
			this.setText("");

		}else  if (mainEnum.equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)){
			if (value<10) { twoDForm = new DecimalFormat("#.##");}
			this.setText(resources.getString(getLabel())+ ": "+twoDForm.format(value) +" " +getUnit());
		
			
		}else {
			
		
		if (isval) {
			if(!this.mainEnum.equals(MainNutrientEnum.ENERGIE)) {
				if ((value)<1) { twoDForm = new DecimalFormat("#.###");}
				else	if ((value)<10) { twoDForm = new DecimalFormat("#.##");}
				this.setText(resources.getString(getLabel())+ ":  "+twoDForm.format(value) +" "+getUnit()+"/"+ kd.getUnit()+refv.getSign());} else{
					this.setText(resources.getString(getLabel())+ ":  "+twoDForm.format(value) +" "+getUnit()+"    ");	}

			
		}
		else {
			if ((value)<1) { twoDForm = new DecimalFormat("#.###");}
			else	if ((value)<10) { twoDForm = new DecimalFormat("#.##");}
			this.setText(resources.getString(getLabel())+ ":  > "+twoDForm.format(value) +" "+getUnit()+"/"+ kd.getUnit()+" /!\\"+refv.getSign());
			
		}
		
		}
		if (kd.equals(KindData.BW)|kd.equals(KindData.MW)|kd.equals(KindData.SER)) {
			if (!isval& value==0) {
				this.getStyleClass().add("absent");
			}else {
				if (refv.getColor().equals("F81818")){

					this.getStyleClass().add("dangerous");}else if( refv.getColor().equals("1844F8")) {
						this.getStyleClass().add("notopti");
					}else if( refv.getColor().equals("F333FF")) {
						this.getStyleClass().add("dis");
					}
					else 
					{
						this.getStyleClass().add("normal");
					
						}

					}
	}
		WebView  web = new WebView();
		WebEngine webEngine = web.getEngine();
		webEngine.loadContent
		(refv.getNorm(kd)+
				paricip
				);
		web.setPrefHeight(300);
		web.setPrefWidth(300);
		web.setFontScale(0.8);
		Tooltip  tip = new Tooltip();
		tip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		tip.setGraphic(web);
		tip.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
		this.setTooltip(tip);
	}*/
	public void UpdateValue(RationCalculator calc,  ObservableList<NutrientRef> ol,ResourceBundle resources, boolean glob) {


		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
	
		if (glob) {
			isval=this.isVal(calc.getRationList());
		}else {
			isval=this.isVal(calc.getRation());
		}
		 divider=1.0F;
		 if(!glob) {
		 paricip=getParticip( calc.getRation());}
		 else { paricip=getParticip( calc.getRationList());}
		float div=1.0F;
		 value=glob? getValue(calc.getRationList()): getValue(calc.getRation()); 
		String coloring= new String( "000000");
		refv=new RefValues();


		refv.setRef((value), ol, calc.getBEE(), calc.getOptiPoids(),calc.getPMOpti(),  kd, mainEnum, nut);
	
		this.getStyleClass().clear();
		this.getStyleClass().add("label");

		if(mainEnum.equals(MainNutrientEnum.NO)) {
			this.setText("");

		}else  if (mainEnum.equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)){
			if (value<10) { twoDForm = new DecimalFormat("#.##");}

		value=glob?	0:getValue(calc.getRation());
			this.setText(resources.getString(getLabel())+ ": "+(glob?"NA"	:(twoDForm.format(value) +" ")) +getUnit());
			val=""+twoDForm.format(value);
			if (refv.getColor().equals("F81818")){

				this.getStyleClass().add("dangerous");}else if( refv.getColor().equals("1844F8")) {
					this.getStyleClass().add("notopti");
				}else if( refv.getColor().equals("F333FF")) {
					this.getStyleClass().add("dis");
				}
				else 
				{
					this.getStyleClass().add("normal");
				}
		
			//	this.setTextFill(Paint.valueOf("000000"));
		}else {
		
			switch(kd){
			case SER:
				divider=calc.getBEE()/1000;

				value=value/divider;

				break;
			case BW:
				divider=calc.getOptiPoids();

				value=value/divider;

				break;
			case MW:
				divider=calc.getPMOpti();

				value=value/divider;

				break;
			case FENER:
				divider=(glob?getEner(calc.getRationList()):calc.getRation().getEnerT())/1000;
				value=value/divider;
				break;
			case FDESC:
				divider=(glob? getPoids(calc.getRationList()):  calc.getRation().getPoids())/100;
				value=value/divider;
				break;
			case DM:
				divider=(glob?(getPoids(calc.getRationList())-getHum(calc.getRationList()))  :(calc.getRation().getPoids()-calc.getRation().getNutrient(NutrientBase.HUMIDITE)))/100;
				value=value/divider;
				break;

			case NO:
			
				if (mainEnum.equals(MainNutrientEnum.ENERGIE)) {
				
					if (nut.equals(NutrientEnergy.TOT)){
						divider=1;
						value=value/divider;
					}
					else	if (nut.equals(NutrientEnergy.DE)){
						divider=(glob? getPoids(calc.getRationList()):  calc.getRation().getPoids())/100;
						value=value/divider;
						equat="Def DE: "+(calc.getEqAlim()!=null?calc.getEqAlim().getEquationScript():"")+
								"<br/>Com DE: "+(calc.getEqAlimCom()!=null?calc.getEqAlimCom().getEquationScript():"");			
						
					}
					else	if (nut.equals(NutrientEnergy.DEDM)){
						divider=(glob?(getPoids(calc.getRationList())-getHum(calc.getRationList()))  :(calc.getRation().getPoids()-calc.getRation().getNutrient(NutrientBase.HUMIDITE)))/100;
							value=value/divider;
							equat="Def DE: "+(calc.getEqAlim()!=null?calc.getEqAlim().getEquationScript():"")+
									"<br/>Com DE: "+(calc.getEqAlimCom()!=null?calc.getEqAlimCom().getEquationScript():"");			
						
					}
					else	if (nut.equals(NutrientEnergy.K)){
						divider=calc.getBEE();
						value=value/divider;
					}
					else	if (nut.equals(NutrientEnergy.PERC)){
						divider=calc.getBE()/100;
						value=value/divider;
					}
					else		if (nut.equals(NutrientEnergy.KPRED)){
						divider=calc.getBEE();
						value=calc.getBE()/divider;
					}
				else	if (nut.equals(NutrientEnergy.MW)){
					divider=1;
					value=calc.getPM();
					equat=calc.getEqBW()!=null?calc.getEqBW().getEquationScript():"";			
				
				}
				else			if (nut.equals(NutrientEnergy.BEE)){
					divider=1;
					value=calc.getBEE();
					
					equat=calc.getEqBEE()!=null?calc.getEqBEE().getEquationScript():"";				}
				else		if (nut.equals(NutrientEnergy.BE)){
					divider=1;
					value=calc.getBE();
				}


				else {

					divider=1;

				}}
			case INDICAT:
				break;
			case INGRED:
				break;
			default:
				break;}

			if (isval) {
				if(!this.mainEnum.equals(MainNutrientEnum.ENERGIE)) {
					if ((value)<1) { twoDForm = new DecimalFormat("#.###");}
					else	if ((value)<10) { twoDForm = new DecimalFormat("#.##");}
					this.setText(resources.getString(getLabel())+ ":  "+twoDForm.format(value) +" "+getUnit()+"/"+ kd.getUnit()+refv.getSign());} else{
						this.setText(resources.getString(getLabel())+ ":  "+twoDForm.format(value) +" "+getUnit()+"    ");	}

				
			}
			else {
				if ((value)<1) { twoDForm = new DecimalFormat("#.###");}
				else	if ((value)<10) { twoDForm = new DecimalFormat("#.##");}
				this.setText(resources.getString(getLabel())+ ":  > "+twoDForm.format(value) +" "+getUnit()+"/"+ kd.getUnit()+" /!\\"+refv.getSign());
				
			}
			
			
			if (glob&mainEnum.equals(MainNutrientEnum.ANA)&!nut.equals(NutrientAnalysis.MethCys)&!nut.equals(NutrientAnalysis.PhenTyr)){
				this.setText(resources.getString(getLabel())+ ":   "+"NA" +" "+getUnit()+"/"+ kd.getUnit()+" /!\\"+refv.getSign());
				
					isval=false; 
					value=0;
				}
			//Couleur des labels


			if (kd.equals(KindData.BW)|kd.equals(KindData.MW)|kd.equals(KindData.SER)) {
				if (!isval& value==0) {
					this.getStyleClass().add("absent");
				}else {
					if (refv.getColor().equals("F81818")){

						this.getStyleClass().add("dangerous");}else if( refv.getColor().equals("1844F8")) {
							this.getStyleClass().add("notopti");
						}else if( refv.getColor().equals("F333FF")) {
							this.getStyleClass().add("dis");
						}
						else 
						{
							this.getStyleClass().add("normal");
						}}}else if (this.mainEnum.equals(MainNutrientEnum.ENERGIE)) {
							if (nut.equals(NutrientEnergy.PERC)|nut.equals(NutrientEnergy.TOT)) {
							
								if (Math.abs(((glob?getEner(calc.getRationList()):(calc.getRation().getEnerT()))-calc.getBE())/calc.getBE())>0.05){
									this.getStyleClass().add("dangerous");
								}else {
									this.getStyleClass().add("normal");
								}
							}

						}
		}
		
		toolTipEdit();

	}
	public void toolTipEdit () {

		WebView  web = new WebView();
		WebEngine webEngine = web.getEngine();
		webEngine.loadContent
		(this.getText()+"<br/>"+
			(	equat.isBlank()?"":("Equation: "+equat+"<br/>"))+(
	!(nut.equals(NutrientEnergy.BEE)|	nut.equals(NutrientEnergy.BE)
		|nut.equals(NutrientEnergy.MW)|nut.equals(NutrientEnergy.KPRED))	?refv.getNorm(kd)+paricip:"")
				
				);
		web.setPrefHeight(300);
		web.setPrefWidth(300);
		web.setFontScale(0.8);
		Tooltip  tip = new Tooltip();
		tip.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
		tip.setGraphic(web);
		tip.setAnchorLocation(AnchorLocation.WINDOW_TOP_LEFT);
		this.setTooltip(tip);
	}
	public String getLabel() {
		if(nut!=null) {	
			return	nut.getLabel()
					;	}else {return "";}	}


	public String getSign() {
		return refv.getSign();
	}
	private String getParticip( Ration rat) {

		String s=new String();
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent;
		String reponse="";
		pourcentPart p;


		if (!mainEnum.equals(MainNutrientEnum.ANA)&!mainEnum.equals(MainNutrientEnum.ENERGIE)) {
			pourcent=rat.getNutrientPart(nut);

			for (int i =pourcent.length-1; i>-1; i--){
				p=pourcent[i];
				reponse+=" <br/> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
			}
			s+=reponse;

			return s;}else if (mainEnum.equals(MainNutrientEnum.ENERGIE)) {


				pourcent=rat.getNutrientPartEner();
				for (int i =pourcent.length-1; i>-1; i--){
					p=pourcent[i];
					reponse+=" <br/> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
				}
				s=reponse;

				return s;
			}else if (mainEnum.equals(MainNutrientEnum.ANA)) {
				return "";}
			else {
				return "";
			}
	}
	private String getParticip( ArrayList<Ration> rat) {
				return "";
	}
	
	public float getValue(Ration rat) {
if (mainEnum.equals(MainNutrientEnum.ENERGIE)) {
	return rat.getEnerT();} else if (mainEnum.equals(MainNutrientEnum.NO)) {
		return 0;
	}
else {
			return rat.getNutrient(nut);}
	
		
	}
	public float getValue(ArrayList<Ration> rat) {
		float sol=0;
		float sumCof=0;
		if (mainEnum.equals(MainNutrientEnum.ENERGIE)) {
		
			for (Ration r:rat) {
				sol+=r.getEnerT()*r.getCoef();
				sumCof+=r.getCoef();
			}
			
			return sol/sumCof;} else if (mainEnum.equals(MainNutrientEnum.NO)) {
				return 0;
			}
		else {
			
			for (Ration r:rat) {
				sol+=r.getNutrient(nut)*r.getCoef();
				sumCof+=r.getCoef();
			}
			
			return sol/sumCof;
					}
			
				
			}
	public float getEner(ArrayList<Ration> rat) {
		float sol=0;
		float sumCof=0;
	
		
			for (Ration r:rat) {
				sol+=r.getEnerT()*r.getCoef();
				sumCof+=r.getCoef();
			}
			
			return sol/sumCof;
			
				
			}
	
	public float getPoids(ArrayList<Ration> rat) {
		float sol=0;
		float sumCof=0;
	
		
			for (Ration r:rat) {
				sol+=r.getPoids()*r.getCoef();
				sumCof+=r.getCoef();
			}
			
			return sol/sumCof;
			
				
			}
	
	public float getHum(ArrayList<Ration> rat) {
		float sol=0;
		float sumCof=0;
	
		
			for (Ration r:rat) {
				sol+=r.getNutrient(NutrientBase.HUMIDITE)*r.getCoef();
				sumCof+=r.getCoef();
			}
			
			return sol/sumCof;
			
				
			}
	public String getUnit() {
			return nut.getUnite();
	}
	private boolean isVal(Ration rat) {
		switch(mainEnum){
		case BASE:
		case MIN:
		case MACRO:
			case LIPID:
			case VITAM:
			case AMA:
			case OTHER:
		return rat.isNutrient(nut);
		
		case ANA:
			return true;
		default:
			return true;
		}
	}
	private boolean isVal(ArrayList<Ration> rat) {
		boolean v=false;
		for(Ration r:rat) {
			v|=isVal(r);
		}
		return v; 
	}
	public int getCoef() {
		switch(mainEnum){
		case BASE:
		
		case MIN:
		
		case MACRO:
		
		case LIPID:
			
		case VITAM:
			
		case OTHER:
			
		case ANA:
		case AMA:

		case ENERGIE:
			return nut.getCoef();
		default:
			return 0;
		}
	}
	public float getVal() {
		return value;
	}
	public float getDivider() {
		return divider;
	}
	 public boolean isIsval() {
		return isval;
	}
	
	public MainNutrientEnum getMainEnum() {
		return mainEnum;
	}

}

package model;

import java.text.DecimalFormat;
import java.util.ResourceBundle;

import Aliments.pourcentPart;
import Enumerise.ConditionEnum;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientEnergy;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Paint;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
public class ConditionSelection {
private NutrientAnalysis ana;
private NutrientBase base; 
private NutrientLipid lipid;
private NutrientMacro macro;
private NutrientMin min;
private NutrientOther other;
private NutrientVitam vitam;
private NutrientEnergy ener;
private MainNutrientEnum mainEnum;
private KindData kd;
private ResourceBundle resources;
private ConditionEnum CE;
private float valueF;
private String valueS=new String();

public ConditionSelection(NutrientAnalysis anal, KindData kd, ConditionEnum ce, float value ) {
	this.ana=anal;
	mainEnum= MainNutrientEnum.ANA;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}
public ConditionSelection() {
this.kd=KindData.NO;
this.mainEnum=MainNutrientEnum.NO;
}

public ConditionSelection(NutrientVitam anal, KindData kd, ConditionEnum ce, float value ) {
	this.vitam=anal;
	mainEnum= MainNutrientEnum.VITAM;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientEnergy an, KindData kd, ConditionEnum ce, float value ) {

	mainEnum= MainNutrientEnum.ENERGIE;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientBase anal, KindData kd, ConditionEnum ce, float value ) {
	this.base=anal;
	mainEnum= MainNutrientEnum.BASE;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientLipid anal, KindData kd, ConditionEnum ce, float value ) {
	this.lipid=anal;
	mainEnum= MainNutrientEnum.LIPID;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientMacro anal, KindData kd, ConditionEnum ce, float value ) {
	this.macro=anal;
	mainEnum= MainNutrientEnum.MACRO;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientMin anal, KindData kd, ConditionEnum ce, float value ) {
	this.min=anal;
	mainEnum= MainNutrientEnum.MIN;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(NutrientOther anal, KindData kd, ConditionEnum ce, float value ) {
	this.other=anal;
	mainEnum= MainNutrientEnum.OTHER;
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}
public ConditionSelection(String anal, KindData kd, ConditionEnum ce, float value ) {
	setKind(anal);
	
	this.kd=kd;
this.CE=ce;
this.valueF=value;
}

public ConditionSelection(String anal, ConditionEnum ce, String valueS) {
	setKind(anal);
	
this.CE=ce;
this.valueS=valueS;
}

public boolean isCondition(AlimentEv alim) {
	boolean result; 
	
	switch(mainEnum){
	case BASE:
	case MIN:
	case MACRO:
	case LIPID:
	case VITAM:
	case OTHER:
	
		return CompareFloat(alim.getNutrient(base)/getDivider(alim));
	
	
	case ANA:
		return true;
	case ENERGIE:
		return CompareFloat(alim.getEner(Espece.CHIEN)/getDivider(alim));
		
	case INGREDIENT:
		if (CE.equals(ConditionEnum.INCLUDE)) {
		return alim.getIngredients().toLowerCase() .contains(valueS.toLowerCase());}
		else {
			return !alim.getIngredients().toLowerCase().contains(valueS.toLowerCase());
		}

		default:
			return false;
			
	}

}
	
	public float getDivider(AlimentEv alimentEv ) {
	
		
		switch(kd){
		
		case FENER:
			return alimentEv.getEner(Espece.CHAT)/1000;
		
			case FDESC:
			return 1;
			
			case DM:
				return(100-alimentEv.getNutrient(NutrientBase.HUMIDITE))/100;
			
				default:
					return 0F;
		}
					
			}
			
	
	public boolean CompareFloat (float value) {
		switch (CE) {
		case MORE:
			return value>valueF;
		case LESS:
			return value<valueF;
			default:
				return false;
		}
	}
	
public void	setKind(String name){
	mainEnum=NutrientBase.isByLabel(name)? MainNutrientEnum.BASE:
		 NutrientMacro.isByLabel(name)? MainNutrientEnum.MACRO:
			 NutrientMin.isByLabel(name)? MainNutrientEnum.MIN:
				 NutrientLipid.isByLabel(name)? MainNutrientEnum.LIPID:
					 NutrientVitam.isByLabel(name)? MainNutrientEnum.VITAM:
						 name.equals("ENER")? MainNutrientEnum.ENERGIE:
							 name.equals("INGREDIENT")? MainNutrientEnum.INGREDIENT:MainNutrientEnum.NO;
	
	switch(mainEnum){
	case BASE:
		base=NutrientBase.getByLabel(name);
		break;
	case MIN:
		min=NutrientMin.getByLabel(name);
		break;
	case MACRO:
		macro=NutrientMacro.getByLabel(name);
		break;
	case LIPID:
		lipid=NutrientLipid.getByLabel(name);
		break;
	case VITAM:
		vitam=NutrientVitam.getByLabel(name);
		break;
	case OTHER:
	other=NutrientOther.getByLabel(name);
	
		break;
	}
	}

private String getUnit() {
	switch(mainEnum){
		case BASE:
			return base.getUnite();
		case MIN:
			return min.getUnite();
		case MACRO:
			return macro.getUnite();
		case LIPID:
			return lipid.getUnite();
		case VITAM:
			return vitam.getUnite();
		case OTHER:
			return other.getUnite();
		case ANA:
			return ana.getUnite();
		case ENERGIE:
			return ener.getUnite();
			default:
				return "";
	}
}
private boolean isVal(Ration rat) {
	switch(mainEnum){
		case BASE:
			
			return rat.isNutrient( base);
		case MIN:
			return rat.isNutrient( min);
		case MACRO:
			return rat.isNutrient( macro);
		case LIPID:
			return rat.isNutrient( lipid);
		case VITAM:
			return rat.isNutrient( vitam);
		case OTHER:
			return rat.isNutrient( other);
		case ANA:
			return true;
			default:
				return true;
	}
}
}
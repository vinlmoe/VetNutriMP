package DataStruct;



import java.util.Date;

import Enumerise.ConditionEnum;
import Enumerise.KindData;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientVitam;
import Enumerise.UnitEnum;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.AlimentRation;
import model.AlimentUnif;
import model.ConsultationEv;
import model.Ration;
import model.targetAdjust;

public class ConstrainP{



private SimpleStringProperty Name; 

private MainNutrientEnum MNE;

private boolean present;

private SimpleStringProperty Quantity =new SimpleStringProperty ("0.0");
private SimpleObjectProperty<KindData> Unit ;
private SimpleObjectProperty<ConditionEnum> CE=new SimpleObjectProperty<ConditionEnum>(ConditionEnum.MORE)  ;



public ConstrainP( String name, String q,ConditionEnum cEnum, KindData unit  ) {

setName(name);
setCE(cEnum);
setQuantity(q);
setUnit(unit);

}
public ConditionEnum getCE() {
	return CE.get();
}
public void setCE(ConditionEnum cE) {
	switch(MNE) {
	case INGREDIENT:
	case INDICAT:
	
	if (cE!= ConditionEnum.EXCLIDE&cE!= ConditionEnum.INCLUDE) {
		cE=ConditionEnum.INCLUDE;
	
	}
	break;
	default:
		if (cE!= ConditionEnum.MORE&cE!= ConditionEnum.LESS) {
			cE=ConditionEnum.MORE;
		}
	break;}
	CE = new SimpleObjectProperty<ConditionEnum>(cE);
}
public MainNutrientEnum getMNE() {
	return MNE;
}
public void setMNE(MainNutrientEnum mNE) {
	MNE = mNE;
}
public String getName() {
	return Name.get();
}
public void setName(String name) {
	setMNE( NutrientBase.isByLabel(name)? MainNutrientEnum.BASE:
		 NutrientMacro.isByLabel(name)? MainNutrientEnum.MACRO:
			 NutrientMin.isByLabel(name)? MainNutrientEnum.MIN:
				 NutrientLipid.isByLabel(name)? MainNutrientEnum.LIPID:
					 NutrientVitam.isByLabel(name)? MainNutrientEnum.VITAM:
						 name.equals("ENER")? MainNutrientEnum.ENERGIE:
							 name.equals("INGREDIENT")? MainNutrientEnum.INGREDIENT:MainNutrientEnum.NO);
	setCE(this.getCE());
	Name = new SimpleStringProperty(name);
	switch(MNE) {
	case INGREDIENT:
	case INDICAT:	
	try {
		Float.parseFloat(getQuantity());
		setQuantity("");
		}catch (NumberFormatException e) {
			}
	break;
			default:
				try {
					Float.parseFloat(getQuantity());
					}catch (NumberFormatException e) {
						setQuantity("0.0");}
				break;
	}
}
public String getQuantity() {
	return Quantity.get();
}
public void setQuantity(String quantity) {
	switch(MNE) {
	case INGREDIENT:
	case INDICAT:
		break;
	
	default:
	quantity=noPoint(quantity);
	break;
	}
	
	Quantity = new SimpleStringProperty(quantity);
}
public KindData getUnit() {
	return Unit.get();
}
public void setUnit(KindData unit) {
	Unit = new  SimpleObjectProperty<KindData> ( unit);
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

ReP="0.0";
}
	return ReP;
}
}

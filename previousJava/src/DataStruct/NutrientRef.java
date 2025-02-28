package DataStruct;


import java.util.Date;

import Enumerise.MainNutrientEnum;
import Enumerise.Reflevel;
import Enumerise.ReturnRefTrigger;
import Enumerise.UnitEnum;
import Enumerise.UnitReqEnum;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AlimentRation;
import model.AlimentUnif;
import model.BiblioRef;
import model.ConsultationEv;
import model.Ration;
import model.targetAdjust;

public class NutrientRef{



private Reflevel relativekind; 

private boolean present;

private float quantity;
private String unit=""; 
private UnitReqEnum UnitReq;
private BiblioRef Biblio;
private boolean dis=false;
private String nameRef="";
private float totalQuant=0;



private String name="";



public NutrientRef( String Name,  Reflevel r, float q, String unit , UnitReqEnum Ure, BiblioRef bib, boolean dis, String NameRef, float BEE, float BW, float MW) {



this.quantity= q;
this.unit=unit;
this.name=Name;
	this.relativekind=r;
	this.UnitReq=Ure;
	this.Biblio=bib;
	this.dis=dis;
	this.nameRef=NameRef;
	
	switch (UnitReq) {
	case KGBW:
		totalQuant=BW*quantity;
		break;
	case KGMW:
		totalQuant=MW*quantity;
		break;
	case MCAL:
		totalQuant=BEE*quantity/1000;
		break;
	case NO:
		totalQuant=quantity;
		break;
	default:
		totalQuant=quantity;
		break;
	}
}

public BiblioRef getBiblio() {
	return Biblio;
}


public float getQuantity() {
	return quantity;
}
public Reflevel getRelativekind() {
	return relativekind;
}
public UnitReqEnum getUnitReq() {
	return UnitReq;
}
public String getNameRef() {
	return nameRef;
}
public boolean isDisease() {
	return dis;
}
public float getTotalQuant() {
	return totalQuant;
}
public String getName() {
	return name;
}
public ReturnRefTrigger Trigger(float value ) {

	switch (relativekind) {
	case MAX:
		return (value>totalQuant)?(dis?ReturnRefTrigger.DISUP:ReturnRefTrigger.REFUP):ReturnRefTrigger.NORM;
	case MIN:
	
		return (value<totalQuant)?(dis?ReturnRefTrigger.DISDOWN:ReturnRefTrigger.REFDOWN):ReturnRefTrigger.NORM;
		
	case OPTIMAX:
		return (value>totalQuant)?ReturnRefTrigger.REFOPTIUP:ReturnRefTrigger.NORM;
	case OPTIMIN:
		return (value<totalQuant)?ReturnRefTrigger.REFOPTIDOWN:ReturnRefTrigger.NORM;
	default:
		return ReturnRefTrigger.NORM;
	
	}
	
}

public static NutrientRef getNutrientRef(ObservableList<NutrientRef>ol, Reflevel rl ) {
	
	for (NutrientRef nr:ol) {
		
		if (!nr.isDisease()&rl.equals(nr.getRelativekind())) {
			return nr; 
		}
	}
	return null;
}
public static ObservableList<NutrientRef> getNutrientRefDis(ObservableList<NutrientRef>ol ) {
	ObservableList<NutrientRef> res=FXCollections.observableArrayList();
	for (NutrientRef nr:ol) {
		if (nr.isDisease()) {
			res.add(nr);
		}
	}
	return res;
}
}


package DataStruct;


import java.util.Date;

import Enumerise.AAEnum;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.UnitEnum;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class NutrientP{

private SimpleStringProperty Nom;

private SimpleIntegerProperty kind; 

private MainNutrientEnum MNE;
private boolean present;

private SimpleStringProperty quantity;
private SimpleObjectProperty<UnitP> Unit ;
private UnitP UnitMain ;




public NutrientP( MainNutrientEnum mne, String Name, int k,String q,boolean p, UnitP unit ) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);
this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(unit);
this.UnitMain=(unit);
}

public NutrientP( MainNutrientEnum mne, String Name, int k, String q, boolean p,UnitEnum unit  ) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);
this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(new UnitP(unit));
this.UnitMain=new UnitP(unit);

}

public void setQuantity(String quantity) {
	this.quantity.set(quantity);;
}


public void setNom(SimpleStringProperty nom) {
	Nom = nom;
}
public void setNom(String nom) {
	Nom.set(nom);
}
public SimpleStringProperty getNomProp() {
	return Nom;
}
public String getNom() {
	return Nom.get();
}


public String getQuantity() {
	return quantity.get();
}

public SimpleStringProperty getQuantyProp() {
	return quantity;
	
}
 public void setKind(SimpleIntegerProperty kind) {
	this.kind = kind;
}
 public SimpleIntegerProperty getKind() {
	return kind;
}
 public UnitP getUnit() {
	return Unit.get();
}
 public float getConverter() {
	 float res =Unit.get().getUnit().getConv()/  UnitMain.getUnit().getConv() ;
		
	 return res ;

	 
 }
 public void setMNE(MainNutrientEnum mNE) {
	MNE = mNE;
}
 public MainNutrientEnum getMNE() {
	return MNE;
}
 
 public boolean isPresent() {
	return present;
}
 public void setPresent(boolean present) {
	this.present = present;
}
 public void setUnit(UnitP unit) {
	Unit = new SimpleObjectProperty<UnitP>(unit);
}
public Nutrient getNutrientEnum() {
	switch (MNE) {
	case AMA:
		return AAEnum.getByCoef(kind.get());
		
	case ANA:
		return NutrientAnalysis.getByCoef(kind.get());
		
	case BASE:
		return NutrientBase.getByCoef(kind.get());
		
	
		case LIPID:
			return NutrientLipid.getByCoef(kind.get());
	
	case MACRO:
		return NutrientMacro.getByCoef(kind.get());
	
	case MIN:
		return NutrientMin.getByCoef(kind.get());
	

	case OTHER:
		return NutrientOther.getByCoef(kind.get());
	
	case VITAM:
		return NutrientVitam.getByCoef(kind.get());
		
	default:
		return null;

	
	}
}
 

}
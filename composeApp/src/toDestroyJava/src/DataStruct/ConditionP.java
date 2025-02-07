package DataStruct;


import java.util.Date;

import Enumerise.MainNutrientEnum;
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

public class ConditionP{

private SimpleStringProperty Nom;

private SimpleIntegerProperty kind; 

private MainNutrientEnum MNE;
private boolean present;

private SimpleStringProperty quantity;
private SimpleObjectProperty<UnitP> Unit ;




public ConditionP( MainNutrientEnum mne, String Name, int k,String q,boolean p, UnitP unit  ) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);
this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(unit);
	
}

public ConditionP( MainNutrientEnum mne, String Name, int k, String q, boolean p,UnitEnum unit  ) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);
this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(new UnitP(unit));
	
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

}
package DataStruct;


import java.util.Date;

import Enumerise.MainNutrientEnum;
import Enumerise.Reflevel;
import Enumerise.UnitEnum;
import Enumerise.UnitReqEnum;
import application.GFun;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.AlimentRation;
import model.AlimentUnif;
import model.BiblioRef;
import model.ConsultationEv;
import model.Ration;
import model.targetAdjust;

public class NutrientRefP{

private SimpleStringProperty Nom;

private SimpleIntegerProperty kind; 
private SimpleIntegerProperty relativekind; 

private MainNutrientEnum MNE;
private boolean present;

private SimpleStringProperty quantity;
private SimpleObjectProperty<UnitP> Unit ;
private UnitP unitm;
private SimpleObjectProperty<UnitReqEnum>UnitReq;
private SimpleObjectProperty<BiblioRef>Biblio;
private boolean dis=false;
private Reflevel rl;





public NutrientRefP( MainNutrientEnum mne, String Name, int k,int r, String q,boolean p, UnitP unit , UnitReqEnum Ure, BiblioRef bib) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);

this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(unit);
unitm=unit;
	this.relativekind=new SimpleIntegerProperty(r);
	this.UnitReq=new SimpleObjectProperty<UnitReqEnum> (Ure);
	this.Biblio=new SimpleObjectProperty<BiblioRef>(bib);
}

public NutrientRefP( MainNutrientEnum mne, String Name, int k,  int r, String q, boolean p,UnitEnum unit  , UnitReqEnum Ure, BiblioRef bib) {

	this.Nom=new SimpleStringProperty(Name);
this.MNE=mne;
this.kind=new SimpleIntegerProperty(k);
this.quantity= new SimpleStringProperty(q);
this.present=p;
this.Unit=new SimpleObjectProperty<UnitP>(new UnitP(unit));
unitm=new UnitP(unit);
this.relativekind=new SimpleIntegerProperty(r);
this.UnitReq=new SimpleObjectProperty<UnitReqEnum> (Ure);
this.Biblio=new SimpleObjectProperty<BiblioRef>(bib);
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
 public int getRelativekind() {
	return relativekind.get();
}
 
 public UnitReqEnum getUnitReq() {
	return UnitReq.get();
}
 public void setRelativekind(int relativekind) {
	this.relativekind.set( relativekind);
}
 public void setUnitReq(UnitReqEnum unitReq) {
	UnitReq.set(unitReq);;
}
 public BiblioRef getBiblio() {
	return Biblio.get();
}
 public float getQuantityConverted(){
	 float value=Float.parseFloat(GFun.noPoint(quantity.get()));
	 value=value*getConverter();
	 
	 return value;
 }
 public float getConverter() {
	 if(Unit.getValue().getUnit().equals(UnitEnum.NO)) {
		 return 1;
	 }
	 float res =Unit.get().getUnit().getConv()/  unitm.getUnit().getConv() ;
		
	 return res ;

	 
 }
 public void setBiblio(BiblioRef biblio) {
	Biblio.set(biblio);
}
 
}
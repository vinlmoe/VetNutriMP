package DataStruct;


import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import Enumerise.NutrientLipid;
import application.DataConnector;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.AlimentEv;
import model.AlimentRation;
import model.AlimentUnif;
import model.ConsultationEv;
import model.Espece;
import model.Ration;
import model.targetAdjust;

public class AlimP{
private int id=0;
private SimpleStringProperty Nom;
private SimpleStringProperty Brand;
private SimpleStringProperty Range;
private String eqHash="";
private Map<String, Float> map = new HashMap<String, Float>();
private AlimentEv alim=null;
private AlimentRation ar=null;
private SimpleFloatProperty quantity;
private SimpleObjectProperty<TargetP> adjustOn ;
private float DE=0;

private String UUID;


public AlimP(AlimentRation rat) {

this.ar=rat;
	this.Nom=new SimpleStringProperty(rat.getNom());
this.Brand=new SimpleStringProperty(rat.getFamillyBrand());
this.Range=new SimpleStringProperty("");
this.quantity= new SimpleFloatProperty(rat.getQuantite());
this.adjustOn=new SimpleObjectProperty<TargetP>(new TargetP(rat.getTarget()));
	this.UUID=rat.getUUID();
}
public AlimP(String UUID, String nom, String brand, String range, float quant ) {

	this.Nom=new SimpleStringProperty(nom);
this.Brand=new SimpleStringProperty(brand);
this.Range=new SimpleStringProperty(range);
this.quantity= new SimpleFloatProperty(quant);
this.adjustOn=new SimpleObjectProperty<TargetP>(new TargetP(targetAdjust.CALCIUM));
	this.UUID=UUID;
}
public AlimP(AlimentEv rat) {
this.ar=new AlimentRation (rat);
	this.Nom=new SimpleStringProperty(rat.getNom());
this.Brand=new SimpleStringProperty(rat.getFamillyBrand());
this.Range=new SimpleStringProperty(rat.getGamme());
this.quantity= new SimpleFloatProperty(0.0F);
this.adjustOn=new SimpleObjectProperty<TargetP>(new TargetP(targetAdjust.CALCIUM));
	this.UUID=rat.getUUID();
}
public void setQuantity(float quantity) {

	this.quantity.set(quantity);;
}

public  float getDE (String i){

return map.get(i);
}
public  float getDE (){
	return  DE;
}
public  boolean isDE (String i){
	
	return  map.containsKey(i);
}
public void setRange(SimpleStringProperty range) {
	Range = range;
}
public void setRange(String range) {
	Range.set(range);;
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
public void setAdjustOn(TargetP targ ) {
	this.adjustOn.set(targ);
}
public TargetP getAdjustOn() {
	return adjustOn.get();
}
public String getUUID() {
	return UUID;
}
public float getQuantity() {
	return quantity.get();
}

public void setDE(float dE, String EquaHash) {
	eqHash=EquaHash;
	DE = dE;
	   map.put(EquaHash, dE);
}


public SimpleFloatProperty getQuantyProp() {
	return quantity;
}

public AlimentEv getAlim() {
	if (ar==null) {
		ar=new AlimentRation(DataConnector.readAlim(UUID, null));
	return ar.getAlim();
}else {
	return  ar.getAlim();
}
	}
public String getBrand() {
	return Brand.get();
}
public String getRange(){
	return Range.get();
}
public AlimentRation getAlimR() {
	return ar;
}
public void setAlim(AlimentEv alimU) {
	this.ar = new AlimentRation(alimU);
	map = new HashMap<String, Float>();
	
}public void setBrand(String brand) {
	Brand.set(brand);
}
public int getId() {
	return id;
}
public void setId(int id) {
	this.id = id;
}

public AlimentEv toAlimentEv() {
    AlimentEv aliment = new AlimentEv();
    aliment.setUUID(this.getUUID());
    aliment.setNom(this.getNom());
    // ... autres conversions de propriétés ...
    return aliment;
}

}
package DataStruct;

import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.ConsultationEv;
import model.Ration;

public class RationP{

private SimpleStringProperty Nom;
private SimpleFloatProperty coef;
private SimpleBooleanProperty proposed;


private String UUID;
private ConsultationEv cons;

public RationP(Ration rat) {

	this.Nom=new SimpleStringProperty(rat.getNom());
	this.coef=new SimpleFloatProperty(rat.getCoef());
	this.proposed=new SimpleBooleanProperty(rat.isActual());
	this.UUID=rat.getUUID();
}
public void setCoef(float coef) {
	this.coef.set(coef);;
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
	if (Nom==null) {
		Nom=new SimpleStringProperty("");
	}
	return Nom.get();
}
public void setProposed(boolean proposed) {
	this.proposed.set(proposed);;
}
public String getUUID() {
	return UUID;
}
public float getCoef() {
	return coef.get();
}

public SimpleFloatProperty getCoefProp() {
	return coef;
	
}
public SimpleBooleanProperty getProposedProp() {
	return proposed;
	
}


public boolean getProposed() {
	return proposed.get();
}}
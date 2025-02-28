package DataStruct;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.AlimentRation;
import model.ConsultationEv;
import model.Ration;
import model.WeightDate;

public class CurveParamP implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	private double max;
	private double half;
	private double slope;
	private String UUID; 
	private String name;
	

	public CurveParamP(String name,float desc, float c, float group, String uuid ) {
		this.name=name;
		this.max=(c);
		this.half= (desc);
this.slope=group;
this.UUID=uuid;
	}
	
	public CurveParamP( String name, double c,double desc, double group) {
	this.name=name;
		this.max=(c);
		this.half= (desc);
		this.slope=group;
		this.UUID=java.util.UUID.randomUUID().toString();
		
	}

public double getMax() {
	return max;
}
public double getHalf() {
	return half;
}
public double getSlope() {
	return slope;
}
public void setMax(float coef) {
	this.max = coef;
}
public void setHalf(float description) {
	this.half = description;
}

public String getUUID() {
	return UUID;
}

public String getName() {
	return name;
}
@Override
public String toString() {
return UUID;
		}
public CurveParamP clone() {
	CurveParamP o = null;
	try {
		// On r??cup??re l'instance ?? renvoyer par l'appel de la 
		// m??thode super.clone()
		o=(CurveParamP) super.clone();
		
		
	} catch(CloneNotSupportedException cnse) {
		// Ne devrait jamais arriver car nous impl??mentons 
		// l'interface Cloneable
		cnse.printStackTrace(System.err);
	}
	// on renvoie le clone
	return o;
}

}


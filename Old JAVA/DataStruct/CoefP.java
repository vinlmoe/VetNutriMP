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

public class CoefP implements Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;
	private float coef;
	private String description;
	private int GroupUUID;
	private String UUID; 
	

	public CoefP(String desc, float c, int group, String uuid ) {
		
		this.coef=(c);
		this.description= (desc);
this.GroupUUID=group;
this.UUID=uuid;
	}
	
	public CoefP(String desc, float c, int group) {
	
		this.coef=(c);
		this.description= (desc);
		this.GroupUUID=group;
		this.UUID=java.util.UUID.randomUUID().toString();
		
	}

public float getCoef() {
	return coef;
}
public String getDescription() {
	return description;
}
public int getGroupUUID() {
	return GroupUUID;
}
public void setCoef(float coef) {
	this.coef = coef;
}
public void setDescription(String description) {
	this.description = description;
}

public String getUUID() {
	return UUID;
}
@Override
public String toString() {
return description;
		}
public CoefP clone() {
	CoefP o = null;
	try {
		// On r??cup??re l'instance ?? renvoyer par l'appel de la 
		// m??thode super.clone()
		o=(CoefP) super.clone();
		
		
	} catch(CloneNotSupportedException cnse) {
		// Ne devrait jamais arriver car nous impl??mentons 
		// l'interface Cloneable
		cnse.printStackTrace(System.err);
	}
	// on renvoie le clone
	return o;
}

}


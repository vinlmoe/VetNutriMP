package DataStruct;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import Enumerise.VariableKind;
import equation.Equation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.AlimentRation;
import model.ConsultationEv;
import model.Ration;
import model.WeightDate;

public class SupplementalvariableP implements Cloneable, Serializable {
	
	private String name;
	private VariableKind variable;
	private float value;

	


	
	public SupplementalvariableP( VariableKind eq) {
		this.name=
(eq.getName());
		this.variable= (eq);
this.value=(0);
		
	}

public VariableKind getVariable() {
	return variable;
}

public String getName() {
	return name;
}
public float getValue() {
	return value;
}
public void setValue(float value) {
	this.value=(value);
}
public void setVariable(VariableKind eq, float val) {
	this.name=(eq.getName());
	this.variable= (eq);
	this.value=(val);
}

public SupplementalvariableP clone() {
	SupplementalvariableP o = null;
	try {
		// On r??cup??re l'instance ?? renvoyer par l'appel de la 
		// m??thode super.clone()
		o =(SupplementalvariableP) super.clone();
	
	} catch(CloneNotSupportedException cnse) {
		// Ne devrait jamais arriver car nous impl??mentons 
		// l'interface Cloneable
		cnse.printStackTrace(System.err);
	}
	// on renvoie le clone
	return o;
}

}


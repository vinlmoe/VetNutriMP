package DataStruct;

import java.time.LocalDate;
import java.util.Date;

import equation.Equation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.ConsultationEv;
import model.WeightDate;

public class EquationConsP {
	
	private SimpleStringProperty name;
	private SimpleObjectProperty<Equation> equation;
	private SimpleStringProperty description;
	private SimpleStringProperty especeStr;
	private SimpleStringProperty ref;
	


	
	public EquationConsP( Equation eq) {
		this.name=new SimpleStringProperty(eq.getName());
		this.equation= new SimpleObjectProperty<Equation>(eq);
		this.description=new SimpleStringProperty(eq.getDescription());
		this.especeStr=new SimpleStringProperty(eq.getSpecie().getName());
		this.ref=new SimpleStringProperty(eq.getBib().toString());
	}

public Equation getEquation() {
	return equation.get();
}
public String getDescription() {
	return description.get();
}
public String getEspeceStr() {
	return equation.get().getSpecie().getName();
} 
public String getName() {
	return equation.get().getName();
}
public String getRef() {
	return equation.get().getBib().toString();
}public void setEquation(Equation eq) {
	this.name=new SimpleStringProperty(eq.getName());
	this.equation= new SimpleObjectProperty<Equation>(eq);
	this.description=new SimpleStringProperty(eq.getDescription());
	this.especeStr=new SimpleStringProperty(eq.getSpecie().getName());
	this.ref=new SimpleStringProperty(eq.getBib().toString());
}
@Override
public String toString() {
    return name.get();
}
	

}


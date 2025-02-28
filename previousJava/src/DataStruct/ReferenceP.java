package DataStruct;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.ReferenceEv;


public class ReferenceP {
	
	private SimpleStringProperty name;
	private SimpleObjectProperty<ReferenceEv> reference;
	private SimpleStringProperty description;
	private SimpleStringProperty especeStr;
	private SimpleStringProperty disease;
	
	


	
	public ReferenceP(ReferenceEv eq) {
		this.name=new SimpleStringProperty(eq.getName());
		this.reference= new SimpleObjectProperty<ReferenceEv>(eq);
		this.description=new SimpleStringProperty(eq.getDescription());
		this.especeStr=new SimpleStringProperty(eq.getSpecies());
		this.disease=new SimpleStringProperty(eq.getNameDisease());
	}
	public void Update(ReferenceEv eq) {
		this.name=new SimpleStringProperty(eq.getName());
		this.reference= new SimpleObjectProperty<ReferenceEv>(eq);
		this.description=new SimpleStringProperty(eq.getDescription());
		this.especeStr=new SimpleStringProperty(eq.getSpecies());
		this.disease=new SimpleStringProperty(eq.getNameDisease());
	}

public ReferenceEv getReference() {
	return reference.get();
}
public String getDescription() {
	return description.get();
}
public String getEspeceStr() {
	return especeStr.get();
} 
public String getName() {
	return name.get();
}
public String getDisease() {
	return disease.get();
}public void setEquation(ReferenceEv eq) {
	this.name=new SimpleStringProperty(eq.getName());
	this.reference= new SimpleObjectProperty<ReferenceEv>(eq);
	this.description=new SimpleStringProperty(eq.getDescription());
	this.especeStr=new SimpleStringProperty(eq.getSpecies());
	this.disease=new SimpleStringProperty(eq.getNameDisease());
}

	
@Override
public String toString() {
return name.get();
		}

}


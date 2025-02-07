package DataStruct;



import java.util.ResourceBundle;

import application.DataConnector;
import javafx.beans.property.SimpleStringProperty;

import model.AnimalEv;
import model.ConsultationEv;
import model.Espece;


public class AnimP{

private SimpleStringProperty Nom;
private SimpleStringProperty Breed;
private SimpleStringProperty Specie;
private SimpleStringProperty Owner;
private SimpleStringProperty ID;
private String SpUUID;




private String UUID;



public AnimP(String UUID, String nom, String breed, String specie, String owner, String id,String  spUUID) {
	// TODO Auto-generated constructor stub
	
	this.UUID=UUID;
	this.Nom=new SimpleStringProperty(nom);
	this.Breed=new SimpleStringProperty(breed);
	System.out.println(specie);
	this.Specie=new SimpleStringProperty(Espece.getEnumFromStringId(spUUID).getName());
	this.Owner=new SimpleStringProperty(owner);
	this.ID=new SimpleStringProperty(id);
	this.SpUUID=spUUID;
}


public String getBreed() {
	return this.Breed.get();
}
public String getSpecie() {
	return this.Specie.get();
}
public String getOwner() {
	return this.Owner.get();
}
public String getNom() {
	return this.Nom.get();
}
public String getID(){
	return this.ID.get();
}
public String getUUID() {
	return UUID;
}
public AnimalEv getAnial() {
	return DataConnector.readAnimal(UUID);
}
public boolean isSpecie(String sp) {

	if(sp=="ALL") {
		return true;
	}
	return (SpUUID.equals(sp));
}
}
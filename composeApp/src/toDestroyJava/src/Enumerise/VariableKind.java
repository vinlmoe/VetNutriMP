package Enumerise;

import DataStruct.CoefP;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AlimIndic;

public enum VariableKind {
AdultWeight("AdultWeight",0, "AW"),
LitterSize("LitterSize",1, "L"),
WeekGestation("WeekGestation",2, "wG"),
WeekLactation("WeekLactation",3, "wL")
	;
	
private String var ="";
private String name ="";
private int uuid=0;
VariableKind(String name, int i, String var){
	this.name=name;
	this.uuid=i;	
	this.var=var;
}

public String getVariable() {
	return var;
}
public String getName() {
	return name;
}
public int getUuid() {
	return uuid;
}

public static VariableKind getById (int i) {
	
	for (VariableKind ek:VariableKind.values()) {
		if (ek.getUuid()==i) {
			return ek;
		}
	}
	return VariableKind.AdultWeight;
}
 
}

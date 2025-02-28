package Enumerise;

import DataStruct.CoefP;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.AlimIndic;

public enum EquationKind {
	ENERGYNEED("energyNeedDesc", "energyNeed", 0),
	ENERGYDENSITY("energyDensityDesc", "energyDensity", 1),
	MW("metabolicWeightDesc", "metabolicWeight", 2),
	INDICATOR("indicatorDesc", "indicator", 3),
	NEED("NeedDesc", "NeedEq", 4)
	
	;
	
private String description ="";
private String name ="";
private int uuid=0;
EquationKind(String desc, String name, int i){
	this.description=desc;
	this.name=name;
	this.uuid=i;	
}

public String getDescription() {
	return description;
}
public String getName() {
	return name;
}
public int getUuid() {
	return uuid;
}

public static EquationKind getById (int i) {
	
	for (EquationKind ek:EquationKind.values()) {
		if (ek.getUuid()==i) {
			return ek;
		}
	}
	return EquationKind.ENERGYNEED;
}
 
}

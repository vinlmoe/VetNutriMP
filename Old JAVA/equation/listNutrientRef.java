package equation;

import DataStruct.NutrientRef;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class listNutrientRef {
	
	private ObservableList<NutrientRef> ol=FXCollections.observableArrayList();
	
	public listNutrientRef() {
	
		// TODO Auto-generated constructor stub
	}
	public listNutrientRef(ObservableList<NutrientRef>  nut) {
		ol=FXCollections.observableArrayList();
		for (NutrientRef n:nut) {
			ol.add(n);}
	}
	public void add( ObservableList<NutrientRef>  nut) {
	for (NutrientRef n:nut) {
		ol.add(n);}
	}
	public ObservableList<NutrientRef> get() {
		return ol;
	}

}

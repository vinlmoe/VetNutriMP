package Enumerise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.TypeAlim;

public enum PriceCateg {
	ALL("", "Tous"),
	NO("n", "Not Relevant"),
REG("r", "Online/Petshop Brand"),
IND("i", "Mass Brand"),
VET("v", "Vet Brand");

	private String id="i";
	private String name="GMS";
	PriceCateg(String id, String name) {
		this.id=id;
		this.name=name;
	}
	

public static PriceCateg getById(String i){
	
	return (PriceCateg) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (PriceCateg pageType : PriceCateg.values()) {
        map.put(pageType.id, pageType);
    }
}

public String getId() {
	return id;
}
@Override
public String toString() {
	return name;
}
public static ArrayList<PriceCateg> valuesExcept() {
	
	ArrayList<PriceCateg>es=new ArrayList<PriceCateg>();
	for (PriceCateg e:PriceCateg.values()) {
		if (e!=PriceCateg.ALL) {
			es.add(e);
		}
			
	}
	return es;
}
}

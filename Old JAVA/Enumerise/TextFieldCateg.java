package Enumerise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.TypeAlim;

public enum TextFieldCateg {
	ADVISE(0, "Tous"),
	NOM(1, "Not Relevant"),
RUE(2, "Online/Petshop Brand"),
ZIPCODE(3, "Mass Brand");

	private int id=0;
	private String name="GMS";
	TextFieldCateg(int id, String name) {
		this.id=id;
		this.name=name;
	}
	

public static TextFieldCateg getById(String i){
	
	return (TextFieldCateg) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (TextFieldCateg pageType : TextFieldCateg.values()) {
        map.put(pageType.id, pageType);
    }
}

public int getId() {
	return id;
}
@Override
public String toString() {
	return name;
}
}
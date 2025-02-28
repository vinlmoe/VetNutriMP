package Enumerise;

import java.util.ResourceBundle;

public enum ContEnum {


NO("NO",0),
GEL("Gelule",9),
CAN("Can",2),
SACHET("Sachet",3),
PRESSION("Pression",4),
ML("mL", 5),
COMP("comprim", 6),
BOUCH("Bouch",7),
DOSETTE("Dosette",8)

	;
	
	


	
private String name = "";

private int ID=0;
private int IDFamily=0;
private int refID=0;

private float conv=1.F;
private
 
//Constructeur
ContEnum(String name, int id){

	ID=id;
	this.name = name;

}
 

 
public String nameToString(){
  return name;
}
public float getConv() {
	return conv;
}
public int getID() {
	return ID;
}
public int getIDFamily() {
	return IDFamily;
}
public String getName() {
	return name;
}

public static ContEnum byId(int id) {
	
	for(ContEnum e:ContEnum.values()) {
		if (e.getID()==id) {
			return e;
		}
	}
	return ContEnum.NO;
}
public static ContEnum getByName(String str) {
	
	for(ContEnum e:ContEnum.values()) {
		if (e.getName().equals(str)) {
			return e;
		}
	}
	return ContEnum.NO;
}

public static float getConversionByName(ResourceBundle res, String s) {
	for (ContEnum enu:ContEnum.values()) {
		if (s.equals(res.getString(enu.getName()))) {
			return enu.conv;
		}
	}
	return -2F;
}

}

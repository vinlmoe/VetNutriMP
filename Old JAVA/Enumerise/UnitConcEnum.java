package Enumerise;

import java.util.ResourceBundle;

public enum UnitConcEnum {


BU100g("P100g", 1, 1, 1, 1),
BUkg("Pkg", 2, 1, 1, 0.1F),
Prot("P100gProt", 3, 2, 1, -1F)
	;
	
	


private String name = "";
private int ID=0;
private int IDFamily=0;
private int refID=0;

private float conv=1.F;
private
 
//Constructeur
UnitConcEnum(String name, int id, int idf, int rid, float conv){
  this.name = name;
this.ID=id;
this.IDFamily=idf;
this.refID=rid;
this.conv=conv;
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
public int getRefID() {
	return refID;
}
public UnitConcEnum byId(int id) {
	
	for(UnitConcEnum e:UnitConcEnum.values()) {
		if (e.getID()==id) {
			return e;
		}
	}
	return UnitConcEnum.BU100g;
}
public static UnitConcEnum getByName(String str, int Family) {
	
	for(UnitConcEnum e:UnitConcEnum.values()) {
		if (e.getName().equals(str)&e.getIDFamily()<=Family) {
			return e;
		}
	}
	return UnitConcEnum.BU100g;
}

public static float getConversionByName(ResourceBundle res, String s) {
	for (UnitConcEnum enu:UnitConcEnum.values()) {
		if (s.equals(res.getString(enu.getName()))) {
			return enu.conv;
		}
	}
	return -2F;
}

}

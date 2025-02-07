package Enumerise;

public enum UnitEnum {
	BUg("g", 1, 1, 1, 1),
	BUmg("mg", 2, 1, 1, 0.001F),
	BUmu("µg", 3, 1, 1, 0.000001F),
	AUui("UI", 4, 2, 4, 1),
	AUmu("µg", 5, 2, 4, 3.33F),
	DUui("UI", 6, 3, 6, 1F),
	DUmu("µg", 7, 3, 6, 40F),
	EUui("UI", 8, 4, 6, 1F),
	EUmg("mg", 9, 4, 8, 1F),
	NO("",0,5, 10,0)
	;
	
	


private String name = "";
private int ID=0;
private int IDFamily=0;
private int refID=0;

private float conv=1.F;
private
 
//Constructeur
UnitEnum(String name, int id, int idf, int rid, float conv){
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
public UnitEnum byId(int id) {
	
	for(UnitEnum e:UnitEnum.values()) {
		if (e.getID()==id) {
			return e;
		}
	}
	return UnitEnum.BUg;
}
public static UnitEnum getByName(String str, int Family) {
	
	for(UnitEnum e:UnitEnum.values()) {
		if (e.getName().equals(str)&e.getIDFamily()==Family) {
			return e;
		}
	}
	return UnitEnum.BUg;
}


}

package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Enumerise.UnitReqEnum;

public enum Espece {
	CH("ALL", 2, "ALL"),
	CHIEN("DOG", 0, "0"),
	CHAT("CAT", 1, "1"),
	PRIMATE("PRIMATE", 3, "3"),
	RAT("RAT", 4, "4"),
	SOURIS("SOURIS", 5, "5"),
	FURET("FURET", 6 ,"6"),
	LAPIN("LAPIN", 7, "7"),
	CHEVAL("CHEVAL", 8, "8"),
	FELIN("FELIN", 9, "9"),
	CANIN("LAPIN", 10, "10"),
	HERBIVORE("HERBIVORE", 11, "11"),
	FOLIVORE("FOLIVORE", 12, "12")

	
	
	;


private String name = "";


private int categorie=0;
private String UUID="0";
 
//Constructeur
 Espece(String name, int categrorieo, String UUID){
  this.name = name;

  this.categorie=categrorieo;
  this.UUID=UUID;

}
 
 public String nameToString() {
	return name;
}
 public int getCategorie() {
	return categorie;
}
 public static String getStringFromInt(int id){
	 String str=Espece.CHIEN.nameToString();
	 for (Espece espe:Espece.values()){
		 if (id==espe.getCategorie()){
			 str=espe.nameToString();
		 }
	 }
	 return str;
 }
 public static Espece getEnumFromInt(int id){
	 Espece esp=Espece.CHIEN;
	 for (Espece espe:Espece.values()){
		 if (id==espe.getCategorie()){
			 esp=espe;
		 }
	 }
	 return esp;
 }
 
 public static Espece getEnumFromString(String id){
	 Espece esp=Espece.CHIEN;
	 for (Espece espe:Espece.values()){
		 if (id.equals(espe.nameToString())){
			 esp=espe;
		 }
	 }
	 return esp;
 }
 
 public String getName() {
	return name;
}
public String getUUID() {
	return UUID;
}
public static ArrayList<Espece> valuesExcept() {
	
	ArrayList<Espece>es=new ArrayList<Espece>();
	for (Espece e:Espece.values()) {
		if (e!=Espece.CH) {
			es.add(e);
		}
			
	}
	return es;
}
 
public static Espece getEnumFromStringId(String i){
	
	return (Espece) map.get(i);
}
 private static Map map = new HashMap<>();
static {
    for (Espece pageType : Espece.values()) {
        map.put(pageType.UUID, pageType);
    }
} 


}

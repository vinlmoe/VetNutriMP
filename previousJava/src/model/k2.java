package model;

public enum k2 {
	 
	 LETHARGIE("Inactif (sorties hygi??niques uniquement)", 0.7F),
	  SEDENTAIRE("S??dentaire (<1h/j)",0.8F),
	CALME("Calme (1-2h/j)", 0.9F),
	NORMAL("Normal (3h/j)", 1.0F),
	ACTIF("Actif", 1.1F)

	;


private String name = "";
private float coef = 1.0F;

//Constructeur
k2(String name, float coef){
this.name = name;
this.coef=coef;
}



public String nameToString(){
return name;
}
public float getCoef(){
	  return coef;
	}



}

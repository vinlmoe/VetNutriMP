package model;

public enum k3 {
	ADULTE("Normal", 1.0F),
	AGE("Ag??", 0.9F),
	CASTRE("St??rilis??", 0.8F),
GESTATION("Gestation", 0F),
ALLAITEMENT("Lactation", 3F),
COISSANCE("Croissance", 0F), 
	
	;
	


private String name = "";
private float coef = 1.0F;

//Constructeur bod 
k3(String name, float coef){
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

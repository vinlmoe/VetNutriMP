package model;

public enum PathoEnum {
	NORM("Pas de pathologie particuli??re", 1),
	DIAB("Diab??tique", 2),
	IRC("Insuffisance r??nale chronique", 3),
	STRUV("Cristaux de Struvite", 4),
	OXA("Cristaux d'oxalate", 5)
	
	;
	


private String name = "";
private int coef = 1;

//Constructeur
PathoEnum(String name, int coef){
this.name = name;
this.coef=coef;
}



public String nameToString(){
return name;
}
public int getCoef(){
	  return coef;
	}
}
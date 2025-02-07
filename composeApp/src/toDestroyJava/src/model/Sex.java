package model;

public enum Sex  {
	MALEE("M??le entier", 1F, 0, "MaleEnt"),
	MALEC("M??le castr??",0.8F, 1, "MaleSpray"),
	FEMELLEE("Femelle enti??re", 1F, 2, "FemEnt"),
	FEMELLE("Femelle st??rilis??e",0.8F, 3, "FemSpray")
	;
	


private String name = "";
private float coef = 1.0F;
private int ID=0;
private String label="";

//Constructeur
Sex(String name, float coef, int ID, String lab){
this.name = name;
this.coef=coef;
this.ID=ID;
this.label=lab;
}

public String getLabel() {
	return label;
}

public String nameToString(){
return name;
}
public float getCoef(){
	  return coef;
	}
public int getID() {
	return ID;
}
public static Sex byID(int id) {
	Sex s=Sex.MALEE;
	for (Sex t:Sex.values()) {
		if (t.getID()==id) {
			return t;
		}
		
	}
	return s;
}
}

package model;



public enum GesCoef {
	 
	 ER("1", 1.0F,1.0F, 1),
	 DEUX("2", 1.0F,1.05F, 2),
	 TROIS("3", 1.0F,1.1F,3),
	 QUATRE("4", 1.0F,1.15F,4),
	 CINQ("5", 1.1F,1.2F,5),
	 SIX("6", 1.2F, 1.25F,6),
	 SEPT("7", 1.3F, 1.3F,7),
	 OCTO("8", 1.4F,1.4F, 8),
	 NOVO("9", 1.5F,1.5F, 9)

	;


private String name = "";
private float coef = 1.0F;
private float fcoef = 1.0F;
private int id;

//Constructeur
GesCoef(String name, float coef){
this.name = name;
this.coef=coef;
}
GesCoef(String name, float coef, float fcoef, int id){
this.name = name;
this.coef=coef;
this.fcoef=fcoef;
this.id=id;
}


public String nameToString(){
return name;
}
public float getCoef(){
	  return coef;
	}

public float getFCoef(){
	  return fcoef;
	}
 public int getId() {
	return id;
}
public static GesCoef byId(int i ) {
	GesCoef s=GesCoef.ER;
	for (GesCoef a:GesCoef.values()) {
		if (a.getId()==i) {
			return a;
		}
	}
	return s;
}

}

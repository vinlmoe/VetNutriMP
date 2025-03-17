package model;



public enum LactCoef {
	 
	 ER("1", 0.75F,0.9F,1),
	 DEUX("2", 0.95F,0.9F,2),
	 TROIS("3", 1.1F,1.2F, 3),
	 QUATRE("4", 1.2F,1.2F, 4),
	 CINQ("5", 0F,1.1F, 5),
	 SIX("6", 0F, 1.0F, 6),
	 SEPT("7", 0F, 0.8F, 7)


	;


private String name = "";
private float coef = 1.0F;
private float fcoef = 1.0F;
private int id=1;

//Constructeur
LactCoef(String name, float coef){
this.name = name;
this.coef=coef;
}
LactCoef(String name, float coef, float fcoef, int id){
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
public static float GetCoefPlus(String er){
	float response=1F;
	for (LactCoef lc:LactCoef.values()) {
		if(lc.nameToString().equals(er)) {
			response=lc.getCoef();
		}
	}
	return response;
}
public static float GetFCoefPlus(String er){
	float response=1F;
	for (LactCoef lc:LactCoef.values()) {
		if(lc.nameToString().equals(er)) {
			response=lc.getFCoef();
		}
	}
	return response;
}

public static LactCoef byId(int i ) {
	LactCoef s=LactCoef.ER;
	for (LactCoef a:LactCoef.values()) {
		if (a.getId()==i) {
			return a;
		}
	}
	return s;
}
public int getId() {
	return id;
}

}

package model;

public enum k1 {
	 NORDIQUE("Race Nordique", 0.8F),
	  RETRIEVER("Retriever",0.8F),
	TERRE_NEUVE("Terre neuve", 0.8F),
	BEAGLE("Beagle", 0.9F),
	COCKER("Cocker", 0.9F),
	LEVRIER("L??vrier", 1.1F),
	DOGUE_ARGENTIN("Dogue argentin", 1.15F),
	DANOIS("Danois", 1.15F)

	;
	


private String name = "";
private float coef = 1.0F;
 
//Constructeur
k1(String name, float coef){
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

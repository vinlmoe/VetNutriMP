package Enumerise;

public enum TypeCalc {
	SIMPLE("Simple", "Energie calcul?? avec les coefficients 3.5/8.5/3.5"),
	HAUT("Hautement digestible", "Tous les aliments sont consid??r??s comme hautement digestibles: 4/9/4"),
	COMPLEX("Hautement digestible", "Tous les aliments sont consid??r??s comme hautement digestibles: 4/9/4")
	;
	


private String name = "";
private String desc= "";

//Constructeur
TypeCalc(String name, String sesc){
 this.name = name;
this.desc=desc;
}



public String nameToString(){
 return name;
}

}

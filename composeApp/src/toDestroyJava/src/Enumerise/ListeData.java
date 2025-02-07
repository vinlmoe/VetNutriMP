package Enumerise;



public enum ListeData{
	  //Objets directement construits
	  CIQUAL("Aliments Ciqual", "resources/aliments.vbr"),
	 
	HILLS("Aliments complets","resources/alimentsHills.vbr"),
	CMV("Aliments Compl??mentaires","resources/comp.vbr"),
	GAL("Aliments Base GAL","resources/gal.vbr");

private String name = "";
private String namefile = "";
 
//Constructeur
ListeData(String name, String namefile){
  this.name = name;
this.namefile=namefile;
}
 

 
public String nameToString(){
  return name;
}
public String nameFileToString(){
	  return namefile;
	}
 
}
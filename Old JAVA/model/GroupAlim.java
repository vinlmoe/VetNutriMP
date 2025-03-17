package model;

import java.util.ArrayList;

public enum GroupAlim {
	ALL("Tous", 1,0, targetAdjust.NO,999),
ABATS("Abats", 1,0, targetAdjust.PROT,0),
AIDE("Aides culinaires", 10,0, targetAdjust.NO,1),
ALGUES("Algues", 5,0, targetAdjust.NO,2),
AROMATEf("Aromates frais",5,0, targetAdjust.NO,3),
AROMATEs("Aromates séchés",5,0, targetAdjust.NO,4),
AutreCereal("Autres céréales",3,0, targetAdjust.ENERGIE,5),
AutrePOISSON("Autres produits à base de poisson",1,0, targetAdjust.PROT,6),
ALIMenf("Aliments infantiles",10,0, targetAdjust.ENERGIE,7),
MGAnim("Autres matières grasses animales", 4,0, targetAdjust.LIP,8),
MGLait("Beurres et matières grasses laitières",4,0, targetAdjust.LIP,9),
MGAutre("Margarines et matières grasses composées",4,0, targetAdjust.LIP,10),
BISCOTTE("Biscottes et pains non levés", 3,0, targetAdjust.ENERGIE,11),
BISCUITSAL("Biscuits salés apéritifs", 3,0, targetAdjust.ENERGIE,12),
BISCUITScu("Biscuits secs sucrés",3,0, targetAdjust.ENERGIE,13),
BOUILLON("Bouillons préts à consommer", 3,0, targetAdjust.ENERGIE,14),
CEREALPD("Céréales petit déjeuner et barres céréalières",3,0, targetAdjust.ENERGIE,15),
CHARCUT("Charcuteries et salaisons", 1,0, targetAdjust.PROT,16),
COMPLEMENT("Compléments alimentaires",5,0, targetAdjust.CALCIUMPHOS,17),
CREMES("Crèmes et spécialités à base de créme", 1,0, targetAdjust.PROT,18),
CRUSTACE("Crustacés et mollusques",1,0, targetAdjust.PROT,19),
DESSERTLAIT("Desserts lactés", 1,0, targetAdjust.PROT,20),
DESSERT("Desserts Autres",3,0, targetAdjust.ENERGIE,21),
EAUX("Eaux", 6,0, targetAdjust.NO,22),
EPICES("Epices",10,0, targetAdjust.NO,23),
FRUITSIRP("Fruits au sirop/au jus, compotes", 2,0, targetAdjust.NO,24),
FRUITFRAIS("Fruits", 2,0, targetAdjust.NO,25),
FRUITCOQUE("Fruits à coques et graines oléagineuses",4,0, targetAdjust.NO,26),
JUS("Jus", 10,0, targetAdjust.NO,27),
AutreFRUITS("Autres produits transformés à base de fruits",2,0, targetAdjust.NO,28),
FarinesAmidon("Farines et amidons",3,0, targetAdjust.ENERGIE,29),
PAINS("Pains",3,0, targetAdjust.ENERGIE,30),
VIENNOISERIE("Viennoiseries et brioches",3,0, targetAdjust.ENERGIE,31),
GATEAUX("Gateaux et patisseries",3,0, targetAdjust.ENERGIE,32),
CAKES("Feuilletés et cakes salés",3,0, targetAdjust.ENERGIE,33),
PATETARTE("Pâtes à tarte, à pizza, etc.",3,0, targetAdjust.ENERGIE,34),
FROMAGES("Fromages", 1,0, targetAdjust.PROT,35),
FRUITSEC("Fruits séchés ou lyophilisés", 2,0, targetAdjust.NO,36),
EPICE("Herbes, épices et assaisonnements",5,0, targetAdjust.NO,37),
HUILES("Huiles et graisses végétales", 4,0, targetAdjust.LIP,38),
LAITS("Laits", 1,0, targetAdjust.PROT,39),
LEGUMES("Légumes",2,0, targetAdjust.FIBER,40),
DIVERS("Ingrédients divers",10,0, targetAdjust.NO,41),
LEGUMESSEC("Légumes secs", 2,0, targetAdjust.FIBER,42),
OEUF("Oeufs et dérivés",1,0, targetAdjust.PROT,43),
HUILEPOISSON("Huiles de poissons",4,0, targetAdjust.EPA,44),
POISSON("Poissons et batraciens",1,0, targetAdjust.PROT,45),
POMMETERRE("Pommes de terre et apparentés", 3,0, targetAdjust.ENERGIE,46),
PATES("Pâtes et semoules", 3,0, targetAdjust.ENERGIE,47),
SAUCES("Sauces et condiments",10,0, targetAdjust.NO,48),
SELS("Sels",5,0, targetAdjust.NA,49),
POISSONPROD("Produits à base de poissons", 1,0, targetAdjust.PROT,50),
RIZ("Riz et autres graines",3,0, targetAdjust.ENERGIE,51),
SALADE("Salades composées et crudités",2,0, targetAdjust.FIBER,52),
PLAT("Plats composés",10,0, targetAdjust.NO,53),
SANDWICH("Sandwichs",10,0, targetAdjust.NO,54),
SOUPE("Soupes, Bouillons",10,0, targetAdjust.NO, 55),
SUCRE("Sucres, miels, sirops, confiseries", 5,0, targetAdjust.ENERGIE,56),
VIANDES("Viandes", 1,0, targetAdjust.PROT,57),
VOLLAILLE("Volailles", 1,0, targetAdjust.PROT,58),
YAOURT("Yaourts et spécialités laitiéres type yaourts",1,0, targetAdjust.PROT,59),
AUTRES("Autres",10,0, targetAdjust.NO,60),
FLAIT("Produits laitiers et d'oeufs",1,1, targetAdjust.PROT,61),
FARO("Épices et fines herbes",5,1, targetAdjust.NO,62),
FBEBE("Aliments pour bébés",10,1, targetAdjust.NO,63),
FHUILE("Matières grasses et huiles",4,1, targetAdjust.LIP,64),
FVOL("Produits de volaille",1,1, targetAdjust.PROT,65),
FPOT("Potages et sauces",2,1, targetAdjust.LIP,66),
FSAUCI("Saucisses et viandes froides",1,1, targetAdjust.PROT,67),
FCERE("Céréales à déjeuner",3,1, targetAdjust.ENERGIE,68),
FFRUIT("Fruits et jus de fruits",2,1, targetAdjust.FIBER,69),
FPORC("Produits de porc",1,1, targetAdjust.PROT,70),
FLEG("Légumes et produits végétaux",2,1, targetAdjust.FIBER,71),
FNOIX("Noix et graines",3,1, targetAdjust.NO,72),
FBOEUF("Produits de boeuf",1,1, targetAdjust.PROT,73),
FBOISS("Boissons",6,1, targetAdjust.NO, 74),
FPOISS("Produits de poissons, mollusques et crustacés",1,1, targetAdjust.PROT,75),
FLEGUMUNEUSE("Légumineuses et produits de légumineuses",2,1, targetAdjust.FIBER,76),
FAGNEAU("Ageau, veau et gibier",1,1, targetAdjust.PROT,77),
FBOULAN("Produits de boulangerie",3,1, targetAdjust.ENERGIE,78),
FSUCRE("Sucreries",3,1, targetAdjust.ENERGIE,79),
FPAIN("Céréales, grains et pâtes",3,1, targetAdjust.ENERGIE,80),
FPRET("Aliments préts-à-manger",10,1, targetAdjust.NO, 81),
FCOMP("Mets composés",10,1, targetAdjust.ENERGIE,82),
FGRI("Grignotises",10,1, targetAdjust.ENERGIE,83)


	;


private String name = "";


private int categorie=0;
private int type=0;
private targetAdjust target=targetAdjust.ENERGIE;
private int id=0;

//Constructeur
 GroupAlim(String name, int categrorieo, int type, targetAdjust t, int id){
  this.name = name;
this.id=id;
  this.categorie=categrorieo;
 this.type=type;
 this.target=t;
}
 
 public String nameToString() {
	return name;
}
public int getCategorie() {
	return categorie;
}
public static boolean isPresent(GroupAlim sz){
	boolean r=false;
	for (GroupAlim ga: GroupAlim.values()){
		if (ga.equals(sz)){
			r=true;
		}
		
	}
	return r;
}
 public int getId() {
	return id;
}
 public static GroupAlim setById(int id) {
	 GroupAlim r=GroupAlim.AUTRES;
		for (GroupAlim ga: GroupAlim.values()){
			if (ga.getId()==id){
				return ga;
			}
		}
		return r;
 }
public int getType() {
	return type;
}
public static GroupAlim StringToGroup(String sz){
	GroupAlim r=GroupAlim.AUTRES;
	for (GroupAlim ga: GroupAlim.values()){
		if (ga.nameToString().toLowerCase().equals(sz.toLowerCase())){
			return ga;
		}
		
	}
	return r;
}
public static ArrayList<String> ListOfGroup(int sz){

	 ArrayList<String> list =new  ArrayList<String>();
	for (GroupAlim ga: GroupAlim.values()){
		if (ga.getType()==sz){
			list.add(ga.nameToString());
		}
		
	}
	return list;
}
public static ArrayList<GroupAlim> valuesExcept() {
	
	ArrayList<GroupAlim>es=new ArrayList<GroupAlim>();
	for (GroupAlim e:GroupAlim.values()) {
		if (e!=GroupAlim.ALL) {
			es.add(e);
		}
			
	}
	return es;
}
public targetAdjust getTarget() {
	return target;
}
@Override
public String toString() {
	return name;
}
	
}

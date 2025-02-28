package model;

public enum NEC {
	
		N1("1/5",0, 0,0,0,"","",true,0),
		N2("2/5",10, 0,0,0, "","",true,1),
		N3("3/5", 20, 0,0,0, "","",true,2),
		N4("4/5",30, 0,0,0, "","",true,3),
		N5A("5A/5",40, 0,0,0, "","",true,3),
		N5B("5B/5",50, 0, 0,0,"","",true,4),
		N5C("5C/5",60, 0, 0,0,"","",true,5),
		A1("1/9",0, 0,0,0,"<html><html>Les c??tes, la colonne vert??brale et les os des hanches sont visibles de loin.<p> Aucune graisse corporelle discernable et une perte ??vidente de masse musculaire."
				,"<html>C??tes visibles sur les chats ?? poils courts<p>  pas de graisse palpable<p>  abdomen fortement repli??<p>  vert??bres lombaires et ailes des iliaques ??videntes et facilement palpables"
				,false,6),
		A2("2/9",3.7F, 2.6F, 5F,5F,"<html>Les c??tes, la colonne vert??brale et les os des hanches sont facilement visibles. <p> Pas de graisse corporelle palpable et une perte minimale de masse musculaire."
				,"<html>Caract??ristiques communes des scores 1 et 3."
				,false,7),
		A3("3/9", 8.2F, 8.4F,15F,15F,"<html>Les c??tes sont facilement palpables et peuvent ??tre visibles sans graisse palpable.<p> Le haut de la colonne vert??brale est visible et les os des hanches peuvent ??galement ??tre pro??minents."
				,"<html>Des c??tes facilement palpables avec une couverture de graisse minimale ;<p> vert??bres lombaires ??videntes ;<p> taille ??vidente derri??re les c??tes ;<p> graisse abdominale minimale."
						,false,8),
		A4("4/9",12.7F, 14.1F, 19.8F,19.8F,"<html>Les c??tes peuvent ??tre facilement ressenties avec une couverture de graisse minimale.<p> La taille est facile ?? noter lorsqu'on la regarde de haut.<p> On observe ??galement un \"repli abdominal\", <p> c'est-??-dire que l'abdomen semble repli?? derri??re la cage thoracique lorsqu'on le regarde de c??t??."
				,"<html>Caract??ristiques communes des scores 3 et 5.",false,9),
		A5("5/9",17.2F, 19.9F, 21.8F,28F,"<html>C??tes palpables sans un exc??s de graisse.<p> Taille observ??e derri??re les c??tes lorsqu'il est vu de haut.<p> Abdomen rentr?? lorsqu'il est visionn??."
				,"<html> Bien proportionn?? ;<p> taille observ??e derri??re les c??tes ;<p> c??tes palpables avec une l??g??re couverture de graisse ;<p> coussinet adipeux abdominal minimal.",false,10),
		A6("6/9",21.7F, 25.7F,28.7F,42.8F,"<html>Les c??tes sont perceptibles ?? travers un l??ger exc??s de graisse.<p> La taille est visible de dessus, mais pas pro??minente.<p> La ceinture abdominale est pr??sente."
				,"<html>Caract??ristiques communes des scores 5 et 7.",false,11),
		A7("7/9",26.2F, 31.4F, 36.9F,44.1F,"<html>Les c??tes sont difficiles ?? sentir sous une ??paisse couche de graisse.<p> D??p??ts de graisse visibles sur le bas du dos et la base de la queue.<p> La taille est absente ou ?? peine visible et l'abdomen peut para??tre visiblement arrondi ou affaiss??."
				,"<html>C??tes difficilement palpables avec une couverture adipeuse mod??r??e ;<p> taille peu discernable ;<p> arrondi ??vident de l'abdomen ;<p> coussinet adipeux abdominal mod??r??",false,12),
		A8("8/9",30.7F, 37.2F, 39.2F,47.4F,"<html>Les c??tes ne peuvent ??tre ressenties qu'avec une forte pression.<p> D'importants d??p??ts de graisse sur le bas du dos et la base de la queue.<p> La taille et l'abdomen sont tous deux absents.<p> Une distension abdominale ??vidente peut ??galement ??tre pr??sente."
				,"<html>Caract??ristiques communes des scores 7 et 9.",false,13),
		A9("9/9", 35.1F, 43F,45F,53.4F,"<html><html>Les c??tes ne peuvent pas ??tre senties sous une tr??s lourde couverture de graisse.<p> De gros d??p??ts de graisse sont visibles sur le cou, la poitrine, la colonne vert??brale et la base de la queue.<p> La taille et l'abdomen sont tous deux absents.<p> Une distension abdominale ??vidente et un dos large et plat peuvent ??galement ??tre pr??sents. "
				,"<html>C??tes non palpables sous une forte couverture adipeuse ;<p> importants d??p??ts de graisse sur la r??gion lombaire, le visage et les membres ;<p> distension de l'abdomen sans taille ;<p> vaste coussinet adipeux abdominal.",false,14)
	
		
		;
		

	private String name = "";
	private float coef = 1.0F;
	private float coefF = 1.0F;
	private float coefchat = 1.0F;
	private float coefchatF = 1.0F;
	private String descriptionchien = "";
	private String descriptionchat = "";
	private boolean old= true;
private int id=0;

	//Constructeur
	NEC(String name, float coef,float coefF, float coefc, float coefcf, String desc,String descc, boolean old, int id){
	this.name = name;
	this.coef=coef;
	this.coefF=coefF;
	this.coefchat=coefc;
	this.coefchatF=coefcf;
	this.descriptionchat=descc;
	this.descriptionchien=desc;
	this.old=old;
	this.id=id;
	}



	public String nameToString(){
	return name;
	}
	public float getCoef(){
		  return coef;
		}
	public float getCoefchat() {
		return coefchat;
	}
	public String getDescriptionchat() {
		return descriptionchat;
	}
	public String getDescriptionchien() {
		return descriptionchien;
	}
	public float getCoefchatF() {
		return coefchatF;
	}
	public float getCoefF() {
		return coefF;
	}
	public boolean isOld() {
		return old;
	}
	public int getId() {
		return id;
	}
	public static NEC necById(int id)
	{
		NEC a=NEC.A1;
		for (NEC s:NEC.values()) {
			if (s.getId()==id)return s;
		}
		return a;
	}

	
	public NEC oldToNew(NEC old) {
		NEC New=NEC.A5;
		switch(old) {
		case N1:
			New=NEC.A1;
			break;
		case N2:
			New=NEC.A3;
			break;
		case N3:
			New=NEC.A5;
			break;
		case N4:
			New=NEC.A7;
			break;
		case N5A:
		case N5B:
		case N5C:
			New=NEC.A9;
			break;
		default:
			New=old; 
			break;
		}
		return New;
	}
 public int getNewID() {
	 if (isOld()) {
		 return( oldToNew(this).getId()-6);
	 }else {
		 return( getId()-6);
	 }
 }
	}




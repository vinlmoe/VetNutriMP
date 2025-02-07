package model;
public enum RaceChat {
	A1	("Sib??rien",	1,	100,	1	),

A2	("Snowshoe",	1,	100,	1	),
	A3	("British shorthair",	1,	100,	1	),
	A4	("Minskin",	1,	100,	1	),
	A5	("Selkirk rex",	1,	100,	1	),
	A6	("Anatoli",	1,	100,	1	),
	A7	("Bengal",	1,	100,	1	),
	A8	("Balinais",	1,	100,	1	),
	A9	("Somali",	1,	100,	1	),
	A10	("Turc de Van",	1,	100,	1	),
	A11	("Highlander",	1,	100,	1	),
	A12	("Brazilian shorthair",	1,	100,	1	),
	A13	("Ojos azules",	1,	100,	1	),
	A14	("Norv??gien ou Skogkatt",	1,	100,	1	),
	A15	("Siamois",	1,	100,	1	),
	A16	("Cymric",	1,	100,	1	),
	A17	("Ragamuffin",	1,	100,	1	),
	A18	("Colorpoint shorthair",	1,	100,	1	),
	A19	("American shorthair",	1,	100,	1	),
	A20	("Ural rex",	1,	100,	1	),
	A21	("Khao Manee",	1,	100,	1	),
	A22	("Pixie-bob",	1,	100,	1	),
	A23	("Bleu russe",	1,	100,	1	),
	A24	("Seychellois",	1,	100,	1	),
	A25	("Tiffany",	1,	100,	1	),
	A26	("Exotic shorthair",	1,	100,	1	),
	A27	("Cornish rex",	1,	100,	1	),
	A28	("Serengeti",	1,	100,	1	),
	A29	("Abyssin",	1,	100,	1	),
	A30	("Safari",	1,	100,	1	),
	A31	("Bombay",	1,	100,	1	),
	A32	("Ragdoll",	1,	100,	1	),
	A33	("Asian",	1,	100,	1	),
	A34	("Tha??",	1,	100,	1	),
	A35	("Maine coon",	1,	100,	1	),
	A36	("Chartreux",	1,	100,	1	),
	A37	("Californian spangled",	1,	100,	1	),
	A38	("York chocolat",	1,	100,	1	),
	A39	("LaPerm",	1,	100,	1	),
	A40	("Savannah",	1,	100,	1	),
	A41	("British longhair",	1,	100,	1	),
	A42	("Korat",	1,	100,	1	),
	A43	("Mau arabe",	1,	100,	1	),
	A44	("Devon rex",	1,	100,	1	),
	A45	("Manx",	1,	100,	1	),
	A46	("Persan",	1,	100,	1	),
	A47	("Bobtail des Kouriles",	1,	100,	1	),
	A48	("Scottish fold",	1,	100,	1	),
	A49	("Tonkinois",	1,	100,	1	),
	A50	("European shorthair",	1,	100,	1	),
	A51	("Bobtail am??ricain",	1,	100,	1	),
	A52	("Chantilly",	1,	100,	1	),
	A53	("Donskoy",	1,	100,	1	),
	A54	("American wirehair",	1,	100,	1	),
	A55	("Oriental shorthair",	1,	100,	1	),
	A56	("Burmese",	1,	100,	1	),
	A57	("Mau égyptien",	1,	100,	1	),
	A58	("Angora turc",	1,	100,	1	),
	A59	("Ocicat",	1,	100,	1	),
	A60	("Mandarin",	1,	100,	1	),
	A61	("Chausie",	1,	100,	1	),
	A62	("Munchkin",	1,	100,	1	),
	A63	("Skookum",	1,	100,	1	),
	A64	("Highland fold[11]",	1,	100,	1	),
	A65	("American curl",	1,	100,	1	),
	A66	("Sacré de Birmanie",	1,	100,	1	),
	A67	("Brume australienne",	1,	100,	1	),
	A68	("Himalayen",	1,	100,	1	),
	A69	("Sokok??",	1,	100,	1	),
	A70	("Havana brown",	1,	100,	1	),
	A71	("Bobtail japonais",	1,	100,	1	),
	A72	("German rex",	1,	100,	1	),
	A73	("Peterbald",	1,	100,	1	),
	A74	("Burmilla",	1,	100,	1	),
	A75	("Nebelung",	1,	100,	1	),
	A76	("Sphynx",	1,	100,	1	),
	A77	("Singapura",	1,	100,	1	),
	A78	("Toyger",	1,	100,	1	),
	A79	("Californian rex",	1,	100,	1	),
	A80	("Ceylan",	1,	100,	1	),

	Europeen("Européen", 3, 5, 1),
	Sacre("Sacré de birmanie", 3, 5, 1),
	Siamois("Siamois", 3, 5, 1)
	;


private String name = "";

private float maxP =0F;
private float minP=0F;
private int categorie=0;
private float coeffRace=1;
 
//Constructeur
 RaceChat(String name, float mminPo, float maxP, int categrorieo, float race){
  this.name = name;
  this.minP=mminPo;
  this.maxP=maxP;
  this.categorie=categrorieo;
  this.coeffRace=race;
}
 RaceChat(String name, float mminPo, float maxP, int categrorieo){
	  this.name = name;
	  this.minP=mminPo;
	  this.maxP=maxP;
	  this.categorie=categrorieo;
	
	}
 public String nameToString() {
	return name;
}
 public float getCoeffRace() {
	return coeffRace;
}
 public float getMaxP() {
	return maxP;
}
 public float getMinP() {
	return minP;
}
 public String nameToID() {
	return name()+"CT";
}



}

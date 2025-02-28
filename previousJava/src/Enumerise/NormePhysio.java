package Enumerise;

public enum NormePhysio {
	VOID("Pas de r??f??rence", -1,-1,-1,-1,""),
	RPCanimM("RPC aliment/BEE", 20, -1,60,-1,"g/Mcal"),
	RPCaninCrois("RPC aliment/BEE", 45, -1,75,-1,"g/Mcal"),
	RPCaninDiab("RPC aliment/BEE", 65, -1,85,-1,"g/Mcal"),
	VitACaninPhys("Vitamine A /BEE", 1000, 50000, 2500, 5000,"UI/Mcal"),//NRC et Blanchard
	VitACaninGes("Vitamine A /BEE", 1000, 50000, 2500, 5000,"UI/Mcal"),//NRC et Blanchard
	VitACaninCrois("Vitamine A /BEE", 1000, 11000, 2500, 5000,"UI/Mcal"),//NRC et Blanchard
	NaCaninPhys("Sodium /BEE", 0.075F, 4, 0.2500F, 1.25F,"g/Mcal"),//NRC et Blanchard
	NaCaninCrois("Sodium /BEE", 0.075F, 4, 0.5500F, 1.25F,"g/Mcal"),
	NaCaninGes("Sodium /BEE", 0.075F, 4, 0.500F, 1.25F,"g/Mcal"),
	VitDCaninPhys("Vitamine D /BEE", 70, 800, 120, 500,"UI/Mcal"),//NRC et Blanchard
	VitDCaninCrois("Vitamine D /BEE", 70, 800, 120, 500,"UI/Mcal"),//NRC et Blanchard
	VitDCaninGes("Vitamine D /BEE", 70, 800, 120, 500,"UI/Mcal"),//NRC et Blanchard
	VitB2CaninPhys("Vitamine B2 /BEE", 1.05F, -1, 1.3F, -1,"mg/Mcal"),//NRC et Blanchard
	CaCaninPhys("Calcium /BEE", 0.5F, -1, 1.5F, 2,"g/Mcal"),//NRC et Blanchard
	CaCaninGes("Calcium /BEE", 1.75F, 4.5F, 2F, 3.75F,"g/Mcal"),//NRC et Blanchard
	CaCaninCroisGrand("Calcium /BEE", 1.75F, 3F, 2F, 2.5F,"g/Mcal"),//NRC et Blanchard
	CaCaninCrois("Fe /BEE", 1.75F, 4.5F, 2F, 3.75F,"g/Mcal"),//NRC et Blanchard
	FeCaninPhys("Fe /BEE", -1, -1, 7.5F, 15,"mg/Mcal"),//NRC et Blanchard
	FeCaninGes("Fe /BEE", -1, -1, 17F, 27,"mg/Mcal"),//NRC et Blanchard
	
	FeCaninCrois("Fe /BEE", 18F, -1, 20F, 25,"mg/Mcal"),//NRC et Blanchard
	
	CuCaninPhys("Fe /BEE", -1, -1, 1.5F, 2.5F,"mg/Mcal"),//NRC et Blanchard
	CuCaninGes("Fe /BEE", -1, -1, 3.1F, 4.1F,"mg/Mcal"),//NRC et Blanchard
	ZnCuCaninPhys("Fe /BEE", -1, -1, 8F, 12F,""),//NRC et Blanchard
	CuCaninCrois("Fe /BEE", -1, -1, 2.7F, 3.7F,"mg/Mcal"),//NRC et Blanchard
	ZnCaninCrois("Zn /BEE", 10, -1, 25F, -1,"mg/Mcal"),//NRC et Blanchard
	MnCaninCrois("Mn /BEE", -1, -1, 1.4F, -1,"mg/Mcal"),//NRC et Blanchard
	SeCaninCrois("Se /BEE", 52.5F, -1, 87.5F, -1,"??g/Mcal"),//NRC et Blanchard
	ICaninCrois("Iode /BEE", -1, -1, 220F, -1,"??g/Mcal"),//NRC et Blanchard
	
	ZnCaninGes("Zn /BEE", -1, - 1, 24F, -1,"mg/Mcal"),//NRC et Blanchard
	MnCaninGes("Mn /BEE", -1, -1, 1.8F, -1,"mg/Mcal"),//NRC et Blanchard
	SeCaninGes("Se /BEE", -1, -1, 87.5F, -1,"??g/Mcal"),//NRC et Blanchard
	ICaninGes("Iode /BEE", 175, 1000, 220, 500,"??g/Mcal"),//NRC et Blanchard
	
	ZnCaninPhys("Zn /BEE", -1, -1, 15F, -1,"mg/Mcal"),//NRC et Blanchard
	MnCaninPhys("Mn /BEE", -1, -1, 1.2F, -1,"mg/Mcal"),//NRC et Blanchard
	SeCaninPhys("Se /BEE", -1, -1, 87.5F, -1,"??g/Mcal"),//NRC et Blanchard

	MgCaninPhys("Magnesium /BEE", 45, -1, 150, 250,"mg/Mcal"),//NRC et Blanchard
	MgCaninCrois("Magnesium /BEE", 45, -1, 100, 250,"mg/Mcal"),//NRC et Blanchard
	
	ICaninPhys("Iode /BEE", 175, 1000, 200, 500,"??g/Mcal"),//NRC et Blanchard
	KCaninPhys("Potassium /BEE", -1, -1, 1000F, 2500F,"mg/Mcal"),//NRC et Blanchard
	KCaninGes("Potassium /BEE", -1, -1, 900F, -1,"mg/Mcal"),
	KCaninCrois("Potassium /BEE", -1, -1, 1100F, -1,"mg/Mcal"),
	EauCanin("Apport en eau /kg",-1,-1,50F,100F,"mL/kg"),
	EauFelin("Apport en eau /kg",-1,-1,50F,75F,"mL/kg"),
	PCaninPhys("Phosphore /BEE", -1, -1, 0.75F, 2,"g/Mcal"),
	PCaninIRC("Phosphore /BEE", -1, 1, -1, -1,"g/Mcal"),//NRC et Blanchard
	CaPCaninPhys("Calcium/Phosphore", 1, 2, -1, -1,""),
	CaPCaninIRC("Calcium/Phosphore", 2, 3, -1, -1,""),//Blanchard
	KNaCaninIRC("Potassium/Sodium", -1, -1, 2, -1,""),
	O6O3CaninPhys("Omega6:Omega3", -1, -1, 2, 7,""),
	O6O3CaninIRC("Omega6:Omega3", -1, -1, 1, 3,""),
	O6O3CaninDiab("Omega6:Omega3", -1, -1, 1, 3,""),
	CaPCaninCrois("Calcium/Phosphore", 1, 2, -1, -1,""),
	CaPCaninGes("Calcium/Phosphore", 1, 2, -1, -1,""),
	CaPCaninLac("Calcium/Phosphore", 1, 2, -1, -1,""),
	
	
	
	O6CaninPhys("Omega6", 2.4F, 16.3F, 2.8F, -1, "g/Mcal"),
	O6CaninCrois("Omega6", 3F, 16.3F, 3.3F, -1, "g/Mcal"),
	ArgCaninPhys("Arginine", 0.70F, -1, 0.88F, -1, "g/Mcal"),
	HisCaninPhys("Histidine", 0.37F, -1, 0.48F, -1, "g/Mcal"),
	IsoleuCaninPhys("Isoleucine", 0.75F, -1, 0.95F, -1, "g/Mcal"),
	MethCaninPhys("Methionine", 0.65F, -1, 0.83F, -1, "g/Mcal"),
	MethCysCaninPhys("Methionine & cyst??ine", 1.30F, -1, 1.63F, -1, "g/Mcal"),
	LeucineCaninPhys("Leucine", 1.35F, -1, 1.70F, -1, "g/Mcal"),
	LysineCaninPhys("Lysine", 0.70F, -1, 0.88F, -1, "g/Mcal"),
	PhenylCaninPhys("Phenylalanine", 0.90F, -1, 1.13F, -1, "g/Mcal"),
	PhenylTyrCaninPhys("Phenylalanine & Tyrosine", 1.48F, -1, 1.85F, -1, "g/Mcal"),
	ThreoCaninPhys("Threonine", 0.85F, -1, 1.08F, -1, "g/Mcal"),
	TryCaninPhys("Tryptophane", 0.28F, -1, 0.35F, -1, "g/Mcal"),
	ValCaninPhys("Valine", 0.98F, -1, 1.23F, -1, "g/Mcal"),
	
	
	ArgCaninCrois("Arginine", 1.58F, -1, 1.98F, -1, "g/Mcal"),
	HisCaninCrois("Histidine", 0.78F, -1, 0.98F, -1, "g/Mcal"),
	IsoleuCaninCrois("Isoleucine", 1.30F, -1, 1.63F, -1, "g/Mcal"),
	MethCaninCrois("Methionine", 0.70F, -1, 0.88F, -1, "g/Mcal"),
	MethCysCaninCrois("Methionine & cyst??ine", 1.40F, -1, 1.75F, -1, "g/Mcal"),
	LeucineCaninCrois("Leucine", 2.58F, -1, 3.22F, -1, "g/Mcal"),
	LysineCaninCrois("Lysine", 1.75F, -1, 2.2F, -1, "g/Mcal"),
	PhenylCaninCrois("Phenylalanine", 1.30F, -1, 1.63F, -1, "g/Mcal"),
	PhenylTyrCaninCrois("Phenylalanine & Tyrosine", 2.60F, -1, 3.25F, -1, "g/Mcal"),
	ThreoCaninCrois("Threonine", 1.63F, -1, 2.03F, -1, "g/Mcal"),
	TryCaninCrois("Tryptophane", 0.45F, -1, 0.58F, -1, "g/Mcal"),
	ValCaninCrois("Valine", 1.35F, -1, 1.70F, -1, "g/Mcal"),
	
	RPFelimM("RPC aliment/BEE", 40, -1,70,-1,"g/Mcal"),
	RPFelinCrois("RPC aliment/BEE", 45, -1,75,-1,"g/Mcal"),
	//RPFelinDiab("RPC aliment/BEE", 75, -1,95,-1,"g/Mcal"),
	VitAFelinPhys("Vitamine A /BEE", -1, 50000, 1000, 2500,"UI/Mcal"),//NRC et Blanchard
	VitAFelinCrois("Vitamine A /BEE", -1, 50000, 2000, 3000,"UI/Mcal"),//NRC et Blanchard
	VitAFelinGes("Vitamine A /BEE", 1000, 50000, 2000, 3000,"UI/Mcal"),//NRC et Blanchard
	NaFelinPhys("Sodium /BEE", 0.16F, 4, 0.1700F, 1.25F,"g/Mcal"),
	NaFelinCrois("Sodium /BEE", 0.310F, 4, 0.35F, 1.25F,"g/Mcal"),
	NaFelinGes("Sodium /BEE", 0.67F, 4, 0.67F, 1.25F,"g/Mcal"),
	PFelinPhys("Sodium /BEE", 0.35F, -1, 0.6500F, -1,"g/Mcal"),
	PFelinCrois("Sodium /BEE", 0.670F, -1, 1.F, -1,"g/Mcal"),

	CuFelinPhys("Fe /BEE", -1, -1, 1.2F, -1,"mg/Mcal"),//NRC et Blanchard
	CuFelinGes("Fe /BEE", -1, -1, 2.2F, -1,"mg/Mcal"),//NRC et Blanchard
	
	CuFelinCrois("Fe /BEE", 1.1F, -1, 2.1F, -1,"mg/Mcal"),//NRC et Blanchard
	///NRC et Blanchard
	VitDFelinPhys("Vitamine D /BEE", 50, 2000, 150, 500,"UI/Mcal"),//NRC et Blanchard
	VitDFelinCrois("Vitamine D /BEE", 50, 2000, 150, 500,"UI/Mcal"),//NRC et Blanchard
	VitDFelinGes("Vitamine D /BEE", 50, 2000, 150, 500,"UI/Mcal"),//NRC et Blanchard
	VitB2FelinPhys("Vitamine B2 /BEE", 1.05F, -1, 1.3F, -1,"mg/Mcal"),//NRC et Blanchard
	CaFelinPhys("Calcium /BEE", 0.5F, -1, 1.5F, 2,"g/Mcal"),//NRC et Blanchard
	CaFelinCrois("Calcium /BEE", 2F, 4.5F, 2F, 3.75F,"g/Mcal"),//NRC et Blanchard
	
	MgFelinPhys("Magnesium /BEE", 50, -1, 100, -1,"mg/Mcal"),//NRC et Blanchard
	MgFelinCrois("Magnesium /BEE", 40, -1, 100, -1,"mg/Mcal"),//NRC et Blanchard
	MgFelinGes("Magnesium /BEE", 104, -1, 125, -1,"mg/Mcal"),//NRC et Blanchard
	IFelinPhys("Iode /BEE", 320, 1000, 350, 500,"??g/Mcal"),//NRC et Blanchard
	KFelinPhys("Potassium /BEE", -1, -1, 1300F, -1,"mg/Mcal"),//NRC et Blanchard
	KFelinCrois("Potassium /BEE", 670, -1, 1000F, -1,"mg/Mcal"),//NRC et Blanchard
	KFelinGes("Potassium /BEE", -1, -1, 1300F, -1,"mg/Mcal"),//NRC et Blanchard
	
	FeFelinPhys("Fe /BEE", -1, -1, 20F, 30,"mg/Mcal"),//NRC et Blanchard
	FeFelinGes("Fe /BEE", -1, -1, 20, 30,"mg/Mcal"),//NRC et Blanchard
	
	FeFelinCrois("Fe /BEE", 18F, -1, 20F, 25,"mg/Mcal"),//NRC et Blanchard
	
	
//	PFelinIRC("Phosphore /BEE", -1, 1, -1, -1,"mg/Mcal"),//NRC et Blanchard
	CaPFelinPhys("Calcium/Phosphore", 1, 2, -1, -1,""),
	CaPFelinIRC("Calcium/Phosphore", 2, 3, -1, -1,""),//Blanchard
	//KNaFelinIRC("Potassium/Sodium", 2, -1, -1, -1,""),
	O6O3FelinPhys("Omega6:Omega3", -1, -1, 2, 7,""),
//	O6O3FelinIRC("Omega6:Omega3", -1, -1, 1, 3,""),
//	O6O3FelinDiab("Omega6:Omega3", -1, -1, 1, 3,""),
	O6FelinPhys("Omega6", 2.4F, 16.3F, 2.8F, -1, "g/Mcal"),
	O6FelinCrois("Omega6", 3F, 16.3F, 3.3F, -1, "g/Mcal"),

	ZnFelinCrois("Zn /BEE", 12.5F, - 1, 18.5F, -1,"mg/Mcal"),//NRC et Blanchard
	MnFelinCrois("Mn /BEE", -1, -1, 1.2F, -1,"mg/Mcal"),//NRC et Blanchard
	SeFelinCrois("Se /BEE", 30F, -1, 75F, -1,"??g/Mcal"),//NRC et Blanchard
	IFelinCrois("Iode /BEE", -1, -1, 450F, -1,"??g/Mcal"),//NRC et Blanchard
	
	ZnFelinGes("Zn /BEE", 10.5F, - 1, 15F, -1,"mg/Mcal"),//NRC et Blanchard
	MnFelinGes("Mn /BEE", -1, -1, 1.8F, -1,"mg/Mcal"),//NRC et Blanchard
	SeFelinGes("Se /BEE", -1, -1, 75F, -1,"??g/Mcal"),//NRC et Blanchard
	IFelinGes("Iode /BEE", -1, 1000, 450, 500,"??g/Mcal"),//NRC et Blanchard
	
	ZnFelinPhys("Zn /BEE", -1, -1, 18.5F, -1,"mg/Mcal"),//NRC et Blanchard
	MnFelinPhys("Mn /BEE", -1, -1, 1.2F, -1,"mg/Mcal"),//NRC et Blanchard
	SeFelinPhys("Se /BEE", -1, -1, 75F, -1,"??g/Mcal"),//NRC et Blanchard
	ZnCuFelinPhys("Fe /BEE", -1, -1, 8F, 12F,""),//NRC et Blanchard
	
	
	ArgFelinCrois("Arginine", 1.93F, 8.75F, 2.4F, -1, "g/Mcal"),
	HisFelinCrois("Histidine", 0.65F, 5.5F, 0.83F, -1, "g/Mcal"),
	IsoleuFelinCrois("Isoleucine", 1.08F, 21.7F, 1.4F, -1, "g/Mcal"),
	MethFelinCrois("Methionine", 0.88F, 3.25F, 1.1F, -1, "g/Mcal"),
	MethCysFelinCrois("Methionine & cyst??ine", 1.75F, -1, 2.2F, -1, "g/Mcal"),
	LeucineFelinCrois("Leucine", 2.55F, 21.7F, 3.2F, -1, "g/Mcal"),
	LysineFelinCrois("Lysine", 1.7F, 14.5F, 2.1F, -1, "g/Mcal"),
	PhenylFelinCrois("Phenylalanine", 1.0F, 7.25F, 1.3F, -1, "g/Mcal"),
	PhenylTyrFelinCrois("Phenylalanine & Tyrosine", 3.83F, 17, 4.8F, -1, "g/Mcal"),
	ThreoFelinCrois("Threonine", 1.3F, 12.7F, 1.6F, -1, "g/Mcal"),
	TryFelinCrois("Tryptophane", 0.33F, 4.25F, 0.40F, -1, "g/Mcal"),
	ValFelinCrois("Valine", 1.28F, 21.7F, 1.6F, -1, "g/Mcal"),
	TaurFelinCrois("Taurine", 0.08F, 82.5F, 0.1F, -1, "g/Mcal"),
	
	ArgFelinPhys("Arginine", 1.93F, -1, 1.93F, -1, "g/Mcal"),
	HisFelinPhys("Histidine", 0.65F, -1, 0.65F, -1, "g/Mcal"),
	IsoleuFelinPhys("Isoleucine", 1.08F, -1, 1.08F, -1, "g/Mcal"),
	MethFelinPhys("Methionine", 0.34F, -1, 0.43F, -1, "g/Mcal"),
	MethCysFelinPhys("Methionine & cyst??ine", 0.68F, -1, 0.85F, -1, "g/Mcal"),
	LeucineFelinPhys("Leucine", 2.55F, -1, 2.55F, -1, "g/Mcal"),
	LysineFelinPhys("Lysine", 0.68F, -1, 0.85F, -1, "g/Mcal"),
	PhenylFelinPhys("Phenylalanine", 1.0F, -1, 1.0F, -1, "g/Mcal"),
	PhenylTyrFelinPhys("Phenylalanine & Tyrosine", 3.83F, -1, 3.83F, -1, "g/Mcal"),
	ThreoFelinPhys("Threonine", 1.3F, -1, 1.3F, -1, "g/Mcal"),
	TryFelinPhys("Tryptophane", 0.33F, -1, 0.33F, -1, "g/Mcal"),
	ValFelinPhys("Valine", 1.28F, -1, 1.28F, -1, "g/Mcal"),
	TaurFelinPhys("Taurine", 0.08F, -1, 0.1F, -1, "g/Mcal")
	;


private String name = "";
private float optimax=0F;
private float optimin=0F;

private float max =0F;
private float min=0F;

private String unite="";
 
//Constructeur

NormePhysio(String name, float mminPo, float maxP, float optiminp, float optimaxp, String categrorieo){
	  this.name = name;
	  this.min=mminPo;
	  this.max=maxP;
	  this.optimin=optiminp;
	  this.optimax=optimaxp;
	  this.unite=categrorieo;
	
	}
 public String nameToString() {
	return name;
}
public float getMax() {
	return max;
}
public float getMin() {
	return min;
}
public String getUnite() {
	return unite;
}
 public float getOptimax() {
	return optimax;
}
 public float getOptimin() {
	return optimin;
}


}

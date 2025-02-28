package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import DataStruct.CoefP;
import DataStruct.NutrientRef;
import DataStruct.NutrientRefP;
import DataStruct.SupplementalvariableP;
import Enumerise.AAEnum;
import Enumerise.AllNutrient;
import Enumerise.MainNutrientEnum;
import Enumerise.Nutrient;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientOther;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import Enumerise.UnitEnum;
import Enumerise.UnitReqEnum;
import equation.Equation;
import equation.EquationMaster;
import equation.listNutrientRef;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ReferenceEv implements Serializable, Cloneable{
	static private final long serialVersionUID = 1L;
	Map<Nutrient, Nut4Ref> refMapMin=new HashMap<Nutrient, Nut4Ref>();
	Map<Nutrient, Nut4Ref> refMapMax=new HashMap<Nutrient, Nut4Ref>();
	Map<Nutrient, Nut4Ref> refMapOMin=new HashMap<Nutrient, Nut4Ref>();
	Map<Nutrient, Nut4Ref> refMapOMax=new HashMap<Nutrient, Nut4Ref>();
	public String UUID;
	private String nom="";
	private String description="";
	private boolean disease=false;
	private String nameDisease="";
	private String nameEnergy="";
private int consistent=1;
	private Equation BWEqu=new Equation();
	private Equation BEEqu=new Equation();
	private Equation DEcomEqu=new Equation();
	private Equation DErawEqu=new Equation();
private ArrayList<Equation> NutEqu=new ArrayList<Equation>();
	private ArrayList<CoefP> modk1=new ArrayList<CoefP>();
	private ArrayList<CoefP> modk2=new ArrayList<CoefP>();
	private ArrayList<CoefP> modk3=new ArrayList<CoefP>();
	private ArrayList<CoefP> modk4=new ArrayList<CoefP>();
	private ArrayList<CoefP> modk5=new ArrayList<CoefP>();
	private String namek1="";
	private String namek2="";
	private String namek3="";
	private String namek4="";
	private String namek5="";



	private Espece species=Espece.CHIEN;
	private StadePhysio sPhysio= StadePhysio.ADULTE;
	
	public ReferenceEv() {

		//

		
	UUID=java.util.UUID.randomUUID().toString();


	
	modk1.add(new CoefP("Normal", 1, 0 ));
	modk2.add(new CoefP("Normal", 1,1 ));
	modk3.add(new CoefP("Normal", 1,2 ));
	modk4.add(new CoefP("Normal", 1,3));
	modk5.add(new CoefP("Normal", 1,4 ));
		// TODO Auto-generated constructor stub
	}
	public ReferenceEv(String uuid) {

		//

		
	UUID=uuid;

	

		// TODO Auto-generated constructor stub
	}
	public String getUUID() {
		return UUID;
	}
	//mise ?? jour des valeurs
	public void setNutrient(float a,Nutrient enu, Reflevel ref,UnitReqEnum unreq, BiblioRef bibli){
	getMap(ref).put(enu, new Nut4Ref(enu, ref,a, enu.getUe(), unreq, bibli));
	
	}
	public void setsPhysio(StadePhysio sPhysio) {
		this.sPhysio = sPhysio;
	}
	public StadePhysio getsPhysio() {
		return sPhysio;
	}

	//orise des valeurs
	public float getNutrient(Nutrient enu, Reflevel ref){
		

			if (this.isNutrient(enu, ref)){
				return this.getMap(ref).get(enu).getQuantity();}
				else return -1;
	
	}
	
	public boolean isNutrient(Nutrient enu, Reflevel ref){
		return this.getMap(ref).containsKey(enu);
	
	}
	
	public void removeNutrient(Nutrient enu, Reflevel ref){
		this.getMap(ref).remove(enu);
	}

	

	public BiblioRef getNutrientBib(Nutrient enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.getMap(ref).get(enu).getBiblio();
	} else {
		 BiblioRef re=new BiblioRef();		
		return re;
	}
	}
	
	public int getNutrientUnit(Nutrient enu, Reflevel ref){
		if (this.isNutrient(enu, ref)){
		return this.getMap(ref).get(enu).getUnitReq().getID();
	} else {
		 int re=0;		
		return re;
	}
	}
	
	public String getSpecies() {
		return (""+species.nameToString());
	}
	public void setSpecies(Espece espece) {
		this.species= espece;
	}
	public void setName(String nom) {
		this.nom = nom;
	}
	public void setDescription(String descriprion) {
		this.description = descriprion;
	}public String getDescription() {
		return description;
	}public String getName() {
		return nom;
	}
	public void setBEEqu(Equation bEEqu) {
		BEEqu = bEEqu;
	}
	public void setBWEqu(Equation bWEqu) {
		BWEqu = bWEqu;
	}
	public void setDEcomEqu(Equation dEcomEqy) {
		DEcomEqu = dEcomEqy;
	}
	public void setDErawEqu(Equation dERaw) {
		DErawEqu = dERaw;
	}
	public void setDisease(boolean disease) {
		this.disease = disease;
	}
	public void setModk1(ObservableList<CoefP> l) {
		this.modk1 = new ArrayList<CoefP>(l);
	}
	public void setModk2(ObservableList<CoefP> l) {
		this.modk2 = new ArrayList<CoefP>(l);
	}
	public void setModk3(ObservableList<CoefP> l) {
		this.modk3 = new ArrayList<CoefP>(l);
	}
	public void setModk4(ObservableList<CoefP> l) {
		this.modk4 = new ArrayList<CoefP>(l);
	}
	public void setModk5(ObservableList<CoefP> l) {
		this.modk5 = new ArrayList<CoefP>(l);
	}
	public void setNameDisease(String nameDisease) {
		this.nameDisease = nameDisease;
	}
	public void setNameEnergy(String nameEnergy) {
		this.nameEnergy = nameEnergy;
	}
	public Equation getBEEqu() {
		return BEEqu;
	}
	public Equation getBWEqu() {
		return BWEqu;
	}
	public Equation getDEcomEqu() {
		return DEcomEqu;
	}
	public Equation getDErawEqu() {
		return DErawEqu;
	}
	public ObservableList<CoefP> getModk1() {
		return FXCollections.observableArrayList(modk1);
	}
	public ObservableList<CoefP> getModk2() {
		return FXCollections.observableArrayList(modk2);
	}
	public ObservableList<CoefP> getModk3() {
		return FXCollections.observableArrayList(modk3);
	}
	public ObservableList<CoefP> getModk4() {
		return FXCollections.observableArrayList(modk4);
	}
	public ObservableList<CoefP> getModk5() {
		return FXCollections.observableArrayList(modk5);
	}
	 public String getNamek1() {
		return namek1;
	}
	 public String getNamek2() {
		return namek2;
	}
	 public String getNamek3() {
		return namek3;
	}
	 public String getNamek4() {
		return namek4;
	}
	 public String getNamek5() {
		return namek5;
	}
	 public void setNamek1(String namek1) {
		this.namek1 = namek1;
	}
	 public void setNamek2(String namek2) {
		this.namek2 = namek2;
	}
	 public void setNamek3(String namek3) {
		this.namek3 = namek3;
	}
	 public void setNamek4(String namek4) {
		this.namek4 = namek4;
	}
	 public void setNamek5(String namek5) {
		this.namek5 = namek5;
	}
	public String getNameDisease() {
		return nameDisease;
	}
	public String getNameEnergy() {
		return nameEnergy;
	}
	public boolean isDisease() {
		return disease;
	}
	
public int getGroupIDk(int i) {
return i;

}
public String getGroupName(int i) {
	switch(i) {
	case 0:
		 return namek1;
		
	case 1:
		 return namek2;
		
	case 2:
		 return namek3;
		
	case 3:
		 return namek4;
		
	case 4:
		 return namek5;
		
	}
	return"";

}
public void setGroupName(int i, String s) {
	switch(i) {
	case 0:
		 this.namek1=s;
		break;
	case 1:
		 this.namek2=s;
		 break;
	case 2:
		 this.namek3=s;
		 break;
	case 3:
		 this.namek4=s;
		 break;
	case 4:
		 this.namek5=s;
		 break;}
	}

	public void setModk(int i, ObservableList<CoefP>l) {
		switch(i) {
		case 0:
			 this.modk1=new ArrayList<CoefP>(l);
			break;
		case 1:
			 this.modk2=new ArrayList<CoefP>(l);
			 break;
		case 2:
			 this.modk3=new ArrayList<CoefP>(l);
			 break;
		case 3:
			 this.modk4=new ArrayList<CoefP>(l);
			 break;
		case 4:
			 this.modk5=new ArrayList<CoefP>(l);
			 break;
		}

}
public ObservableList<CoefP> getGroupk(int i) {
	switch(i) {
	case 0:
		 return FXCollections.observableArrayList(modk1);
		
	case 1:
		 return FXCollections.observableArrayList(modk2 );
		
	case 2:
		 return FXCollections.observableArrayList(modk3 );
		
	case 3:
		 return FXCollections.observableArrayList(modk4 );
		
	case 4:
		 return FXCollections.observableArrayList(modk5) ;
		
	}
	return null;

}

private Map<Nutrient, Nut4Ref> getMap(Reflevel ref){
	switch(ref) {
	case OPTIMIN:
		return refMapOMin;
		
	case OPTIMAX:
		return refMapOMax;
		
	case MIN:
		return refMapMin;
		
	case MAX:
		return refMapMax;
		
	}
	return refMapMax;
}
public int getConsistent() {
	return consistent;
}
public void setConsistent(int consistent) {
	this.consistent = consistent;
}
public void setNutrientRef(NutrientRefP NRP) {

		
			if (NRP.getQuantity().isBlank()) {
			this.removeNutrient(NRP.getMNE().getNutrient(NRP.getKind().get()), Reflevel.getById(NRP.getRelativekind()));
			}else {
				this.setNutrient((NRP.getQuantityConverted()), NRP.getMNE().getNutrient(NRP.getKind().get()),Reflevel.getById(NRP.getRelativekind()), NRP.getUnitReq(), NRP.getBiblio());
			}
			
			
		
	}
@Override
public String toString() {
return nom;
		}
public ArrayList<Equation> getNutEqu() {
	return NutEqu;
}

public void setNutEqu(ArrayList<Equation> nutEqu) {
	NutEqu = nutEqu;
}
public void setNutEqu(ObservableList<Equation> nutEqu) {
	NutEqu.clear();
	
	for(Equation eq:nutEqu) {
		
	if( eq!=null) {
		NutEqu.add(eq);}
	}
}
public void addNutEqu(Equation eq) {
	NutEqu.add(eq);
}
public ReferenceEv clone() throws CloneNotSupportedException
{
	ReferenceEv c=new ReferenceEv();
	
	c.setName("Dup "+getName());
	c.UUID=java.util.UUID.randomUUID().toString();
	c.description=getDescription();
c.disease=isDisease();
 c.nameDisease=getNameDisease();
c.nameEnergy=getNameEnergy();
c. namek1=getNamek1();
c.namek2=getNamek2();
c. namek3=getNamek3();
c. namek4=getNamek4();
c. namek5=getNamek5();
	c.refMapMax=refMapMax;
	c.refMapMin=refMapMin;
	c.refMapOMax=refMapOMax;
	c.refMapOMin=refMapOMin;
	
	c. description=""+description;
	c.disease=disease;

	c. BWEqu=getBWEqu();
	c. BEEqu=getBEEqu();
	c. DEcomEqu=getDEcomEqu();
	c. DErawEqu=getDErawEqu();

	c.modk1=new ArrayList<CoefP>();
	c. modk2=new ArrayList<CoefP>();
	c. modk3=new ArrayList<CoefP>();
c. modk4=new ArrayList<CoefP>();
	c. modk5=new ArrayList<CoefP>();
c.NutEqu=getNutEqu();
for(CoefP p:modk1) {
	c.modk1.add(new CoefP(p.getDescription(), p.getCoef(), p.getGroupUUID()));
}
for(CoefP p:modk2) {
	c.modk2.add(new CoefP(p.getDescription(), p.getCoef(), p.getGroupUUID()));
}
for(CoefP p:modk3) {
	c.modk3.add(new CoefP(p.getDescription(), p.getCoef(), p.getGroupUUID()));
}
for(CoefP p:modk4) {
	c.modk4.add(new CoefP(p.getDescription(), p.getCoef(), p.getGroupUUID()));
}
for(CoefP p:modk5) {
	c.modk5.add(new CoefP(p.getDescription(), p.getCoef(), p.getGroupUUID()));
}


    return c;
}

public ObservableList<NutrientRef> getListNutrient(Nutrient n,  float BEE, float BW, float MW, RationCalculator calc, ObservableList<SupplementalvariableP> svp, Ration rat){
	ObservableList<NutrientRef> ol=FXCollections.observableArrayList();
 boolean isSpeVal=false;
 float speVal=0;
 Equation speEq=new Equation();
	for (Equation eq:NutEqu) {

		if (	eq.getAlllNut().getID()==(n.getMNE().getCoef()*1000+n.getCoef())) {
		
		speVal=(float) eq.runNeed(calc, svp, rat);
		
		speEq=eq;
		isSpeVal=true;
		
	}
	}
	
		for(Reflevel rl:Reflevel.values()) {
			if (isSpeVal & rl.equals(Reflevel.OPTIMIN)) {
				ol.add(speNutRef( n.getMNE(),  n.getCoef(),   BEE,  BW,  MW, speVal,  speEq));
			}
			
			else	if (isNutrient(n, rl))
				 {
				ol.add(new NutrientRef(n.getLabel(), rl, getNutrient(n, rl),
						"%", UnitReqEnum.getById( getNutrientUnit(n, rl)), 
						getNutrientBib(n, rl),
						disease,
						nom, BEE, BW, MW));
			}
		}
	
	

	return ol;
	
	
}

public ArrayList<Equation> getAllEquation (){
	ArrayList<Equation> ale=new ArrayList<Equation>();
	if (!BEEqu.getName().isBlank()){
	ale.add(BEEqu);}
	if (!BWEqu.getName().isBlank()){
	ale.add(BWEqu);}
	if (!DEcomEqu.getName().isBlank()){
	ale.add(DEcomEqu);}
	if (!DErawEqu.getName().isBlank()){
	ale.add(DErawEqu);}
	ale.addAll(NutEqu);
	for(Equation e:ale) {
		if (e.getName().isBlank()) {
			
	}}
	return ale;
}
public ArrayList<BiblioRef> getAllBibli(ArrayList<BiblioRef>res){
	for (Nut4Ref r: refMapMin.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapOMin.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapMax.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapOMax.values()) {
		res=addBibToArray(res,r.getBiblio());
	}

	res=addBibToArray(res,BEEqu.getBib());
	res=addBibToArray(res,BWEqu.getBib());
	res=addBibToArray(res,DErawEqu.getBib());
	res=addBibToArray(res,DEcomEqu.getBib());
	return res;
}
public ArrayList<BiblioRef> getAllBibli(){
	ArrayList<BiblioRef>res=new ArrayList<BiblioRef>();
	for (Nut4Ref r: refMapMin.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapOMin.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapMax.values()) {
		res=addBibToArray(res,r.getBiblio());
	}
	for (Nut4Ref r: refMapOMax.values()) {
		res=addBibToArray(res,r.getBiblio());
	}

	res=addBibToArray(res,BEEqu.getBib());
	res=addBibToArray(res,BWEqu.getBib());
	res=addBibToArray(res,DErawEqu.getBib());
	res=addBibToArray(res,DEcomEqu.getBib());
	return res;
}


public static float[][] deepClone(float[][] source) {
    float[][] result = new float[source.length][];
    for (int i = 0; i < source.length; i++)
        result[i] = source[i].clone();
    return result;}
public static BiblioRef[][] deepClone(BiblioRef[][] source) {
    BiblioRef[][] result = new BiblioRef[source.length][];
    for (int i = 0; i < source.length; i++)
        result[i] = source[i].clone();
    return result;}
public static int[][] deepClone(int[][] source) {
    int[][] result = new int[source.length][];
    for (int i = 0; i < source.length; i++)
        result[i] = source[i].clone();
    return result;}
public static boolean[][] deepClone(boolean[][] source) {
    boolean[][] result = new boolean[source.length][];
    for (int i = 0; i < source.length; i++)
        result[i] = source[i].clone();
    return result;}
private ArrayList<BiblioRef>addBibToArray(ArrayList<BiblioRef> res, BiblioRef[]b){
	for (BiblioRef c:b) {
		if (!c.toString().equals((new BiblioRef()).toString())) {
			if(!res.contains(c)) {
				res.add(c);
			}
		}
	}
	return res;
}
private ArrayList<BiblioRef>addBibToArray(ArrayList<BiblioRef> res, BiblioRef b){
	if (!b.toString().equals((new BiblioRef()).toString())) {
			if(!res.contains(b)) {
				res.add(b);
			}		
	}
	return res;
}
private NutrientRef speNutRef(MainNutrientEnum MNE, int kind,  float BEE, float BW, float MW, float speVal, Equation speEq) {
	return new NutrientRef(((AllNutrient)AllNutrient.values().get(MNE.getCoef()*1000+kind)).getLabel(), Reflevel.OPTIMIN, speVal,
			((AllNutrient)AllNutrient.values().get(MNE.getCoef()*1000+kind)).getUnit()	, UnitReqEnum.NO, 
		speEq.getBib(),
			disease,
			nom, BEE, BW, MW);
}


class Nut4Ref implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Reflevel relativekind; 
private Nutrient nutrient;
	private float quantity;
	private UnitEnum unit; 
	private UnitReqEnum UnitReq;
	private BiblioRef Biblio;


public Nut4Ref(Nutrient n, Reflevel r, float q, UnitEnum u, UnitReqEnum ur, BiblioRef bib) {
	// TODO Auto-generated constructor stub
	nutrient=n;
	quantity=q;
	unit=u;
	UnitReq=ur;
	Biblio=bib;
	
}

public BiblioRef getBiblio() {
	return Biblio;
}
public Nutrient getNutrient() {
	return nutrient;
}
public float getQuantity() {
	return quantity;
}
public Reflevel getRelativekind() {
	return relativekind;
}
public UnitEnum getUnit() {
	return unit;
}
public UnitReqEnum getUnitReq() {
	return UnitReq;
}


	
	
}
}

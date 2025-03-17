package model;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.swing.JOptionPane;

import DataStruct.NutrientRef;
import DataStruct.SupplementalvariableP;
import Enumerise.FoodKind;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.Reflevel;
import Enumerise.UnitReqEnum;
import equation.Equation;
import equation.RequirementAnalyzer;
import equation.listNutrientRef;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RationCalculator {
	private float poids=0;
	private float poidsIdeal=0;
	private NEC nec;
	private String espece="0";
	private AdjustSave Adj;
	private int equac=0;
	private float k1=0, k2=0, k3=0, k4=0, kx=0;
	private int equation=0;
	private int dens=0;
	private Ration rat;
	private ArrayList<Ration> RationList;
	private StadePhysio sPhysio=StadePhysio.ADULTE;
	private ReferenceEv ref;
	private ArrayList<SupplementalvariableP>svp;
	private double BEE=0;
	private double MW=0;
	private Equation EqBW=new Equation();
	private Equation EqBEE=new Equation();
	private Equation EqAlim=new Equation();
	private Equation EqAlimCom=new Equation();
	private int nbpetit=0;
	private int nbSemaine=0;
	private float coefSemaine=0;
	private float poidsAdulte=0;
	public RationCalculator(){
	

	}
	public Equation getEqAlimCom() {
		return EqAlimCom;
	}
	public Equation getEqAlim() {
		return EqAlim;
	}
	public Equation getEqBEE() {
		return EqBEE;
	}
	public Equation getEqBW() {
		return EqBW;
	}
	public void setsPhysio(StadePhysio sPhysio) {
		this.sPhysio = sPhysio;
	}
	public void setConsult(ConsultationEv cons) {
		this.poids=cons.getPoids()
;
		if(cons.isPoidsIdeal()) {
			this.poidsIdeal=cons.getPoidsIdeal();
		}else {
			this.poidsIdeal=cons.getPoids();
		}
		this.k1=cons.getK1value();
		this.k2=cons.getK2value();
		this.k3=cons.getK3value();
		this.k4=cons.getK4value();
		this.kx=cons.getK5value();
		this.svp=cons.getSuppVarp();
			
		}
	
	public void calculate(boolean glob){
		if  (ref!=null) {
			if (ref.getConsistent()==1) {
				EqBW.Update(ref.getBWEqu());
				EqBEE.Update(ref.getBEEqu());
				EqAlim.Update(ref.getDErawEqu());
				EqAlimCom.Update(ref.getDEcomEqu());
				
				MW=EqBW.runAnim(this, FXCollections.observableList(svp));
				
				BEE=EqBEE.runAnim(this,FXCollections.observableList( svp));
				System.out.println("after run anim "+MW);
				if (glob) {
					for (Ration r:RationList) {
						if (r.isActual()) {
					for (AlimentRation ar:r.getAlimentList() ) {
						if (ar.getType().equals(FoodKind.COMPLET)) {
							ar.setDE(EqAlimCom.runAlim(ar));
						}else {
							ar.setDE(EqAlim.runAlim(ar));
						}
						System.out.println("after run alim");
					}}
					
				}
				}else {
				for (AlimentRation ar:rat.getAlimentList() ) {
					if (ar.getType().equals(FoodKind.COMPLET)) {
						ar.setDE(EqAlimCom.runAlim(ar));
					}else {
						ar.setDE(EqAlim.runAlim(ar));
					}
					System.out.println("after run alim");
					
				}
			}
				}
		}
	}
	 public void setRation(Ration rat) {
		this.rat = rat;
	}
	  public Ration getRation() {
		return rat;
	}
	  
	  public ArrayList<Ration> getRationList() {
		return RationList;
	}
	  public void setRationList(ArrayList<Ration> rationList) {
		RationList = rationList;
	}
	public void setEquac(int equac) {
		this.equac = equac;
	}
	public void setCoefSemaine(float coefSemaine) {
		this.coefSemaine = coefSemaine;
	}
	public void setNbPetit(int nbPetir) {
		this.nbpetit = nbPetir;
	}public void setNbSemaine(int nbSemaine) {
		this.nbSemaine = nbSemaine;
	}
	public void setPoidsAdulte(float poidsAdulte) {
		this.poidsAdulte = poidsAdulte;
	}
	public void setK1(float k1) {
		this.k1 = k1;
	}
	public void setDens(int density) {
		this.dens = density;
	}
	public void setEquation(int equation) {
		this.equation = equation;
	}


	public void setK2(float k2) {
		this.k2 = k2;
	}
	public void setK3(float k3) {
		this.k3 = k3;
	}
	public void setK4(float k4) {
		this.k4 = k4;
	}
	public void setKx(float kx) {
		this.kx = kx;
	}
	public void setNec(NEC nec) {
		this.nec = nec;
	}
	public void setPoids(float poids) {
		this.poids = poids;
	}
	public float getBE(){

		if(k1>0 && k2>0 && k3>0 && k4>0 && kx>0 && poids>0){
			float poidsOp= poids;
		return getBEE()*k1*k2*k3*k4*kx;	
		}else{
			return 0;}
	}
	public float getBEE(){
		float reponse=0;
	reponse=(float)BEE;
		return reponse;
	}
	public float getPM(){


return (float)MW;



	}
	public float getPMOpti(){

		return (float)MW;
	}
	public float getOptiPoids(float poids, Sex sex){
		if(poids>0){
			if (espece.equals("0")) {
				if (sex.equals(Sex.FEMELLE)|sex.equals(Sex.FEMELLEE)) {
					return(float) ((poids*(1-nec.getCoefchatF()/100))/(1-NEC.A5.getCoefchatF()/100));}
				else {
					return(float) ((poids*(1-nec.getCoefchat()/100))/(1-NEC.A5.getCoefchat()/100));}
				}else {
					if (sex.equals(Sex.FEMELLE)|sex.equals(Sex.FEMELLEE)) {
						return(float) ((poids*(1-nec.getCoefF()/100))/(1-NEC.A5.getCoefF()/100));}
					else {
						return(float) ((poids*(1-nec.getCoef()/100))/(1-NEC.A5.getCoef()/100));}
			}}
			
		else return 0;
	}
	public float getOptiPoids(){
	
		return poidsIdeal;
	}
	

	public Ration rationCalcEvolve(Ration r, AdjustSaveEv adj, float percentEner ,float pasEner , targetAdjust finalAdjust, RequirementAnalyzer ref) {

	r.reInitialise();
	

		//priorit?? ??n??ergie 
		r.adjustEner(getBE()*percentEner/100,targetAdjust.COMP,pasEner);
	
		for (TargetDefinitionEv at :adj.getAll()) {
		
			
			float objectif=0;
			 float objectifPCa=-1;
			switch(at.getUre()) {
			case KGBW:
				r.adjust(this.getOptiPoids()*at.getValue()*at.getPercentCompletion()/100, at.getTarg(), at.getPas());
				break;
			case KGMW:
				r.adjust(this.getPMOpti()*at.getValue()*at.getPercentCompletion()/100, at.getTarg(), at.getPas());
				break;
			case MCAL:
				r.adjust(this.getBEE()*at.getValue()*at.getPercentCompletion()/100000, at.getTarg(), at.getPas());
				break;
			case NO:
				break;
			case PERC:
				ObservableList<NutrientRef> lnr=ref.getReferences(at.getTarg().getMne(), at.getTarg().getKind());
			
				if (lnr.size()>0) {
				for (NutrientRef nr:lnr) {
					
					if (nr.getRelativekind().equals(Reflevel.MIN)|nr.getRelativekind().equals(Reflevel.OPTIMIN)) {
					
					if (nr.getTotalQuant()>objectif) {
						objectif=nr.getTotalQuant();
					}}
				}
				}
				
				if (at.getTarg().equals(targetAdjust.CALCIUM)) {
				
				 lnr=ref.getReferences(MainNutrientEnum.ANA.getCoef(), NutrientAnalysis.PCa.getCoef());
			
					if (lnr.size()>0) {
					for (NutrientRef nr:lnr) {
						
						if (nr.getRelativekind().equals(Reflevel.MIN)|nr.getRelativekind().equals(Reflevel.OPTIMIN)) {
						
						if (nr.getTotalQuant()>objectifPCa) {
							objectifPCa=nr.getTotalQuant();
							
						}}
					}
				}
				}
				if (objectifPCa>0) {
					r.adjustCal(objectif*at.getValue()*at.getPercentCompletion()/10000, at.getPas(), objectifPCa);
				}
				else {
				r.adjust(objectif*at.getValue()*at.getPercentCompletion()/10000, at.getTarg(), at.getPas());}
			
				break;
			default:
				break;
			
			}
		
		}
	
			
		
	r.adjustEner(getBE(),finalAdjust,pasEner);



	/*	if (Math.abs(100*(getBE()-r.getEnerT())/getBE())>5) {

			JOptionPane jop3 = new JOptionPane();
			jop3.showMessageDialog(null, "Attention la m??thode/ration propos??e ne semble pas permettre l'ajustement du besoin ??nerg??tique", "Echec", JOptionPane.INFORMATION_MESSAGE);


		}*/

		return r;

	}
	

	public void setEspece(String espece) {
		this.espece = espece;
	}
	public float getSumWeight(List<AlimentRation> alimrat) {
		float coef=0;
		for (int i=0; i<alimrat.size(); i++) {
			coef+=alimrat.get(i).getWeight();
		}
		return coef;
	}
	public List<AlimentRation> getRatObj(List<AlimentRation> alimrat, float obj, NutrientBase enu, float pas) {
		float coef=getSumWeight(alimrat);
		if (coef!=0) {
			for (int i=0; i<alimrat.size(); i++) {
				if (alimrat.get(i).getWeight()>0 & alimrat.get(i).getNutrient(enu)>0& obj>0) {
					float q=100*obj/((coef/alimrat.get(i).getWeight())*alimrat.get(i).getNutrient(enu));
					q=pas*Math.round(q/pas);
					alimrat.get(i).setQuantite(q);} else {
						alimrat.get(i).setQuantite(0);
					}
			}}
		return alimrat;
	}
	public List<AlimentRation> getRatObjEner(List<AlimentRation> alimrat, float obj, float pas) {
		float coef=getSumWeight(alimrat);

		if (coef!=0) {
			for (int i=0; i<alimrat.size(); i++) {

			/*	if (alimrat.get(i).getWeight()>0 & alimrat.get(i).getDensity(espece, dens)>0 & obj>0) {
					float q=100*obj/((coef/alimrat.get(i).getWeight())*alimrat.get(i).getDensity(espece, dens));
					q=pas*Math.round(q/pas);
					alimrat.get(i).setQuantite(q);}else {
						alimrat.get(i).setQuantite(0);
					}*/
			}}
		return alimrat;
	}
	public List<AlimentRation> getRatObj(List<AlimentRation> alimrat, float obj, NutrientLipid enu, float pas) {
		float coef=getSumWeight(alimrat);
		if (coef!=0) {
			for (int i=0; i<alimrat.size(); i++) {
				if (alimrat.get(i).getWeight()>0 & alimrat.get(i).getNutrient(enu)>0 & obj>0) {
					float q=100*obj/((coef/alimrat.get(i).getWeight())*alimrat.get(i).getNutrient(enu));
					q=pas*Math.round(q/pas);
					alimrat.get(i).setQuantite(q);}else {
						alimrat.get(i).setQuantite(0);
					}
			}}
		return alimrat;
	}
	public void setRef(ReferenceEv ref) {
		this.ref = ref;
	}
	public List<AlimentRation> getRatObj(List<AlimentRation> alimrat, float obj, NutrientMacro enu, float pas) {
		float coef=getSumWeight(alimrat);
		if (coef!=0) {
			for (int i=0; i<alimrat.size(); i++) {
				if (alimrat.get(i).getWeight()>0 & alimrat.get(i).getNutrient(enu)>0 & obj>0) {
					float q=100*obj/((coef/alimrat.get(i).getWeight())*alimrat.get(i).getNutrient(enu));
					q=pas*Math.round(1+q/pas);
					alimrat.get(i).setQuantite(q);}else {
						alimrat.get(i).setQuantite(0);
					}
			}}
		return alimrat;
	}
	
	public Ration reOrder(Ration r,List<AlimentRation> alimrat) {
		
		
		return r ; 
	}
	
}

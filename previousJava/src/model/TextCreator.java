package model;

import java.text.DecimalFormat;

import javax.swing.JLabel;

import Aliments.pourcentPart;
import javafx.scene.control.Tooltip;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
//import affichage.PrincipalPan;

public class TextCreator {

	public TextCreator(){
		
	}
/*	public JLabel refBEE(float value, PrincipalPan pp, NutrientMacro nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientMacro(nutrient, Reflevel.MIN)
						,refer.getNutrientMacro(nutrient, Reflevel.MAX)
						,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientMacro(nutrient, Reflevel.MIN)
				,refer.getNutrientMacro(nutrient, Reflevel.MAX)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientMacroBib(nutrient, Reflevel.MIN)
				,refer.getNutrientMacroBib(nutrient, Reflevel.MAX)
				,refer.getNutrientMacroBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacroBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientMacro(nutrient, Reflevel.MIN)
				,refer.getNutrientMacro(nutrient, Reflevel.MAX)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public JLabel refBEE(float value, PrincipalPan pp, NutrientBase nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientBase(nutrient, Reflevel.MIN)
						,refer.getNutrientBase(nutrient, Reflevel.MAX)
						,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientBase(nutrient, Reflevel.MIN)
				,refer.getNutrientBase(nutrient, Reflevel.MAX)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientBaseBib(nutrient, Reflevel.MIN)
				,refer.getNutrientBaseBib(nutrient, Reflevel.MAX)
				,refer.getNutrientBaseBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBaseBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientBase(nutrient, Reflevel.MIN)
				,refer.getNutrientBase(nutrient, Reflevel.MAX)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public JLabel refBEE(float value, PrincipalPan pp, NutrientVitam nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientVitam(nutrient, Reflevel.MIN)
						,refer.getNutrientVitam(nutrient, Reflevel.MAX)
						,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientVitam(nutrient, Reflevel.MIN)
				,refer.getNutrientVitam(nutrient, Reflevel.MAX)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientVitamBib(nutrient, Reflevel.MIN)
				,refer.getNutrientVitamBib(nutrient, Reflevel.MAX)
				,refer.getNutrientVitamBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitamBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientVitam(nutrient, Reflevel.MIN)
				,refer.getNutrientVitam(nutrient, Reflevel.MAX)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public JLabel refBEE(float value, PrincipalPan pp, NutrientLipid nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientLipid(nutrient, Reflevel.MIN)
						,refer.getNutrientLipid(nutrient, Reflevel.MAX)
						,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientLipid(nutrient, Reflevel.MIN)
				,refer.getNutrientLipid(nutrient, Reflevel.MAX)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientLipidBib(nutrient, Reflevel.MIN)
				,refer.getNutrientLipidBib(nutrient, Reflevel.MAX)
				,refer.getNutrientLipidBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipidBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientLipid(nutrient, Reflevel.MIN)
				,refer.getNutrientLipid(nutrient, Reflevel.MAX)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public JLabel refBEE(float value, PrincipalPan pp, AAEnum nutrient,JLabel lab, boolean comp, Reference refer){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
		System.out.println(nutrient.getLabel()+" "+value);
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+"g/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.OPTIMAX)));
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}

	public JLabel refBEE(float value, PrincipalPan pp, NutrientMin nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientMin(nutrient, Reflevel.MIN)
						,refer.getNutrientMin(nutrient, Reflevel.MAX)
						,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientMin(nutrient, Reflevel.MIN)
				,refer.getNutrientMin(nutrient, Reflevel.MAX)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientMinBib(nutrient, Reflevel.MIN)
				,refer.getNutrientMinBib(nutrient, Reflevel.MAX)
				,refer.getNutrientMinBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMinBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");
	
		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientMin(nutrient, Reflevel.MIN)
				,refer.getNutrientMin(nutrient, Reflevel.MAX)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public JLabel refBEE(float value, PrincipalPan pp, NutrientOther nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value*1000/pp.getBEE())+" "
				+nutrient.getUnite()+"/Mcal"
				+ref.getSign(value*1000/pp.getBEE()
						,refer.getNutrientOther(nutrient, Reflevel.MIN)
						,refer.getNutrientOther(nutrient, Reflevel.MAX)
						,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientOther(nutrient, Reflevel.MIN)
				,refer.getNutrientOther(nutrient, Reflevel.MAX)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientOtherBib(nutrient, Reflevel.MIN)
				,refer.getNutrientOtherBib(nutrient, Reflevel.MAX)
				,refer.getNutrientOtherBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOtherBib(nutrient, Reflevel.OPTIMAX))+"<html> "+participe(nutrient, rat)+"</html>");

		lab.setForeground(ref.getColor(value*1000/pp.getBEE(), 
				refer.getNutrientOther(nutrient, Reflevel.MIN)
				,refer.getNutrientOther(nutrient, Reflevel.MAX)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public String methDesc(AdjustSave AS) {
		String st="<html>";
		int i=1;
		for (TargetDefinition TD: AS.getAll()) {
			st+=" <p> "+ i + ": " +TD.getTarg().nameToString(Lang.FR)+ " " + TD.getPercentCompletion() +"% de "+ TD.getValue() +"g/Mcal";
i++;		
		}
		return st;
	}
	public JLabel refAnalysis(float value, PrincipalPan pp, NutrientAnalysis nutrient,JLabel lab, boolean comp, Reference refer){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			compl=" /!\\";
		}
		String unite="";
		if (nutrient.nameToString().equals(NutrientAnalysis.MethCys.nameToString())||nutrient.nameToString().equals(NutrientAnalysis.PhenTyr.nameToString())){
			unite=nutrient.getUnite()+"/Mcal";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+twoDForm.format(value)+" "
	+unite
				+ref.getSign(value
						,refer.getNutrientAnalysis(nutrient, Reflevel.MIN)
						,refer.getNutrientAnalysis(nutrient, Reflevel.MAX)
						,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMAX))+ " "
				+compl);
		lab.setToolTipText(ref.getNorm(
				refer.getNutrientAnalysis(nutrient, Reflevel.MIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.MAX)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientAnalysisBib(nutrient, Reflevel.MIN)
				,refer.getNutrientAnalysisBib(nutrient, Reflevel.MAX)
				,refer.getNutrientAnalysisBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAnalysisBib(nutrient, Reflevel.OPTIMAX)));
		lab.setForeground(ref.getColor(value, 
				refer.getNutrientAnalysis(nutrient, Reflevel.MIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.MAX)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMAX)));
		return lab;
	}
	public String refTextAnalysis(float value, PrincipalPan pp, NutrientAnalysis nutrient, boolean comp, Reference refer){
		String str=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
	if (!comp){
		compl=" /!\\";
	}
		
		str= " " +ref.getSignPlus(value, refer.getNutrientAnalysis(nutrient, Reflevel.MIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.MAX)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAnalysis(nutrient, Reflevel.OPTIMAX))+ " "+compl;
		return str;
	}
	public String refTextAnalysis(float value, PrincipalPan pp, NutrientVitam nutrient, boolean comp,Reference refer){
		String str=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		String compl="";
		RefValues ref=new RefValues();
	if (!comp){
		compl=" /!\\";
	}
		
		str= " " +ref.getSignPlus(value,refer.getNutrientVitam(nutrient, Reflevel.MIN)
				,refer.getNutrientVitam(nutrient, Reflevel.MAX)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX))+ " "+compl;
		return str;
	}
	public String refTextBEE(float value, PrincipalPan pp, NutrientMin nutrient, boolean comp, Reference refer){
		String str=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
	if (!comp){
		compl=" /!\\";
	}
		
		str= " " +ref.getSignPlus(value,refer.getNutrientMin(nutrient, Reflevel.MIN)
				,refer.getNutrientMin(nutrient, Reflevel.MAX)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX))+ " "+compl;
		return str;
	}
	public String refTextBEE(float value, PrincipalPan pp, NutrientMacro nutrient, boolean comp, Reference refer){
		String str=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
	if (!comp){
		compl=" /!\\";
	}
		
		str= " " +ref.getSignPlus(value, refer.getNutrientMacro(nutrient, Reflevel.MIN)
				,refer.getNutrientMacro(nutrient, Reflevel.MAX)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX))+ " "+compl;
		return str;
	}
	public String refTextBEE(float value, PrincipalPan pp, NutrientLipid nutrient, boolean comp, Reference refer){
		String str=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
	if (!comp){
		compl=" /!\\";
	}
		
		str= " " +ref.getSignPlus(value, refer.getNutrientLipid(nutrient, Reflevel.MIN)
				,refer.getNutrientLipid(nutrient, Reflevel.MAX)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX))+ " "+compl;
		return str;
	}
	public String participe(NutrientBase en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participe(NutrientMacro en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participe(NutrientMin en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participe(NutrientOther en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participe(NutrientVitam en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participeEner(Ration rat, int ener, int dens){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPartEner(ener, dens);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}
	public String participe(NutrientLipid en, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.#");
		pourcentPart[] pourcent=rat.getNutrientPart(en);
		String reponse="";
		pourcentPart p;
		for (int i =pourcent.length-1; i>-1; i--){
			p=pourcent[i];
			reponse+=" <p> "+ p.getName() +" "+twoDForm.format(p.getPourcent())+"%";
		}
		
		return reponse;
	}

/*
 *REF PM 
 *
 */
/*
	public JLabel refPM(float value, PrincipalPan pp, NutrientMacro nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientMacro(nutrient, Reflevel.MIN)
						,refer.getNutrientMacro(nutrient, Reflevel.MAX)
						,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientMacro(nutrient, Reflevel.MIN)
				,refer.getNutrientMacro(nutrient, Reflevel.MAX)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientMacroBib(nutrient, Reflevel.MIN)
				,refer.getNutrientMacroBib(nutrient, Reflevel.MAX)
				,refer.getNutrientMacroBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacroBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientMacro(nutrient, Reflevel.MIN)
				,refer.getNutrientMacro(nutrient, Reflevel.MAX)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMacro(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
	public JLabel refPM(float value, PrincipalPan pp, NutrientBase nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientBase(nutrient, Reflevel.MIN)
						,refer.getNutrientBase(nutrient, Reflevel.MAX)
						,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientBase(nutrient, Reflevel.MIN)
				,refer.getNutrientBase(nutrient, Reflevel.MAX)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientBaseBib(nutrient, Reflevel.MIN)
				,refer.getNutrientBaseBib(nutrient, Reflevel.MAX)
				,refer.getNutrientBaseBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBaseBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientBase(nutrient, Reflevel.MIN)
				,refer.getNutrientBase(nutrient, Reflevel.MAX)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientBase(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
	public JLabel refPM(float value, PrincipalPan pp, NutrientVitam nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientVitam(nutrient, Reflevel.MIN)
						,refer.getNutrientVitam(nutrient, Reflevel.MAX)
						,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientVitam(nutrient, Reflevel.MIN)
				,refer.getNutrientVitam(nutrient, Reflevel.MAX)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientVitamBib(nutrient, Reflevel.MIN)
				,refer.getNutrientVitamBib(nutrient, Reflevel.MAX)
				,refer.getNutrientVitamBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitamBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientVitam(nutrient, Reflevel.MIN)
				,refer.getNutrientVitam(nutrient, Reflevel.MAX)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientVitam(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
	public JLabel refPM(float value, PrincipalPan pp, NutrientLipid nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientLipid(nutrient, Reflevel.MIN)
						,refer.getNutrientLipid(nutrient, Reflevel.MAX)
						,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientLipid(nutrient, Reflevel.MIN)
				,refer.getNutrientLipid(nutrient, Reflevel.MAX)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientLipidBib(nutrient, Reflevel.MIN)
				,refer.getNutrientLipidBib(nutrient, Reflevel.MAX)
				,refer.getNutrientLipidBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipidBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientLipid(nutrient, Reflevel.MIN)
				,refer.getNutrientLipid(nutrient, Reflevel.MAX)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientLipid(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
	public JLabel refPM(float value, PrincipalPan pp, AAEnum nutrient,JLabel lab, boolean comp, Reference refer){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+"g/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmineBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientAcideAmine(nutrient, Reflevel.MIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.MAX)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientAcideAmine(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}

	public JLabel refPM(float value, PrincipalPan pp, NutrientMin nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientMin(nutrient, Reflevel.MIN)
						,refer.getNutrientMin(nutrient, Reflevel.MAX)
						,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientMin(nutrient, Reflevel.MIN)
				,refer.getNutrientMin(nutrient, Reflevel.MAX)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientMinBib(nutrient, Reflevel.MIN)
				,refer.getNutrientMinBib(nutrient, Reflevel.MAX)
				,refer.getNutrientMinBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMinBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");
	
		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientMin(nutrient, Reflevel.MIN)
				,refer.getNutrientMin(nutrient, Reflevel.MAX)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientMin(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
	public JLabel refPM(float value, PrincipalPan pp, NutrientOther nutrient,JLabel lab, boolean comp, Reference refer, Ration rat){
		DecimalFormat twoDForm = new DecimalFormat("#.##");
		
		String complsup="";
		String compl="";
		RefValues ref=new RefValues();
		if (!comp){
			complsup=">";
			compl=" /!\\";
		}
			
		lab.setText(nutrient.nameToString()+" : "
				+complsup
				+twoDForm.format(value/pp.getPM())+" "
				+nutrient.getUnite()+"/kg PM"
				+ref.getSignPM(value/pp.getPM()
						,refer.getNutrientOther(nutrient, Reflevel.MIN)
						,refer.getNutrientOther(nutrient, Reflevel.MAX)
						,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
						,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+ " "
				+compl);
		lab.setToolTipText(ref.getNormPM(
				refer.getNutrientOther(nutrient, Reflevel.MIN)
				,refer.getNutrientOther(nutrient, Reflevel.MAX)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX)
				,refer.getNutrientOtherBib(nutrient, Reflevel.MIN)
				,refer.getNutrientOtherBib(nutrient, Reflevel.MAX)
				,refer.getNutrientOtherBib(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOtherBib(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE())+"<html> "+participe(nutrient, rat)+"</html>");

		lab.setForeground(ref.getColorPM(value/pp.getPM(), 
				refer.getNutrientOther(nutrient, Reflevel.MIN)
				,refer.getNutrientOther(nutrient, Reflevel.MAX)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMIN)
				,refer.getNutrientOther(nutrient, Reflevel.OPTIMAX), pp.getPM(), pp.getBEE()));
		return lab;
	}
*/
}

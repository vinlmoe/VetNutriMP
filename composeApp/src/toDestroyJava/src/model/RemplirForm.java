package model;

import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
/*
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.AcroFields;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfStamper;

*/


public class RemplirForm {
	private DataAccess dat=new DataAccess();
	public RemplirForm(){

	}
/*	public void Remp(Consultation cons, Animal anim, Vet vet, float BE, float opti){
		Document document = new Document();
		try {
			vet=dat.readVet();
			PdfReader reader = new PdfReader("resources/alimTemp.pdf");
			// filling in the form
			PdfStamper stamp1 = new PdfStamper(reader, 
					new FileOutputStream(nameFile ( cons,  anim)));
			AcroFields form1 = stamp1.getAcroFields();
			form1.setField("NomPrescripteur", vet.getNom()+" "+vet.getPrenom());
			form1.setField("Adresse", vet.getAdresse());
			form1.setField("codePostal", vet.getCodePost());
			form1.setField("ville", vet.getVille());
			form1.setField("numOrdre", vet.getOrdre());
			form1.setField("dateOrdonnance", Dater(cons.getDate()));
			form1.setField("lieuOrdonnance", vet.getVille());
			form1.setField("numeroAnimal", anim.getId());
			form1.setField("nomAnimal", anim.getNom());
			form1.setField("poidsAnimal", ""+cons.getPoids());
			form1.setField("nomProprio", anim.getNomProprio());
			form1.setField("Signature", vet.getNom()+" "+vet.getPrenom());
			form1.setField("Intro", Introduire(BE, opti));
			form1.setField("Recette", Recette(cons.getNewRation().get(0)));
			form1.setField("Objectifs", cons.getAutreobserv());
			form1.setField("Prochain", Dater(cons.getPdate()));

			stamp1.close();
			Desktop desk = Desktop.getDesktop();
			desk.open(new File(nameFile ( cons,  anim)));
		} catch (Exception de) {
			de.printStackTrace();
		}
	}
	public void RempAlimAna(Consultation cons, Animal anim, PrincipalPan pp,float BE, float opti, boolean prop, int ratAct){
		Document document = new Document();
		try {
			TextCreator TC=new TextCreator();
			DecimalFormat twoDForm = new DecimalFormat("#.##");
			Ration rat;
			if (prop){
			rat=cons.getNewRation().get(ratAct);}
			else{
				 rat=cons.getPreviousRation();
			}
			PdfReader reader ;
		if (prop){
		 reader = new PdfReader("resources/alimAnap.pdf");
		}else{
			reader = new PdfReader("resources/alimAna.pdf");}
			// filling in the form
			PdfStamper stamp1 = new PdfStamper(reader, 
					new FileOutputStream(nameFileAna ( cons,  anim, prop, ratAct )));
			AcroFields form1 = stamp1.getAcroFields();
			form1.setField("EnergieTot", twoDForm.format(rat.getEnerT(pp.getEspece().getCategorie(), 0))+" kcal");
			form1.setField("InfoAnimal", IntroduireClipRat (BE, cons.getPoids(),opti, cons.getNec().nameToString(), (cons.getK1value()), cons.getK2value(), cons.getK3value(), cons.getK4(), cons.getKx(), anim, pp));
			form1.setField("Ration", Recette(rat));
			form1.setField("version", TextFin());
			form1.setField("Date", anim.getId()+" "+ cons.getDate().getDate()+"/"+cons.getDate().getMonth()+"/"+1900+cons.getDate().getYear());
			form1.setField("BHumidite", twoDForm.format(100*rat.getNutrient(NutrientBase.HUMIDITE)/rat.getPoids()));
			form1.setField("MSHumidite", "-");
			form1.setField("BEEHumidite", "-");
			form1.setField("COMPHumidite"," ");
			form1.setField("BProt", twoDForm.format(100F*rat.getNutrient(NutrientBase.PROTEINE)/(rat.getMasse())));
			form1.setField("MSProt",twoDForm.format(100F*rat.getNutrient(NutrientBase.PROTEINE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEProt", twoDForm.format(rat.getNutrient(NutrientBase.PROTEINE)*1000/pp.getBEE()));
			form1.setField("COMPProt"," ");
			form1.setField("BMG", twoDForm.format(100F*rat.getNutrient(NutrientBase.LIPIDE)/(rat.getMasse())));
			form1.setField("MSMG",twoDForm.format(100F*rat.getNutrient(NutrientBase.LIPIDE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEMG", twoDForm.format(rat.getNutrient(NutrientBase.LIPIDE)*1000/pp.getBEE()));
			form1.setField("COMPMG"," ");
			form1.setField("BCellulose", twoDForm.format(100F*rat.getNutrient(NutrientBase.CELLULOSE)/(rat.getMasse())));
			form1.setField("MSCellulose",twoDForm.format(100F*rat.getNutrient(NutrientBase.CELLULOSE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEECellulose", twoDForm.format(rat.getNutrient(NutrientBase.CELLULOSE)*1000/pp.getBEE()));
			form1.setField("COMPCellulose"," ");
			form1.setField("BENA", twoDForm.format(100F*rat.getNutrient(NutrientBase.ENA)/(rat.getMasse())));
			form1.setField("MSENA",twoDForm.format(100F*rat.getNutrient(NutrientBase.ENA)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEENA", twoDForm.format(rat.getNutrient(NutrientBase.ENA)*1000/pp.getBEE()));
			form1.setField("COMPENA"," ");
			form1.setField("BSucres", twoDForm.format(100F*rat.getNutrient(NutrientBase.SUCRE)/(rat.getMasse())));
			form1.setField("MSSucres",twoDForm.format(100F*rat.getNutrient(NutrientBase.SUCRE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEESucres", twoDForm.format(rat.getNutrient(NutrientBase.SUCRE)*1000/pp.getBEE()));
			form1.setField("COMPSucres"," "+(rat.isNutrient(NutrientBase.AMIDON)?"":"/!\\"));
			form1.setField("BEnergie", twoDForm.format(100F*rat.getEnerT(pp.getEspece().getCategorie(), 0)/(rat.getMasse())));
			form1.setField("MSEnergie",twoDForm.format(100F*rat.getEnerT(pp.getEspece().getCategorie(), 0)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEEnergie", twoDForm.format(rat.getEnerT(pp.getEspece().getCategorie(), 0)*1000/pp.getBEE()));
			form1.setField("COMPEnergie"," ");
			form1.setField("BP", twoDForm.format(100F*rat.getNutrient(NutrientMacro.PHOS)/(rat.getMasse())));
			form1.setField("MSP",twoDForm.format(100F*rat.getNutrient(NutrientMacro.PHOS)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEP", twoDForm.format(rat.getNutrient(NutrientMacro.PHOS)*1000/pp.getBEE()));
			form1.setField("COMPP",""+TC.refTextBEE(rat.getNutrient(NutrientMacro.PHOS)*1000/pp.getBEE(), pp, NutrientMacro.PHOS, rat.isNutrient(NutrientMacro.PHOS), pp.getReference()));
			form1.setField("BCa", twoDForm.format(100F*rat.getNutrient(NutrientMacro.CAL)/(rat.getMasse())));
			form1.setField("MSCa",twoDForm.format(100F*rat.getNutrient(NutrientMacro.CAL)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEECa", twoDForm.format(rat.getNutrient(NutrientMacro.CAL)*1000/pp.getBEE()));
			form1.setField("COMPCa",""+TC.refTextBEE(rat.getNutrient(NutrientMacro.CAL)*1000/pp.getBEE(), pp, NutrientMacro.CAL,  rat.isNutrient(NutrientMacro.CAL), pp.getReference()));
			form1.setField("BMg", twoDForm.format(100F*rat.getNutrient(NutrientMacro.MG)/(rat.getMasse())));
			form1.setField("MSMg",twoDForm.format(100F*rat.getNutrient(NutrientMacro.MG)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEMg", twoDForm.format(rat.getNutrient(NutrientMacro.MG)*1000/pp.getBEE()));
			form1.setField("COMPMg",""+TC.refTextBEE(rat.getNutrient(NutrientMacro.MG)*1000/pp.getBEE(), pp, NutrientMacro.MG,  rat.isNutrient(NutrientMacro.MG), pp.getReference()));
			form1.setField("BNa", twoDForm.format(100F*rat.getNutrient(NutrientMacro.NA)/(rat.getMasse())));
			form1.setField("MSNa",twoDForm.format(100F*rat.getNutrient(NutrientMacro.NA)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEENa", twoDForm.format(rat.getNutrient(NutrientMacro.NA)*1000/pp.getBEE()));
			form1.setField("COMPNa",""+TC.refTextBEE(rat.getNutrient(NutrientMacro.NA)*1000/pp.getBEE(), pp, NutrientMacro.NA,  rat.isNutrient(NutrientMacro.NA), pp.getReference()));
			form1.setField("BK", twoDForm.format(100F*rat.getNutrient(NutrientMacro.K)/(rat.getMasse())));
			form1.setField("MSK",twoDForm.format(100F*rat.getNutrient(NutrientMacro.K)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEK", twoDForm.format(rat.getNutrient(NutrientMacro.K)*1000/pp.getBEE()));
			form1.setField("COMPK",""+TC.refTextBEE(rat.getNutrient(NutrientMacro.K)*1000/pp.getBEE(), pp, NutrientMacro.K,  rat.isNutrient(NutrientMacro.K), pp.getReference()));
			form1.setField("BO3", twoDForm.format(100F*rat.getNutrient(NutrientLipid.O3)/(rat.getMasse())));
			form1.setField("MSO3",twoDForm.format(100F*rat.getNutrient(NutrientLipid.O3)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEO3", twoDForm.format(rat.getNutrient(NutrientLipid.O3)*1000/pp.getBEE()));
			form1.setField("COMPO3",""+TC.refTextBEE(rat.getNutrient(NutrientLipid.O3)*1000/pp.getBEE(), pp, NutrientLipid.O3, rat.isNutrient(NutrientLipid.O3), pp.getReference()));
			form1.setField("BO6", twoDForm.format(100F*rat.getNutrient(NutrientLipid.O6)/(rat.getMasse())));
			form1.setField("MSO6",twoDForm.format(100F*rat.getNutrient(NutrientLipid.O6)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEO6", twoDForm.format(rat.getNutrient(NutrientLipid.O6)*1000/pp.getBEE()));
			form1.setField("COMPO6",""+TC.refTextBEE(rat.getNutrient(NutrientLipid.O6)*1000/pp.getBEE(), pp, NutrientLipid.O6, rat.isNutrient(NutrientLipid.O6), pp.getReference()));
			form1.setField("BEPA", twoDForm.format(100F*rat.getNutrient(NutrientLipid.EPADHA)/(rat.getMasse())));
			form1.setField("MSEPA",twoDForm.format(100F*rat.getNutrient(NutrientLipid.EPADHA)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEEPA", twoDForm.format(rat.getNutrient(NutrientLipid.EPADHA)*1000/pp.getBEE()));
			form1.setField("COMPEPA","");
			form1.setField("BAA", twoDForm.format(100F*rat.getNutrient(NutrientLipid.AG204)/(rat.getMasse())));
			form1.setField("MSAA",twoDForm.format(100F*rat.getNutrient(NutrientLipid.AG204)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEAA", twoDForm.format(rat.getNutrient(NutrientLipid.AG204)*1000/pp.getBEE()));
			form1.setField("COMPAA","");
			
			form1.setField("BA", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITA)/(rat.getMasse())));
			form1.setField("MSA",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITA)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEA", twoDForm.format(rat.getNutrient(NutrientVitam.VITA)*1000/pp.getBEE()));
			form1.setField("COMPA",""+TC.refTextAnalysis(rat.getNutrient(NutrientVitam.VITA)*1000/pp.getBEE(), pp, NutrientVitam.VITA, rat.isNutrient(NutrientVitam.VITA), pp.getReference()));
			form1.setField("BD", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITD)/(rat.getMasse())));
			form1.setField("MSD",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITD)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEED", twoDForm.format(rat.getNutrient(NutrientVitam.VITD)*1000/pp.getBEE()));
			form1.setField("COMPD",""+TC.refTextAnalysis(rat.getNutrient(NutrientVitam.VITD)*1000/pp.getBEE(), pp, NutrientVitam.VITD, rat.isNutrient(NutrientVitam.VITD), pp.getReference()));
			form1.setField("BB1", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB1)/(rat.getMasse())));
			form1.setField("MSB1",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB1)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB1", twoDForm.format(rat.getNutrient(NutrientVitam.VITB1)*1000/pp.getBEE()));
			form1.setField("COMPB1",""+(rat.isNutrient(NutrientVitam.VITB1)?"":"/!\\"));
			form1.setField("BB2", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB2)/(rat.getMasse())));
			form1.setField("MSB2",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB2)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB2", twoDForm.format(rat.getNutrient(NutrientVitam.VITB2)*1000/pp.getBEE()));
			form1.setField("COMPB2",""+(rat.isNutrient(NutrientVitam.VITB2)?"":"/!\\"));
			form1.setField("BB3", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB3)/(rat.getMasse())));
			form1.setField("MSB3",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB3)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB3", twoDForm.format(rat.getNutrient(NutrientVitam.VITB3)*1000/pp.getBEE()));
			form1.setField("COMPB3",""+(rat.isNutrient(NutrientVitam.VITB3)?"":"/!\\"));
			form1.setField("BB5", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB5)/(rat.getMasse())));
			form1.setField("MSB5",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB5)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB5", twoDForm.format(rat.getNutrient(NutrientVitam.VITB5)*1000/pp.getBEE()));
			form1.setField("COMPB5",""+(rat.isNutrient(NutrientVitam.VITB5)?"":"/!\\"));
			form1.setField("BB6", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB6)/(rat.getMasse())));
			form1.setField("MSB6",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB6)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB6", twoDForm.format(rat.getNutrient(NutrientVitam.VITB6)*1000/pp.getBEE()));
			form1.setField("COMPB6",""+(rat.isNutrient(NutrientVitam.VITB6)?"":"/!\\"));
			form1.setField("BB8", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB8)/(rat.getMasse())));
			form1.setField("MSB8",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB8)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB8", twoDForm.format(rat.getNutrient(NutrientVitam.VITB8)*1000/pp.getBEE()));
			form1.setField("COMPB8",""+(rat.isNutrient(NutrientVitam.VITB8)?"":"/!\\"));

			form1.setField("BB12", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB12)/(rat.getMasse())));
			form1.setField("MSB12",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITB12)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEB12", twoDForm.format(rat.getNutrient(NutrientVitam.VITB12)*1000/pp.getBEE()));
			form1.setField("COMPB12",""+(rat.isNutrient(NutrientVitam.VITB12)?"":"/!\\"));
			form1.setField("BE", twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITE)/(rat.getMasse())));
			form1.setField("MSE",twoDForm.format(100F*rat.getNutrient(NutrientVitam.VITE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEE", twoDForm.format(rat.getNutrient(NutrientVitam.VITE)*1000/pp.getBEE()));
			form1.setField("COMPE",""+(rat.isNutrient(NutrientVitam.VITE)?"":"/!\\"));
			form1.setField("BZn", twoDForm.format(100F*rat.getNutrient(NutrientMin.ZN)/(rat.getMasse())));
			form1.setField("MSZn",twoDForm.format(100F*rat.getNutrient(NutrientMin.ZN)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEZn", twoDForm.format(rat.getNutrient(NutrientMin.ZN)*1000/pp.getBEE()));
			form1.setField("COMPZn",""+TC.refTextBEE(rat.getNutrient(NutrientMin.ZN)*1000/pp.getBEE(), pp, NutrientMin.ZN, rat.isNutrient(NutrientMin.ZN), pp.getReference()));
			form1.setField("BFe", twoDForm.format(100F*rat.getNutrient(NutrientMin.FE)/(rat.getMasse())));
			form1.setField("MSFe",twoDForm.format(100F*rat.getNutrient(NutrientMin.FE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEFe", twoDForm.format(rat.getNutrient(NutrientMin.FE)*1000/pp.getBEE()));
			form1.setField("COMPFe",""+TC.refTextBEE(rat.getNutrient(NutrientMin.FE)*1000/pp.getBEE(), pp, NutrientMin.FE, rat.isNutrient(NutrientMin.FE), pp.getReference()));
			form1.setField("BCu", twoDForm.format(100F*rat.getNutrient(NutrientMin.CU)/(rat.getMasse())));
			form1.setField("MSCu",twoDForm.format(100F*rat.getNutrient(NutrientMin.CU)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEECu", twoDForm.format(rat.getNutrient(NutrientMin.CU)*1000/pp.getBEE()));
			form1.setField("COMPCu",""+TC.refTextBEE(rat.getNutrient(NutrientMin.CU)*1000/pp.getBEE(), pp, NutrientMin.CU, rat.isNutrient(NutrientMin.I), pp.getReference()));
			form1.setField("BI", twoDForm.format(100F*rat.getNutrient(NutrientMin.I)/(rat.getMasse())));
			form1.setField("MSI",twoDForm.format(100F*rat.getNutrient(NutrientMin.I)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEEI", twoDForm.format(rat.getNutrient(NutrientMin.I)*1000/pp.getBEE()));
			form1.setField("COMPI",""+TC.refTextBEE(rat.getNutrient(NutrientMin.I)*1000/pp.getBEE(), pp, NutrientMin.I, rat.isNutrient(NutrientMin.I), pp.getReference()));
			form1.setField("BSe", twoDForm.format(100F*rat.getNutrient(NutrientMin.SE)/(rat.getMasse())));
			form1.setField("MSSe",twoDForm.format(100F*rat.getNutrient(NutrientMin.SE)/(rat.getMasse()-rat.getNutrient(NutrientBase.HUMIDITE))));
			form1.setField("BEESe", twoDForm.format(rat.getNutrient(NutrientMin.SE)*1000/pp.getBEE()));
			form1.setField("COMPSe",""+TC.refTextBEE(rat.getNutrient(NutrientMin.SE)*1000/pp.getBEE(), pp, NutrientMin.SE, rat.isNutrient(NutrientMin.SE), pp.getReference()));
			form1.setField("BZnCu", twoDForm.format(rat.getNutrient(NutrientMin.ZN)/(rat.getNutrient(NutrientMin.CU))));
			form1.setField("MSZnCu",twoDForm.format(rat.getNutrient(NutrientMin.ZN)/(rat.getNutrient(NutrientMin.CU))));
			form1.setField("BEEZnCu", twoDForm.format(rat.getNutrient(NutrientMin.ZN)/(rat.getNutrient(NutrientMin.CU))));
			form1.setField("COMPZnCu",""+TC.refTextAnalysis((float)(rat.getNutrient(NutrientMin.ZN)/(rat.getNutrient(NutrientMin.CU))), pp, NutrientAnalysis.ZnCu,rat.isNutrient(NutrientMin.ZN)&rat.isNutrient(NutrientMin.CU), pp.getReference()));
			form1.setField("BNaK", twoDForm.format(rat.getNutrient(NutrientMacro.K)/(1000*(rat.getNutrient(NutrientMacro.NA)))));
			form1.setField("MSNaK",twoDForm.format(rat.getNutrient(NutrientMacro.K)/(1000*(rat.getNutrient(NutrientMacro.NA)))));
			form1.setField("BEENaK", twoDForm.format(rat.getNutrient(NutrientMacro.K)/(1000*(rat.getNutrient(NutrientMacro.NA)))));
			form1.setField("COMPNaK",""+TC.refTextAnalysis((float)(rat.getNutrient(NutrientMacro.K)/(1000*(rat.getNutrient(NutrientMacro.NA)))), pp, NutrientAnalysis.NaK, rat.isNutrient(NutrientMacro.K)&rat.isNutrient(NutrientMacro.NA), pp.getReference()));
			form1.setField("BO6O3", twoDForm.format(rat.getNutrient(NutrientLipid.O6)/(rat.getNutrient(NutrientLipid.O3))));
			form1.setField("MSO6O3",twoDForm.format(rat.getNutrient(NutrientLipid.O6)/(rat.getNutrient(NutrientLipid.O3))));
			form1.setField("BEEO6O3", twoDForm.format(rat.getNutrient(NutrientLipid.O6)/(rat.getNutrient(NutrientLipid.O3))));
			form1.setField("COMPO6O3",""+TC.refTextAnalysis((float)(rat.getNutrient(NutrientLipid.O6)/(rat.getNutrient(NutrientLipid.O3))), pp, NutrientAnalysis.o6o3,rat.isNutrient(NutrientLipid.O6)&rat.isNutrient(NutrientLipid.O3), pp.getReference()));
			form1.setField("BCaP", twoDForm.format(rat.getNutrient(NutrientMacro.CAL)/(rat.getNutrient(NutrientMacro.PHOS))));
			form1.setField("MSCaP",twoDForm.format(rat.getNutrient(NutrientMacro.CAL)/(rat.getNutrient(NutrientMacro.PHOS))));
			form1.setField("BEECaP", twoDForm.format(rat.getNutrient(NutrientMacro.CAL)/(rat.getNutrient(NutrientMacro.PHOS))));
			form1.setField("COMPCaP",""+TC.refTextAnalysis((float)(rat.getNutrient(NutrientMacro.CAL)/(rat.getNutrient(NutrientMacro.PHOS))), pp, NutrientAnalysis.PCa, rat.isNutrient(NutrientMacro.CAL)&rat.isNutrient(NutrientMacro.PHOS), pp.getReference()));
			
			
			stamp1.close();
			Desktop desk = Desktop.getDesktop();
			desk.open(new File(nameFileAna ( cons,  anim, prop ,ratAct )));
		} catch (Exception de) {
			de.printStackTrace();
		}
	}
	private String nameFile (Consultation cons, Animal anim){
		String res=new String("");
		res="ordo/"+cons.getDate().getYear()+""+cons.getDate().getMonth()+""+cons.getDate().getDate()+anim.getNom()+".pdf";

		return res;
	}
	private String nameFileAna (Consultation cons, Animal anim, boolean prop, int r){
		String res=new String("");
		if (prop){
			res="ordo/"+cons.getDate().getYear()+""+cons.getDate().getMonth()+""+cons.getDate().getDate()+anim.getNom()+"act.pdf";
		}else{
			res="ordo/"+cons.getDate().getYear()+""+cons.getDate().getMonth()+""+cons.getDate().getDate()+anim.getNom()+r+"prop.pdf";
		}

		return res;
	}
	private String Dater (Date d){
		String res=new String("");
		int month=d.getMonth()+1;
		int year=1900+d.getYear();
		res=d.getDate()+"/"+month+"/"+year;

		return res;
	}
	private String Introduire (float BE, float opti){
		String res=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		res="La consultation clinique a permis d??estimer le besoin ??nerg??tique journalier ?? "+Math.floor(BE)+" kcal et le poids optimal ?? "+ twoDForm.format(opti)+" kg.\n"+
				"En accord avec le propri??taire une ration est prescrite, elle est compos??e de :";


		return res;
	}*/
	private String IntroduireClip (float BE, float poids,float pid, String nec, float k1, float k2, float k3, float k4, float k5){
		String res=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");

		res="Le poids de l'animal est de "+poids+" kg. Sa note d'état corporel est estimée à :"+nec+", le poids idéal est estimé à "+pid +" kg \n "
				+ "Le besoin énergétique journalier a été estimé à "+Math.floor(BE)+" kcal, en appliquant les coefficients suivants :  \n k1 (race)= "+ k1+"\n k2 (comportement)= "+k2+"\n k3 (stade physiologique)= "+k3+"\n k4 (affection e"
						+ "éventuelle)="+k4+"\n k5 (autre)="+k5+".\n"+
				"\n Sa ration actuelle est :";


		return res;
	}/*
	private String IntroduireClipRat (float BE, float poids, float pid,String nec, float k1, float k2, float k3, float k4, float k5, Animal anim, PrincipalPan pp){
		String res=new String("");
		DecimalFormat twoDForm = new DecimalFormat("#.##");
String Source=new String ("");
if(pp.getEspece().equals(Espece.CHAT)){
	Source="(NRC, 2006)";
}
else {
	if (pp.getPreferedEquation()==0){
		Source="(Blanchard, 2008)";}
		else{
			Source="(NRC, 2006)";
		}
	
}
		res= anim.nom+" "+ anim.getEspece().nameToString()+ " "+ anim.getSex().nameToString()+" dont le poids est de "+poids+" kg et sa note d'??tat corporel est estim??e ?? :"+nec+", le poids id??al est estim?? ?? "+pid +" kg \n "
				+ "Son besoin ??nerg??tique journalier a ??t?? estim?? ?? "+Math.floor(BE)+" kcal "+Source+", en appliquant les coefficients suivants :  \n k1 (race)= "+ k1+"\n k2 (comportement)= "+k2+"\n k3 (stade physiologique)= "+k3+"\n k4 (affection ??ventuelle)="+k4+"\n k5 (autre)="+k5+".\n"
				;


		return res;
	}*/
	private String Recette (Ration rat){
		String res=new String("");
		if (rat!=null){

			for (int i=0; i<rat.getAlimentList().size();i++){
				if(rat.getAlimentList().get(i).getType().getCoef()==1){
					res+=rat.getAlimentList().get(i).getNom()+ "               "+rat.getAlimentList().get(i).getQuantite()+" g/jour \n";
				}
				else{
					res+= rat.getAlimentList().get(i).getMarque()+" "+rat.getAlimentList().get(i).getGamme()+" "+ rat.getAlimentList().get(i).getNom()+ "              "+rat.getAlimentList().get(i).getQuantite()+" g/jour \n";
				}
			}	
		}	
		return res;
	}
	public void ClipText(ConsultationEv cons, AnimalEv anim, Vet vet, float BE, float opti){
		String clip=new String("");
		try {
			vet=dat.readVet();
			clip+=cons.getCRendu()+"\n";
			
			
		
				clip+=IntroduireClip(BE, cons.getPoids(),opti,cons.getBcs(),cons.getK1value(), cons.getK2value(), cons.getK3value(), cons.getK4value(), cons.getK5value());

			for (Ration r:cons.getRationList()) {
				if (!r.isActual()) {
					clip+=Recette(r);
				}
			}
		int i=1;
		
		
		
			clip+="\n En accord avec le propriétaire une ration est prescrite, elle est composée de :";
			
			for (Ration r:cons.getRationList()) {
			
				if (r.isActual()) {
					clip+="\n Ration "+i+" :\n";
					clip+=Recette(r);
					i++;
				}
			}
		
			Toolkit toolKit = Toolkit.getDefaultToolkit();
			Clipboard cb = toolKit.getSystemClipboard();
			cb.setContents(new StringSelection(clip), null);
		} catch (Exception de) {
			de.printStackTrace();
		}
		
	}
	/*
	public String TextFin(){
	
		String str=new String();
		str="Ration analys??e ?? l'aide du logiciel VetNutri version " +TextConstant.VERSION.nameToString()+" et de la base CIQUAL 2016 pour les aliments m??nagers. Cette estimation est uniquement fournie ?? titre informatif."
				+ "++ : Apport tr??s ??lev??; + : apport sup??rieur ?? la normale; - : apport inf??rieur ?? la normale; -- : apport tr??s faible; /!\\ : valeurs manquantes pour au moins un aliment";
				return str;
		
	}*/
}

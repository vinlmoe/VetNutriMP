package model;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;

import com.itextpdf.text.pdf.PdfPageEventHelper;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;
import com.itextpdf.*;

import org.jfree.chart.JFreeChart;

import com.itextpdf.text.Anchor;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chapter;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Section;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEvent;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import DataStruct.CurveP;
import DataStruct.LineWeight;
import Enumerise.ContEnum;
import Enumerise.FoodKind;
import Enumerise.MainNutrientEnum;
import Enumerise.NutrientBase;
import application.DataConnector;
import application.TextConstant;
import equation.RequirementAnalyzer;
import graph.component.Chart;
import graph.component.ChartWeight;
import graph.component.LabelData;

public class PdfExport {
private static Vet vet;
	private static String bddNames="";

	static Font catFont = new Font(Font.FontFamily.HELVETICA, 18,
			Font.BOLD);
	static Font subFont = new Font(Font.FontFamily.HELVETICA, 16,
			Font.BOLD);
	static Font Bfont = new Font(Font.FontFamily.HELVETICA, 8,
			Font.BOLD);
	static Font font = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.BLACK);

	static Font tabfont = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);
	
public PdfExport() {
	// TODO Auto-generated constructor stub
}

	public void createAnimalPdf(AnimalEv anim, ConsultationEv cons, RationCalculator calc, ResourceBundle resource, Vet vet
			) {
		// 1. Create document
		this.vet=vet;
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		String name=(DataConnector.isWindows()?"":"../")+"Prescription/Consultations_"+anim.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+anim.getId().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+cons.getDate()+"_"+cons.getObjet().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+".pdf";

		// 2. Create PdfWriter
		try {
			PdfWriter writer=PdfWriter.getInstance(document, new FileOutputStream(name));

			HeaderAndFooter hAf=new HeaderAndFooter("NALE");
			 writer.setPageEvent(hAf);
			 // 3. Open document
			document.open();
			Paragraph preface = new Paragraph();
			// We add one empty line
			Paragraph title    =  new Paragraph(resource.getString("rationof")+" "+anim.getNom()	, catFont);

			title.setAlignment(Element.ALIGN_CENTER);
			Paragraph num    =  new Paragraph("N° "+anim.getId()	, catFont);

			num.setAlignment(Element.ALIGN_CENTER);

			Paragraph dat    =  new Paragraph("Date :"+cons.getDate()	, catFont);

			dat.setAlignment(Element.ALIGN_CENTER);
			// Lets write a big header


			document.add(title);
			document.add(num);
			document.add(dat);


			// Lets write a big header



			addEmptyLine(preface, 4);
			preface .setAlignment(Element.ALIGN_CENTER);
			document.add(preface);
			Paragraph animPres = new Paragraph();
			animPres.add(new Paragraph(resource.getString("ownerName")+": "+anim.getNomProprio()	, font));
			animPres.add(new Paragraph(resource.getString("specie")+": "+resource.getString(Espece.getEnumFromStringId(anim.getEspece()).getName())	, font));
			animPres.add(new Paragraph(resource.getString("birthDate")+": "+anim.getDateNaiss()	, font));
			animPres.add(new Paragraph(resource.getString("Body_weight")+": "+cons.getPoids() +" kg"	, font));
			if ( cons.getPoidsIdeal()>0) {
				animPres.add(new Paragraph(resource.getString("Ideal_body_weight")+": "+cons.getPoidsIdeal()+" kg", font));}
			document.add(animPres);
			// 4. Add content

			Chapter catPart = new Chapter(new Paragraph(resource.getString("actualDiet"), catFont), 1);


			Chapter catPartn = new Chapter(new Paragraph(resource.getString("proposedDiet"), catFont), 2);
			int i=1;
			int inew=1;

			for(Ration rat:cons.getRationList()) {

				if (!rat.isActual()) {
					descrimeRation( cons,  rat, i,   catPart, resource) ;
					i++;
				}else {
					descrimeRation( cons,  rat,  inew,   catPartn, resource); 
					inew++;
				}
			}
			document.add(catPart);
			document.add(catPartn);



			// 5. Close document
			document.close();

			Desktop desk = Desktop.getDesktop();
			desk.open(new File(name));
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public void createOrdonnancePdf(AnimalEv anim, ConsultationEv cons, ArrayList<Ration>rl, ArrayList<Advise> al, Vet vet,ResourceBundle resource
			) {
		// 1. Create document
		this.vet=vet;
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		String name=(DataConnector.isWindows()?"":"../")+"Prescription/Ordonnance_"+anim.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+anim.getId().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+cons.getDate()+"_"+(cons.getObjet()==null?"no":cons.getObjet().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", ""))+".pdf";

		// 2. Create PdfWriter
		try {
			PdfWriter writer=PdfWriter.getInstance(document, new FileOutputStream(name));

			HeaderAndFooter hAf=new HeaderAndFooter("NALE");
			 writer.setPageEvent(hAf);
			// 3. Open document
			document.open();
			Paragraph preface = new Paragraph();
			preface.add(new Paragraph("Dr. Vet. "+vet.getPrenom()+" "+vet.getNom(),font));
			if(!vet.getOrdre().isBlank() ) { preface.add(new Paragraph("N° "+vet.getOrdre(),font));}
			preface.add(new Paragraph(vet.getAdresse(),font));
			preface.add(new Paragraph(vet.getCodePost()+" "+vet.getVille(),font));
			addEmptyLine(preface, 2);
			// We add one empty line

			preface .setAlignment(Element.ALIGN_LEFT);
			document.add(preface);
		ArrayList<Paragraph> animPres =new ArrayList<Paragraph>();
			animPres.add(new Paragraph(resource.getString("rationof")+" "+anim.getNom()	, Bfont));
			animPres.add(new Paragraph("N°"+" "+anim.getId()	, Bfont));
		
			animPres.add(new Paragraph(resource.getString("ownerName")+": "+anim.getNomProprio()	, font));
			animPres.add(new Paragraph(resource.getString("specie")+": "+resource.getString(Espece.getEnumFromStringId(anim.getEspece()).getName())	, font));
			animPres.add(new Paragraph(resource.getString("birthDate")+": "+anim.getDateNaiss()	, font));
			animPres.add(new Paragraph(resource.getString("Body_weight")+": "+cons.getPoids() +" kg"	, font));
			if ( cons.getPoidsIdeal()>0) {
				animPres.add(new Paragraph(resource.getString("Ideal_body_weight")+": "+cons.getPoidsIdeal()+" kg", font));}

			animPres.add( new Paragraph("Date :"+cons.getDate()	, font));
			animPres.forEach((n)->n.setAlignment(Element.ALIGN_RIGHT));
			animPres.forEach((n)->{
				try {
					document.add(n);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
		
			// 4. Add content
			Paragraph catPart = new Paragraph();
			int i=1;
			for(Ration rat:rl) {
				descrimeRation( cons,  rat, i,   catPart, resource) ;
				i++;
			}
			catPart.add(new Paragraph(" "));
			catPart.add(new Paragraph(" "));
			document.add(catPart);
			ArrayList<Paragraph> adPart =new ArrayList<Paragraph>();
	 adPart.add(new Paragraph(resource.getString("Advise"), subFont));
			for (Advise ad:al) {
				adPart.add(new Paragraph(ad.getText(), font));
			}
			adPart.forEach((n)->n.setAlignment(Element.ALIGN_JUSTIFIED));
			adPart.add(new Paragraph(" "));
			adPart.add(new Paragraph(" "));
			adPart.add(new Paragraph(" "));
			adPart.forEach((n)->{
				try {
					document.add(n);
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			});
			
			Paragraph Signature =new Paragraph("Dr. Vet. "+vet.getNom()+"                ", subFont);
		
			Signature.setAlignment(Element.ALIGN_RIGHT);
			document.add(Signature);
			// 5. Close document
			document.close();
		
			Desktop desk = Desktop.getDesktop();
			desk.open(new File(name));
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	public  void createRationPdf(AnimalEv anim, ConsultationEv cons, Ration rat, RationCalculator calc, 
	
			ReferenceEv ref, 
			RequirementAnalyzer ra,
			LabelData [] labBEE, 
			LabelData [] labBW, 
			LabelData [] labMW, 
			LabelData [] labNeed, 
			LabelData [] labEner, 
			ResourceBundle resource,
			Vet vet
			

			) {
		this.vet=vet;
		// 1. Create document
		PdfWriter writer=null;
		Document document = new Document(PageSize.A4, 50, 50, 50, 50);
		String name=(DataConnector.isWindows()?"":"../")+"Prescription/Ration_"+anim.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+anim.getId().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+cons.getDate()+"_"+rat.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+".pdf";

		try {

		writer=PdfWriter.getInstance(document, new FileOutputStream(name));

			HeaderAndFooter hAf=new HeaderAndFooter("NALE");
			 writer.setPageEvent(hAf);	// 3. Open document
			document.open();
			Paragraph preface = new Paragraph();
			// We add one empty line
			Paragraph title    =  new Paragraph(resource.getString("rationof")+" "+anim.getNom()	, catFont);

			title.setAlignment(Element.ALIGN_CENTER);
			Paragraph num    =  new Paragraph("N° "+anim.getId()	, catFont);

			num.setAlignment(Element.ALIGN_CENTER);

			Paragraph dat    =  new Paragraph("Date : "+cons.getDate()	, catFont);

			dat.setAlignment(Element.ALIGN_CENTER);
			// Lets write a big header


			document.add(title);
			document.add(num);
			document.add(dat);


			// Lets write a big header



			addEmptyLine(preface, 4);
			preface .setAlignment(Element.ALIGN_CENTER);
			document.add(preface);
			Paragraph animPres = new Paragraph();
			animPres.add(new Paragraph(resource.getString("ownerName")+": "+anim.getNomProprio()	, font));
			animPres.add(new Paragraph(resource.getString("specie")+": "+resource.getString(Espece.getEnumFromStringId(anim.getEspece()).getName())	, font));
			animPres.add(new Paragraph(resource.getString("birthDate")+": "+anim.getDateNaiss()	, font));
			animPres.add(new Paragraph(resource.getString("Body_weight")+": "+cons.getPoids() +" kg"	, font));
			if ( cons.getPoidsIdeal()>0) {
				animPres.add(new Paragraph(resource.getString("Ideal_body_weight")+": "+cons.getPoidsIdeal()+" kg", font));}


			for(LabelData l:labNeed) {
				animPres.add(new Paragraph(l.getText(), font));
			}



			document.add(animPres);
			// 4. Add content

			Chapter catPart = new Chapter(new Paragraph(resource.getString("diet"), catFont),1);
			int k=5;

			int width=230*k;
			int height=150*k;



			descrimeRation( cons,  rat,   catPart, resource) ;
			catPart.add(new Paragraph(" "));
			for(LabelData l:labEner) {
				catPart.add(new Paragraph(l.getText(), font));
			}
			catPart.add(new Paragraph(" "));


			Phrase p=new Phrase();
			Chart chartCreator=new Chart(resource);









			float EnerAtwatModif= (float) (rat.getNutrient(NutrientBase.ENA)*3.5+rat.getNutrient(NutrientBase.PROTEINE)*3.5+rat.getNutrient(NutrientBase.LIPIDE)*8.5);
			JFreeChart cart=(chartCreator.EnerPie(350*rat.getNutrient(NutrientBase.ENA)/EnerAtwatModif, 350*rat.getNutrient(NutrientBase.PROTEINE)/EnerAtwatModif, 850*rat.getNutrient(NutrientBase.LIPIDE)/EnerAtwatModif, true,k));
			BufferedImage bufferedImage = cart.createBufferedImage(width, height);
			Image image  = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));


			cart=chartCreator.CompoPie(rat.getNutrient(NutrientBase.HUMIDITE), rat.getNutrient(NutrientBase.PROTEINE), rat.getNutrient(NutrientBase.LIPIDE), rat.getNutrient(NutrientBase.ENA), rat.getNutrient(NutrientBase.CELLULOSE), rat.getNutrient(NutrientBase.CENDRE), true,k);
			bufferedImage = cart.createBufferedImage(width, height);
			image = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));

			cart=    chartCreator.BasePlot(rat, calc, ra, ref, true,k);
			bufferedImage = cart.createBufferedImage(width, height);
			image = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));
			cart=    chartCreator.MacroPlot(rat, calc, ra, ref, true,k);
			bufferedImage = cart.createBufferedImage(width, height);
			image = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));
			cart=    chartCreator.MinPlot(rat, calc, ra, ref, true,k);
			bufferedImage = cart.createBufferedImage(width, height);
			image = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));
			cart=    chartCreator.VitaPlot(rat, calc, ra, ref, true,k);
			bufferedImage = cart.createBufferedImage(width, height);
			image = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(100f/k);
			p.add(new Chunk(image, 0, 0, true));


			// 5. Close document
			catPart.add(p);


			catPart.add(new Paragraph(" "));
			catPart.add(tableIntake(labBEE, labBW,labMW,  rat,  resource));


			document.add(catPart);

			document.close();


			Desktop desk = Desktop.getDesktop();
			desk.open(new File(name));
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	private static void addEmptyLine(Paragraph paragraph, int number) {
		for (int i = 0; i < number; i++) {
			paragraph.add(new Paragraph(" "));
		}
	}

	private static void descrimeRation(ConsultationEv cons, Ration rat, int i,  Chapter catPart, ResourceBundle resource) {
		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
		Paragraph subPara = new Paragraph(resource.getString("diet")+ " "+i+": "+rat.getNom(), subFont);
		Section subCatPart = catPart.addSection(subPara);
		Chunk bullet = new Chunk("\u2022");
		List list = new List(true, false, 10);
		for(AlimentRation al:rat.getAlimentList()) {
			if (al.getAlim().getCont().equals(ContEnum.NO)|al.getQuantite()<0.01) {
				subCatPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + 
						((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
						((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
						     +  ": "+
						al.getQuantite() +"g" +"/"+resource.getString("Jour")
						, font));}else {
							subCatPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + 
									((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
									((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
											      + ": "+
									al.getQuantite() +"g "+"/"+resource.getString("Jour")+" ("+ twoDForm.format(al.getQuantite()/al.getAlim().getQuantInt())+" "+resource.getString(al.getAlim().getCont().getName())+ (al.getQuantite()/al.getAlim().getQuantInt()>1?"s":"")+")"
									, font));
						}
			bddNames+= (bddNames.contains(vet.getAlDBL().getNom(al.getAlim().getDataB())))?"":( ", " + vet.getAlDBL().getNom(al.getAlim().getDataB()));
					}

	}
	private static void descrimeRation(ConsultationEv cons, Ration rat, int i,  Paragraph catPart, ResourceBundle resource) {
		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
		addEmptyLine(catPart,1);
		Paragraph subPara = new Paragraph(resource.getString("diet")+ " "+i+": "+rat.getNom(), subFont);
		catPart.add(subPara);
		Chunk bullet = new Chunk("\u2022");
		List list = new List(true, false, 10);
		for(AlimentRation al:rat.getAlimentList()) {
			if (al.getAlim().getCont().equals(ContEnum.NO)|al.getQuantite()<0.01) {
				catPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + 
						((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
						((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
						     +  ": "+
						al.getQuantite() +"g" +"/"+resource.getString("Jour")
						, font));}else {
							catPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + 
									((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
									((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
									      +  ": "+
									al.getQuantite() +"g "+"/"+resource.getString("Jour")+"("+ twoDForm.format(al.getQuantite()/al.getAlim().getQuantInt())+" "+resource.getString(al.getAlim().getCont().getName())+ (al.getQuantite()/al.getAlim().getQuantInt()>1?"s":"")+")"
									, font));

						}
			bddNames+= (bddNames.contains(vet.getAlDBL().getNom(al.getAlim().getDataB())))?"":( ", " + vet.getAlDBL().getNom(al.getAlim().getDataB()));
		}

	}

	private static void descrimeRation(ConsultationEv cons, Ration rat,  Chapter catPart,  ResourceBundle resource) {
		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
		Paragraph subPara = new Paragraph(resource.getString("diet")+" : "+rat.getNom(), subFont);
		Section subCatPart = catPart.addSection(subPara);
		Chunk bullet = new Chunk("\u2022");
		List list = new List(true, false, 10);
		for(AlimentRation al:rat.getAlimentList()) {
			if (al.getAlim().getCont().equals(ContEnum.NO)|al.getQuantite()<0.01) {
				subCatPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ 
						(  al.getGamme().isBlank()? "":(al.getGamme()+", "))+
						al.getNom() + 
						((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
						((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
				      + ": "+
						al.getQuantite() +"g" +"/"+resource.getString("Jour")
						, font));}else {
							subCatPart.add(new Paragraph(bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + 	((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+
									((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(Espece.getEnumFromStringId(  al.getAlim().getEspeces().getFirst()).getName()):"" )
								      + ": "+
									al.getQuantite() +"g"+"/"+resource.getString("Jour")+" ("+ twoDForm.format(al.getQuantite()/al.getAlim().getQuantInt())+" "+resource.getString(al.getAlim().getCont().getName())+ (al.getQuantite()/al.getAlim().getQuantInt()>1?"s":"")+")"
									, font));
						}
			bddNames+= (bddNames.contains(vet.getAlDBL().getNom(al.getAlim().getDataB())))?"":( ", " + vet.getAlDBL().getNom(al.getAlim().getDataB()));
				}

	}
	private static String descrimeRation(ConsultationEv cons, Ration rat,  int i,  ResourceBundle resource) {
		DecimalFormat twoDForm = new DecimalFormat("#.#"); 
		String res="";
	res+= resource.getString("diet")+" "+i +" : "+rat.getNom()+"\n";

		Chunk bullet = new Chunk("\u2022");
		List list = new List(true, false, 10);
		for(AlimentRation al:rat.getAlimentList()) {
			if (al.getAlim().getCont().equals(ContEnum.NO)|al.getQuantite()<0.01) {
			res+=	bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + ((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(al.getAlim().getEspeces().getFirst()):"" )
				      + ": "+
						al.getQuantite() +"g" +"/"+resource.getString("Jour")+"\n";
					}else {
						res+=	bullet +(al.getFamillyBrand().isBlank()  ?"":(al.getFamillyBrand()+", "))+ (  al.getGamme().isBlank()? "":(al.getGamme()+", "))+al.getNom() + ((al.getType()==FoodKind.COMPLET)? ((al.getNutrient(NutrientBase.HUMIDITE)>14)? (" " +resource.getString("wet")):(" " +resource.getString("dry"))) :"")+ ((al.getType()==FoodKind.COMPLET|al.getType()==FoodKind.COMPLEMENTAIRE)&al.getAlim().getEspeces().size()==1? " "+resource.getString(al.getAlim().getEspeces().getFirst()):"" )
							      +": "+
									al.getQuantite() +"g"+"/"+resource.getString("Jour")+" ("+ twoDForm.format(al.getQuantite()/al.getAlim().getQuantInt())+" "+resource.getString(al.getAlim().getCont().getName())+ (al.getQuantite()/al.getAlim().getQuantInt()>1?"s":"")+")"+"\n";
									
						}
		
		}
		return res;

	}
	private static PdfPTable tableIntake(LabelData[]labBEE, LabelData[]labBW,LabelData[]labMW, Ration rat, ResourceBundle resource) {
		DecimalFormat twoDForm = new DecimalFormat("#.##"); 
		PdfPTable tab=new PdfPTable(4);
		PdfPCell c1;
		PdfPCell c2;
		PdfPCell c3;
		PdfPCell c4;
		tab.addCell(new   Phrase("Nutrient", tabfont));
		tab.addCell(new   Phrase(resource.getString("intakeSER")+ " (/Mcal)", tabfont));
		tab.addCell(new   Phrase(resource.getString("intakeBW")+ " (/kg)",tabfont));
		tab.addCell(new   Phrase(resource.getString("intakeMW")+ " (/kg MW)", tabfont));
		tab.setHeaderRows(1);
		tab.setHorizontalAlignment(Element.ALIGN_CENTER);
		for (int i=0; i<labBEE.length ; i++) {
			if(!labBEE[i].getMainEnum().equals(MainNutrientEnum.NO)) {
				c1=new PdfPCell();
				c2=new PdfPCell();
				c3=new PdfPCell();
				c4=new PdfPCell();

				c1.setPhrase(new   Phrase(resource.getString(labBEE[i].getLabel())+" ("+labBEE[i].getUnit()+")", tabfont));
				c2.setPhrase(new   Phrase(twoDForm .format(labBEE[i].getVal())+" "+labBEE[i].getSign(), tabfont));
				c3.setPhrase(new   Phrase(twoDForm .format(labBW[i].getVal())+" "+ labBEE[i].getSign(), tabfont));
				c4.setPhrase(new   Phrase(twoDForm .format(labMW[i].getVal())+" "+labBEE[i].getSign(), tabfont));


				c2.setHorizontalAlignment(Element.ALIGN_CENTER);
				c3.setHorizontalAlignment(Element.ALIGN_CENTER);
				c4.setHorizontalAlignment(Element.ALIGN_CENTER);

				tab.addCell(c1);
				tab.addCell(c2);
				tab.addCell(c3);
				tab.addCell(c4);
			}	
		}       

		return tab;
	}
	public class HeaderAndFooter extends PdfPageEventHelper {

	    private String name = "";


	    protected Phrase footer;
	    protected Phrase header;

	    /*
	     * Font for header and footer part.
	     */
	    private Font headerFont = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);

	    private Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 6, BaseColor.BLACK);


	    /*
	     * constructor
	     */
	    public HeaderAndFooter(String name) {
	        super();

	        this.name = name;


	        header = new Phrase("***** Header *****");
	        footer = new Phrase("Created with VetNutri 2 version " + TextConstant.VERSION+", values are for information purposes only");
	    }


	    @Override
	    public void onEndPage(PdfWriter writer, Document document) {

	        PdfContentByte cb = writer.getDirectContent();

	        //header content
	        String headerContent = "Name: " +name;

	        //header content
	        String footerContent = "Created with VetNutri 2 version " + TextConstant.VERSION.nameToString()+", values are for information purposes only, Database used: "+bddNames;
	       
	        
	        /*
	         * Header
	         */
	        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(String.format(" %d ", 
	                writer.getCurrentPageNumber()),footerFont), 
	        		   document.right() - 2, document.top() + 30, 0);

	        /*
	         * Foooter
	         */
	        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(footerContent,footerFont), 
	                document.leftMargin() +1 , document.bottom() - 20, 0);

	    }

	}
	public  void createcurvePdf(AnimalEv anim, LineWeight lw, ResourceBundle resource

			) {
		this.vet=vet;
		// 1. Create document
		PdfWriter writer=null;
		Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
		
		String name=(DataConnector.isWindows()?"":"../")+"Prescription/Curve_"+anim.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+anim.getId().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+".pdf";

		try {

		writer=PdfWriter.getInstance(document, new FileOutputStream(name));

			HeaderAndFooter hAf=new HeaderAndFooter("NALE");
			 writer.setPageEvent(hAf);	// 3. Open document
			document.open();
			Paragraph preface = new Paragraph();
			// We add one empty line


			// Lets write a big header






			Paragraph catPart = new Paragraph("", catFont);
			int k=5;

			int width=1400;
			int height=1000;



			Phrase p=new Phrase();
			ChartWeight chartCreator=new ChartWeight(resource);




			JFreeChart cart=chartCreator.createGraphWeightBW(anim, lw);
			BufferedImage bufferedImage = cart.createBufferedImage(width, height);
			Image image  = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(50f);
			p.add(new Chunk(image, 0, 0, true));


			catPart.add(p);
			document.add(catPart);

			document.close();


			Desktop desk = Desktop.getDesktop();
			desk.open(new File(name));
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	public  void createcurvePdf(AnimalEv anim, CurveP lw, ResourceBundle resource

			) {
		this.vet=vet;
		// 1. Create document
		PdfWriter writer=null;
		Document document = new Document(PageSize.A4.rotate(), 50, 50, 50, 50);
		
		String name=(DataConnector.isWindows()?"":"../")+"Prescription/Curve_"+anim.getNom().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+"_"+anim.getId().replaceAll("[^a-zA-Z0-9\\s]", "").replaceAll("[/\\\\]", "")+".pdf";

		try {

		writer=PdfWriter.getInstance(document, new FileOutputStream(name));

			HeaderAndFooter hAf=new HeaderAndFooter("NALE");
			 writer.setPageEvent(hAf);	// 3. Open document
			document.open();
			Paragraph preface = new Paragraph();
			// We add one empty line


			// Lets write a big header






			Paragraph catPart = new Paragraph("", catFont);
			int k=5;

			int width=1400;
			int height=1000;



			Phrase p=new Phrase();
			ChartWeight chartCreator=new ChartWeight(resource);




			JFreeChart cart=chartCreator.createGraphWeightBW(anim, lw);
			BufferedImage bufferedImage = cart.createBufferedImage(width, height);
			Image image  = Image.getInstance(writer, bufferedImage, 1.0f);
			image.scalePercent(50f);
			p.add(new Chunk(image, 0, 0, true));


			catPart.add(p);
			document.add(catPart);

			document.close();


			Desktop desk = Desktop.getDesktop();
			desk.open(new File(name));
		} catch (IOException | DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}public String createOrdonnancePP(AnimalEv anim, ConsultationEv cons, ArrayList<Ration>rl, ArrayList<Advise> al, Vet vet,ResourceBundle resource
			) {
		// 1. Create document
		String resp="";
		this.vet=vet;
		
		// 2. Create PdfWriter
	
			// 4. Add content
			Paragraph catPart = new Paragraph();
			int i=1;
			for(Ration rat:rl) {
			resp+=	descrimeRation( cons,  rat, i,   resource) ;
				i++;
			}
			resp+="\n";
	resp+=resource.getString("Advise") +"\n";
			for (Advise ad:al) {
				resp+=ad.getText()+"\n";
			}
		
			
		return resp;

	}

	

}


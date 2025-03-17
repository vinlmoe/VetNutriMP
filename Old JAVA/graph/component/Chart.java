package graph.component;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.BoxAndWhiskerToolTipGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.labels.StandardPieToolTipGenerator;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.dial.DialCap;
import org.jfree.chart.plot.dial.DialPlot;
import org.jfree.chart.plot.dial.DialPointer;
import org.jfree.chart.plot.dial.DialTextAnnotation;
import org.jfree.chart.plot.dial.DialValueIndicator;
import org.jfree.chart.plot.dial.StandardDialFrame;
import org.jfree.chart.plot.dial.StandardDialRange;
import org.jfree.chart.plot.dial.StandardDialScale;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.ui.RectangleEdge;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.chart.util.LogFormat;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.statistics.BoxAndWhiskerCategoryDataset;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


import DataStruct.AlimP;
import Enumerise.AAEnum;
import Enumerise.FoodKind;
import Enumerise.KindData;
import Enumerise.NutrientAnalysis;
import Enumerise.NutrientBase;
import Enumerise.NutrientLipid;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import equation.RequirementAnalyzer;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Tooltip;
import model.AlimentRation;
import model.AnimalEv;
import model.ConsultationEv;
import model.Ration;
import model.RationCalculator;
import model.RefValues;
import model.Reference;
import model.ReferenceEv;
import model.TypeAlim;
import model.WeightDate;


public class Chart {
	private ResourceBundle bun;
	public Chart(ResourceBundle bun){
		this.bun=bun;
	}
	
	public 	JFreeChart EnerPie(float ENAe, float Prote, float Lipe, boolean Export, int k){
	final DefaultPieDataset pieDataset = new DefaultPieDataset();

	pieDataset.setValue(bun.getString("ENA"), ENAe);
	pieDataset.setValue(bun.getString("PROT"), Prote);
	pieDataset.setValue(bun.getString("LIP"), Lipe);
	 PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
	            "{2}", new DecimalFormat("0"), new DecimalFormat("0%"));
	      

	 JFreeChart pieChart = ChartFactory.createPieChart(bun.getString("EnerCompoTitle"), pieDataset, true, false, false);
	PiePlot plot = (PiePlot) pieChart.getPlot();  
	plot.setSectionPaint(bun.getString("ENA"),Color.decode("#FFFF00"));
	plot.setSectionPaint(bun.getString("PROT"),Color.decode("#FF3399"));
	plot.setSectionPaint(bun.getString("LIP"),Color.decode("#EECA00"));
	plot.setLabelGenerator(gen);
	if(Export) {
		plot.setLabelFont( new Font("SansSerif", Font.BOLD, 6*k));
	}
	pieChart=style(pieChart, k);
	return pieChart;}
	
	public 	JFreeChart CompoPie(float Humide, float Prote, float Lipe, float ENAe, float CBe, float Cendre, boolean Export, int k){
	final DefaultPieDataset pieDataset = new DefaultPieDataset();

	pieDataset.setValue(bun.getString("ENA"), ENAe);
	pieDataset.setValue(bun.getString("PROT"), Prote);
	pieDataset.setValue(bun.getString("LIP"), Lipe);
	pieDataset.setValue(bun.getString("CEN"), Cendre);
	pieDataset.setValue(bun.getString("CEL"), CBe);
		pieDataset.setValue(bun.getString("HUM"), Humide);
	 PieSectionLabelGenerator gen = new StandardPieSectionLabelGenerator(
	            "{2}", new DecimalFormat("0"), new DecimalFormat("0%"));
	      
	
	 JFreeChart pieChart = ChartFactory.createPieChart("Composition", pieDataset, true, false, false);
	PiePlot plot = (PiePlot) pieChart.getPlot();  
	plot.setSectionPaint(bun.getString("ENA"),Color.decode("#FFFF00"));
	plot.setSectionPaint(bun.getString("PROT"),Color.decode("#FF3399"));
	plot.setSectionPaint(bun.getString("LIP"),Color.decode("#EECA00"));
	plot.setSectionPaint(bun.getString("CEN"),Color.decode("#51535A"));
	plot.setSectionPaint(bun.getString("CEL"),Color.decode("#63EC3B"));
	plot.setSectionPaint(bun.getString("HUM"),Color.decode("#40D2F6"));
	plot.setLabelGenerator(gen);
	if(Export) {
		plot.setLabelFont( new Font("SansSerif", Font.BOLD, 6*k));
	}
	pieChart=style(pieChart,k);
	return pieChart;}
	
	
	public 	JFreeChart ProphosPlot(){

		RefValues ref=new RefValues();
	      DefaultValueDataset data = new DefaultValueDataset(0);
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Prot�ines/Phosphore",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

	      DialPlot plot2 = (DialPlot)(chart.getPlot());

	      

			StandardDialFrame  dialFrame = new StandardDialFrame ();

		    dialFrame.setBackgroundPaint(Color.lightGray);

		                  dialFrame.setForegroundPaint(Color.darkGray);

		                  plot.setDialFrame(dialFrame);

	    


	      
	        DialTextAnnotation annotation1 = new DialTextAnnotation("Prot/Phos");

	        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

	        annotation1.setRadius(0.7);

	        plot2.addLayer(annotation1);

	        DialValueIndicator dvi = new DialValueIndicator(0);

	        plot2.addLayer(dvi);



	/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

	and the -120 is the starting angle and -300 is the ending angle in degrees.*/

	                        StandardDialScale scale = new StandardDialScale(0, 120, -120, -300,30, 10);



	        scale.setTickRadius(0.88);

	        scale.setTickLabelOffset(0.15);

	        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

	        plot2.addScale(0, scale);

	        StandardDialRange range = new StandardDialRange(40, 120.0, Color.green);

	        range.setInnerRadius(0.52);

	        range.setOuterRadius(0.58);

	        plot2.addLayer(range);

	        StandardDialRange range2 = new StandardDialRange(30.0, 40.0, Color.blue);

	        range2.setInnerRadius(0.52);

	        range2.setOuterRadius(0.58);

	        plot2.addLayer(range2);

	        StandardDialRange range3 = new StandardDialRange(0.0, 30.0, Color.red);

	        range3.setInnerRadius(0.52);

	        range3.setOuterRadius(0.58);

	        plot2.addLayer(range3);

	        DialPointer needle = new DialPointer.Pointer();

	        plot2.addLayer(needle);

	        DialCap cap = new DialCap();

	        cap.setRadius(0.1);

	        plot2.setCap(cap);




	            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

	              //plot.setBackgroundPaint(gradientPaint);




          
		return chart;}
	public 	JFreeChart ProphosPlot(Ration rat){

		RefValues ref=new RefValues();
	      DefaultValueDataset data = new DefaultValueDataset(rat.getNutrient(NutrientBase.PROTEINE)/rat.getNutrient(NutrientMacro.PHOS));
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Prot�ines/Phosphore",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

		 
		DialPlot plot2 = (DialPlot)(chart.getPlot());

  

		StandardDialFrame  dialFrame = new StandardDialFrame ();

	    dialFrame.setBackgroundPaint(Color.lightGray);

	                  dialFrame.setForegroundPaint(Color.darkGray);

	                  plot.setDialFrame(dialFrame);

    


      
        DialTextAnnotation annotation1 = new DialTextAnnotation("Prot/Phos");

        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

        annotation1.setRadius(0.7);

        plot2.addLayer(annotation1);

        DialValueIndicator dvi = new DialValueIndicator(0);

        plot2.addLayer(dvi);



/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

and the -120 is the starting angle and -300 is the ending angle in degrees.*/

                        StandardDialScale scale = new StandardDialScale(0, 120, -120, -300,30, 10);



        scale.setTickRadius(0.88);

        scale.setTickLabelOffset(0.15);

        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

        plot2.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(40, 120.0, Color.green);

        range.setInnerRadius(0.52);

        range.setOuterRadius(0.58);

        plot2.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(30.0, 40.0, Color.blue);

        range2.setInnerRadius(0.52);

        range2.setOuterRadius(0.58);

        plot2.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(0.0, 30.0, Color.red);

        range3.setInnerRadius(0.52);

        range3.setOuterRadius(0.58);

        plot2.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();

        plot2.addLayer(needle);

        DialCap cap = new DialCap();

        cap.setRadius(0.1);

        plot2.setCap(cap);




            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

              //plot.setBackgroundPaint(gradientPaint);

        chart=style(chart,1);

          
		return chart;}

	public 	JFreeChart O6O3Plot(){

		RefValues ref=new RefValues();
	      DefaultValueDataset data = new DefaultValueDataset(0);
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Omega 6/Omega 3",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

		 
		DialPlot plot2 = (DialPlot)(chart.getPlot());

  

		StandardDialFrame  dialFrame = new StandardDialFrame ();

	    dialFrame.setBackgroundPaint(Color.lightGray);

	                  dialFrame.setForegroundPaint(Color.darkGray);

	                  plot.setDialFrame(dialFrame);

    


      
        DialTextAnnotation annotation1 = new DialTextAnnotation("");

        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

        annotation1.setRadius(0.7);

        plot2.addLayer(annotation1);

        DialValueIndicator dvi = new DialValueIndicator(0);

        plot2.addLayer(dvi);



/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

and the -120 is the starting angle and -300 is the ending angle in degrees.*/

                        StandardDialScale scale = new StandardDialScale(0, 10, -120, -300,2, 1);



        scale.setTickRadius(0.88);

        scale.setTickLabelOffset(0.15);

        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

        plot2.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(2, 5, Color.green);

        range.setInnerRadius(0.52);

        range.setOuterRadius(0.58);

        plot2.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(0, 2, Color.blue);

        range2.setInnerRadius(0.52);

        range2.setOuterRadius(0.58);

        plot2.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(5, 20, Color.red);

        range3.setInnerRadius(0.52);

        range3.setOuterRadius(0.58);

        plot2.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();

        plot2.addLayer(needle);

        DialCap cap = new DialCap();

        cap.setRadius(0.1);

        plot2.setCap(cap);




            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

              //plot.setBackgroundPaint(gradientPaint);


          
		return chart;}
	public 	JFreeChart O6O3Plot(Ration rat){

		RefValues ref=new RefValues();
		
	      DefaultValueDataset data = new DefaultValueDataset(rat.getNutrient(NutrientLipid.O6)/rat.getNutrient(NutrientLipid.O3));
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Omega 6/Omega 3",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

		 
		DialPlot plot2 = (DialPlot)(chart.getPlot());

  

		StandardDialFrame  dialFrame = new StandardDialFrame ();

	    dialFrame.setBackgroundPaint(Color.lightGray);

	                  dialFrame.setForegroundPaint(Color.darkGray);

	                  plot.setDialFrame(dialFrame);

    


      
        DialTextAnnotation annotation1 = new DialTextAnnotation("");

        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

        annotation1.setRadius(0.7);

        plot2.addLayer(annotation1);

        DialValueIndicator dvi = new DialValueIndicator(0);

        plot2.addLayer(dvi);



/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

and the -120 is the starting angle and -300 is the ending angle in degrees.*/

                        StandardDialScale scale = new StandardDialScale(0, 10, -120, -300,2, 1);



        scale.setTickRadius(0.88);

        scale.setTickLabelOffset(0.15);

        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

        plot2.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(2, 5, Color.green);

        range.setInnerRadius(0.52);

        range.setOuterRadius(0.58);

        plot2.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(0, 2, Color.blue);

        range2.setInnerRadius(0.52);

        range2.setOuterRadius(0.58);

        plot2.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(5, 10, Color.red);

        range3.setInnerRadius(0.52);

        range3.setOuterRadius(0.58);

        plot2.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();

        plot2.addLayer(needle);

        DialCap cap = new DialCap();

        cap.setRadius(0.1);

        plot2.setCap(cap);




            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

              //plot.setBackgroundPaint(gradientPaint);
chart=style(chart,1);

          
		return chart;}
	public 	JFreeChart CaPPlot(Ration rat){

		RefValues ref=new RefValues();
	      DefaultValueDataset data = new DefaultValueDataset(rat.getNutrient(NutrientMacro.CAL)/rat.getNutrient(NutrientMacro.PHOS));
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Phosphocalcique",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

		 
		DialPlot plot2 = (DialPlot)(chart.getPlot());

  

		StandardDialFrame  dialFrame = new StandardDialFrame ();

	    dialFrame.setBackgroundPaint(Color.lightGray);

	                  dialFrame.setForegroundPaint(Color.darkGray);

	                  plot.setDialFrame(dialFrame);

    


      
        DialTextAnnotation annotation1 = new DialTextAnnotation("");

        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

        annotation1.setRadius(0.7);

        plot2.addLayer(annotation1);

        DialValueIndicator dvi = new DialValueIndicator(0);

        plot2.addLayer(dvi);



/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

and the -120 is the starting angle and -300 is the ending angle in degrees.*/

                        StandardDialScale scale = new StandardDialScale(0, 4, -120, -300,1, 1);



        scale.setTickRadius(0.88);

        scale.setTickLabelOffset(0.15);

        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

        plot2.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(1, 2, Color.green);

        range.setInnerRadius(0.52);

        range.setOuterRadius(0.58);

        plot2.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(2, 4, Color.blue);

        range2.setInnerRadius(0.52);

        range2.setOuterRadius(0.58);

        plot2.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(0, 1, Color.red);

        range3.setInnerRadius(0.52);

        range3.setOuterRadius(0.58);

        plot2.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();

        plot2.addLayer(needle);

        DialCap cap = new DialCap();

        cap.setRadius(0.1);

        plot2.setCap(cap);




            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

              //plot.setBackgroundPaint(gradientPaint);

        chart=style(chart,1);

          
		return chart;}
	public 	JFreeChart CaPPlot(){

		RefValues ref=new RefValues();
	      DefaultValueDataset data = new DefaultValueDataset(0);
	      DialPlot plot = new DialPlot(data);
	      JFreeChart chart = new JFreeChart("Ratio Phosphocalcique",
	      JFreeChart.DEFAULT_TITLE_FONT, plot, false);
	      plot.setNoDataMessage("No data available");

		 
		DialPlot plot2 = (DialPlot)(chart.getPlot());

  

		StandardDialFrame  dialFrame = new StandardDialFrame ();

	    dialFrame.setBackgroundPaint(Color.lightGray);

	                  dialFrame.setForegroundPaint(Color.darkGray);

	                  plot.setDialFrame(dialFrame);

    


      
        DialTextAnnotation annotation1 = new DialTextAnnotation("");

        annotation1.setFont(new Font("Dialog", Font.BOLD, 14));

        annotation1.setRadius(0.7);

        plot2.addLayer(annotation1);

        DialValueIndicator dvi = new DialValueIndicator(0);

        plot2.addLayer(dvi);



/* set the dial range and the angle of the inside circle for e.g 0 through 120 is the range of the values

and the -120 is the starting angle and -300 is the ending angle in degrees.*/

                        StandardDialScale scale = new StandardDialScale(0, 4, -120, -300,1, 1);



        scale.setTickRadius(0.88);

        scale.setTickLabelOffset(0.15);

        scale.setTickLabelFont(new Font("Dialog", Font.PLAIN, 14));

        plot2.addScale(0, scale);

        StandardDialRange range = new StandardDialRange(1, 2, Color.green);

        range.setInnerRadius(0.52);

        range.setOuterRadius(0.58);

        plot2.addLayer(range);

        StandardDialRange range2 = new StandardDialRange(2, 4, Color.blue);

        range2.setInnerRadius(0.52);

        range2.setOuterRadius(0.58);

        plot2.addLayer(range2);

        StandardDialRange range3 = new StandardDialRange(0, 1, Color.red);

        range3.setInnerRadius(0.52);

        range3.setOuterRadius(0.58);

        plot2.addLayer(range3);

        DialPointer needle = new DialPointer.Pointer();

        plot2.addLayer(needle);

        DialCap cap = new DialCap();

        cap.setRadius(0.1);

        plot2.setCap(cap);




            // GradientPaint gradientPaint = new GradientPaint(0.0F, 10.0F, Color.WHITE, h, w, Color.green.darker());

              //plot.setBackgroundPaint(gradientPaint);


          
		return chart;}
	
	
	public 	JFreeChart MinPlot(Ration rat, RationCalculator calc,RequirementAnalyzer RA, ReferenceEv Ref, boolean Export, int k){

	RefValues refv=new RefValues();
	
    final DefaultCategoryDataset dataset = 
    new DefaultCategoryDataset( );
    float minQuant=0;
    
    for(NutrientMin enu: NutrientMin.values()) {
		   refv.setRef(rat.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(), enu );
		   String s = 	   refv.getColor();
				minQuant=refv.getMinQuant();
			
				if(minQuant!=0) {
		   if (s.equals("000000")   ){
		
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant),bun.getString("normal") ,bun.getString(enu.getLabel()) ); 
					dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) );
						 	
		   }
		   else if (s.equals("1844F8") ) {
			  
					dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 
					dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 				
				}else {
			
					dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 
	
		   }
			   
				}
    }
    
StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));



             		 JFreeChart pbarChart = ChartFactory.createStackedBarChart(
	         bun.getString("Oligoelements"),           
	         "",            
	         "",            
	         dataset,          
	      
	         PlotOrientation.HORIZONTAL,           
	         false, true, false);	
             		 
             

             		  
             		CategoryPlot plot =pbarChart.getCategoryPlot();
             		plot.getRenderer().setSeriesPaint(0, Color.green);
             		plot.getRenderer().setSeriesPaint(1, Color.blue);
             		plot.getRenderer().setSeriesPaint(2, Color.red);
             		plot.getRenderer().setDefaultToolTipGenerator(gen);
             		plot.getRangeAxis().setRange(0,300);
             		if (Export) {
                 		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                 		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));}else {
                	plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
             		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
                }

                     pbarChart=style(pbarChart,k);
                     
	return pbarChart;}
	
	
	public 	JFreeChart LipPlot(Ration rat, RationCalculator pp,Reference refer, boolean Export, int k){
		final DefaultPieDataset pieDataset = new DefaultPieDataset();
		RefValues ref=new RefValues();
	    final DefaultCategoryDataset dataset = 
	    new DefaultCategoryDataset( );  
NutrientLipid nut=NutrientLipid.O6;
	    
		if (refer.isNutrient(nut, Reflevel.OPTIMIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.OPTIMIN)), "% apport recommand�" ,nut.nameToString() ); 
		}else if (refer.isNutrient(nut, Reflevel.MIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.MIN)), "% apport minimal" ,nut.nameToString() ); 

		}
nut=NutrientLipid.AG204;
	    
		if (refer.isNutrient(nut, Reflevel.OPTIMIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.OPTIMIN)), "% apport recommand�" ,nut.nameToString() ); 
		}else if (refer.isNutrient(nut, Reflevel.MIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.MIN)), "% apport minimal" ,nut.nameToString() ); 

		}
nut=NutrientLipid.O3;
	    
		if (refer.isNutrient(nut, Reflevel.OPTIMIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.OPTIMIN)), "% apport recommand�" ,nut.nameToString() ); 
		}else if (refer.isNutrient(nut, Reflevel.MIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.MIN)), "% apport minimal" ,nut.nameToString() ); 

		}
nut=NutrientLipid.EPADHA;
	    
		if (refer.isNutrient(nut, Reflevel.OPTIMIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.OPTIMIN)), "% apport recommand�" ,nut.nameToString() ); 
		}else if (refer.isNutrient(nut, Reflevel.MIN)){
			dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.MIN)), "% apport minimal" ,nut.nameToString() ); 

		}
		
		
	StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));
	LogAxis yAxis = new LogAxis();
	yAxis.setBase(10);
	yAxis.setRange(10,1000);


	             		 JFreeChart pbarChart = ChartFactory.createBarChart(
		         "Acides gras essentiels",           
		         "",            
		         "% de couverture",            
		         dataset,          
		      
		         PlotOrientation.HORIZONTAL,           
		         true, true, false);	
	             		 pbarChart.getCategoryPlot().setRangeAxis(yAxis);
	             
	              		Paint[] colors = new Paint[dataset.getColumnCount()];
	             		for (int i = 0; i < colors.length; i++) {
	             		    colors[i] = dataset.getValue(0, i).floatValue()<100 ? Color.red : Color.green;
	             			}
	             
	             		CategoryItemRenderer render = new  CustomRenderer(colors);
	             		CategoryPlot plot =pbarChart.getCategoryPlot();
	             		plot.setRenderer(render);
	             		BarRenderer rend = (BarRenderer) pbarChart.getCategoryPlot().getRenderer() ;
	             		rend.setDefaultToolTipGenerator(gen);
	             		CategoryItemRenderer ci = pbarChart.getCategoryPlot().getRenderer() ;
                        StandardCategoryItemLabelGenerator stditemlabel = new StandardCategoryItemLabelGenerator("{2} %", new DecimalFormat("0"));        
                        ci.setDefaultItemLabelsVisible(true);
                   ci.setDefaultItemLabelGenerator(stditemlabel);
                   ci.setDefaultItemLabelFont(new Font("SansSerif", 0, 12));
                   ci.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                                           ItemLabelAnchor.CENTER, TextAnchor.CENTER));
		return pbarChart;}
		
		public 	JFreeChart MacroPlot(Ration rat, RationCalculator calc,RequirementAnalyzer RA, ReferenceEv Ref, boolean Export, int k){
	final DefaultPieDataset pieDataset = new DefaultPieDataset();
	RefValues refv=new RefValues();
    final DefaultCategoryDataset dataset = 
    new DefaultCategoryDataset( );  
    float minQuant=0;
for (NutrientMacro enu:NutrientMacro.values()){
	   refv.setRef(rat.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(),enu );
	   String s = 	   refv.getColor();
			minQuant=refv.getMinQuant();
		
			if(minQuant!=0) {
	   if (s.equals("000000")   ){
	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant),bun.getString("normal") ,bun.getString(enu.getLabel()) ); 
				dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) );
					 	
	   }
	   else if (s.equals("1844F8") ) {
		  
				dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 
				dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 				
			}else {
		
				dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 

	   }
		   
			}
}
StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));



         		 JFreeChart pbarChart = ChartFactory.createStackedBarChart(
         bun.getString("Minerals"),           
         "",            
         "",            
         dataset,          
      
         PlotOrientation.HORIZONTAL,           
         false, true, false);	
         		 
         

         		  
         		CategoryPlot plot =pbarChart.getCategoryPlot();
         		plot.getRenderer().setSeriesPaint(0, Color.green);
         		plot.getRenderer().setSeriesPaint(1, Color.blue);
         		plot.getRenderer().setSeriesPaint(2, Color.red);
         		plot.getRenderer().setDefaultToolTipGenerator(gen);
         		plot.getRangeAxis().setRange(0,300);
         		if (Export) {
             		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
             		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
            plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));
            plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));}else {
            	plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
         		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
        plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            }

                 pbarChart=style(pbarChart,k);
                 
return pbarChart;}
	
	
	public 	JFreeChart BasePlot(Ration rat, RationCalculator calc,RequirementAnalyzer RA, ReferenceEv Ref, boolean Export, int k){
	final DefaultPieDataset pieDataset = new DefaultPieDataset();
	RefValues refv=new RefValues();
    final DefaultCategoryDataset dataset = 
    new DefaultCategoryDataset( );  
    float minQuant=0;
for (NutrientBase enu:NutrientBase.values()){
	   refv.setRef(rat.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(), enu );
	
	   String s = 	   refv.getColor();
			minQuant=refv.getMinQuant();
		
			if(minQuant!=0) {
	   if (s.equals("000000")   ){
	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant),bun.getString("normal") ,bun.getString(enu.getLabel()) ); 
				dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) );
					 	
	   }
	   else if (s.equals("1844F8") ) {
		  
				dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 
				dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 				
			}else {
		
				dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
				dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 

	   }
		   
			}
}
StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));



         		 JFreeChart pbarChart = ChartFactory.createStackedBarChart(
         bun.getString("MainNutrients"),           
         "",            
         "",            
         dataset,          
      
         PlotOrientation.HORIZONTAL,           
         true, true, false);	
         		 
         

         		  
         		CategoryPlot plot =pbarChart.getCategoryPlot();
         		plot.getRenderer().setSeriesPaint(0, Color.green);
         		plot.getRenderer().setSeriesPaint(1, Color.blue);
         		plot.getRenderer().setSeriesPaint(2, Color.red);
         		plot.getRenderer().setDefaultToolTipGenerator(gen);
         		plot.getRangeAxis().setRange(0,300);
         		if (Export) {
             		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
             		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
            plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));
            plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));}else {
            	plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
         		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
        plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
        plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            }

                 pbarChart=style(pbarChart,k);
                 pbarChart.getLegend().setPosition(RectangleEdge.BOTTOM);
return pbarChart;}
	
	public 	JFreeChart VitaPlot(Ration rat, RationCalculator calc,RequirementAnalyzer RA, ReferenceEv Ref, boolean Export, int k){
		final DefaultPieDataset pieDataset = new DefaultPieDataset();
		RefValues refv=new RefValues();
	    final DefaultCategoryDataset dataset = 
	    new DefaultCategoryDataset( );  
	    float minQuant=0;
	for (NutrientVitam enu:NutrientVitam.values()){
		
		   refv.setRef(rat.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(),enu );
		   String s = 	   refv.getColor();
				minQuant=refv.getMinQuant();
			
				if(minQuant!=0) {
		   if (s.equals("000000")   ){
		
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant),bun.getString("normal") ,bun.getString(enu.getLabel()) ); 
					dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) );
						 	
		   }
		   else if (s.equals("1844F8") ) {
			  
					dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 
					dataset.addValue(0, bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 				
				}else {
			
					dataset.addValue(0, bun.getString("normal") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue(0, bun.getString("notOpti") ,bun.getString(enu.getLabel()) ); 	
					dataset.addValue( (double)(100*rat.getNutrient(enu)/minQuant), bun.getString("potDanger") ,bun.getString(enu.getLabel()) ); 
	
		   }
			   
				}
    }

	StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));



	         		 JFreeChart pbarChart = ChartFactory.createStackedBarChart(
	         bun.getString("Vitamins"),           
	         "",            
	         "",            
	         dataset,          
	      
	         PlotOrientation.HORIZONTAL,           
	         false, true, false);	
	         		 
	         

	         		  
	         		CategoryPlot plot =pbarChart.getCategoryPlot();
	         		plot.getRenderer().setSeriesPaint(0, Color.green);
	         		plot.getRenderer().setSeriesPaint(1, Color.blue);
	         		plot.getRenderer().setSeriesPaint(2, Color.red);
	         		plot.getRenderer().setDefaultToolTipGenerator(gen);
	         		plot.getRangeAxis().setRange(0,300);
	         		if (Export) {
                 		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                 		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));
                plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 8*k));}else {
                	plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
             		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
            plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 10*k));
                }

                     pbarChart=style(pbarChart,k);
	                 
	return pbarChart;}
		
		
		class CustomRenderer extends BarRenderer {

		        /** The colors. */
		        private Paint[] colors;

		        /**
		         * Creates a new renderer.
		         *
		         * @param colors  the colors.
		         */
		        public CustomRenderer(final Paint[] colors) {
		            this.colors = colors;
		        }

		        /**
		         * Returns the paint for an item.  Overrides the default behaviour inherited from
		         * AbstractSeriesRenderer.
		         *
		         * @param row  the series.
		         * @param column  the category.
		         *
		         * @return The item color.
		         */
		        public Paint getItemPaint(final int row, final int column) {
		            return this.colors[column % this.colors.length];
		        }
		    }
		
		public JFreeChart createGraphWeight(AnimalEv anim) {
			
				         
			XYSeries series3 = new XYSeries("");
			 String xax="";
		  
			ArrayList<WeightDateSerieData>lisx=new ArrayList<WeightDateSerieData>();
			for (WeightDate wd:anim.getListWeight()) {
				lisx.add(new WeightDateSerieData(wd.getDate(),wd.getValue()) );
			}
			for (ConsultationEv cons:anim.getList().getListConsult()) {
				lisx.add(new WeightDateSerieData(cons.getDate(),cons.getPoids()) );
			}
			Comparator<WeightDateSerieData> wdsdComparator = Comparator.comparing(WeightDateSerieData::getDate);
			lisx.sort(wdsdComparator);



			//defining a series

			for (int i=1; i<lisx.size();i++) {
				lisx.get(i).setVariation(lisx.get(i-1).getDate(), lisx.get(i-1).getWeight());
			}
			//populating the series with data
			// 
			if (lisx.size()>0) {
			if(lisx.get(lisx.size()-1).getWeekFromBirth(anim.getDateNaiss())<52) {
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(wdsd.getWeekFromBirth(anim.getDateNaiss()), wdsd.getWeight());
					  
xax=bun.getString("AgeWeeks");
				
				}
			}else {
				
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(wdsd.getYearFromBirth(anim.getDateNaiss()), wdsd.getWeight());
					  xax=bun.getString("AgeAnn");
				}
			}
			}
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(series3);

			  JFreeChart chart = ChartFactory.createXYLineChart(
					  bun.getString("Weight"),
				         xax, bun.getString("Weight")+" (kg)",
				        dataset,
				         PlotOrientation.VERTICAL,
				         true,true,false);
				XYPlot plot =chart.getXYPlot();
				plot.getRenderer().setSeriesPaint(0, Color.GREEN);
			
			//	plot.getRenderer().setDefaultToolTipGenerator(gen);
				plot.getRangeAxis().setLowerMargin(0);
				plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
				plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


		   
		chart=style(chart,1);

		   return chart;
		}

		

public 	JFreeChart AAPlot(Ration rat, RationCalculator pp,Reference refer){
final DefaultPieDataset pieDataset = new DefaultPieDataset();
RefValues ref=new RefValues();
final DefaultCategoryDataset dataset = 
new DefaultCategoryDataset( );  
for (AAEnum nut: AAEnum.values()){
if (refer.isNutrient(nut, Reflevel.OPTIMIN)){
	dataset.addValue( (double)(rat.getNutrient(nut)*100000/pp.getBEE()/refer.getNutrient(nut, Reflevel.OPTIMIN)), "% apport recommand�" ,nut.nameToString() ); 
}
}
if (refer.isNutrient(NutrientAnalysis.MethCys, Reflevel.OPTIMIN)){
	dataset.addValue( (double)((rat.getNutrient(AAEnum.METHIONINE)+rat.getNutrient(AAEnum.CYSTEINE))*100000/pp.getBEE()/refer.getNutrient(NutrientAnalysis.MethCys, Reflevel.OPTIMIN)), "% apport recommand�" ,NutrientAnalysis.MethCys.nameToString() ); 
}
if (refer.isNutrient(NutrientAnalysis.PhenTyr, Reflevel.OPTIMIN)){
	dataset.addValue( (double)((rat.getNutrient(AAEnum.PHENYLALANINE)+rat.getNutrient(AAEnum.TYROSINE))*100000/pp.getBEE()/refer.getNutrient(NutrientAnalysis.PhenTyr, Reflevel.OPTIMIN)), "% apport recommand�" ,NutrientAnalysis.PhenTyr.nameToString() ); 
}
StandardCategoryToolTipGenerator gen = new StandardCategoryToolTipGenerator("{1} : {2} %", new DecimalFormat("0"));
LogAxis yAxis = new LogAxis();
yAxis.setBase(10);
yAxis.setRange(10,1000);


         		 JFreeChart pbarChart = ChartFactory.createBarChart(
         "Acides Amin�s",           
         "",            
         "% de couverture",            
         dataset,          
      
         PlotOrientation.HORIZONTAL,           
         true, true, false);	
         		 pbarChart.getCategoryPlot().setRangeAxis(yAxis);
         
          		Paint[] colors = new Paint[dataset.getColumnCount()];
         		for (int i = 0; i < colors.length; i++) {
         		    colors[i] = dataset.getValue(0, i).floatValue()<100 ? Color.red : Color.green;
          		}
         
         		CategoryItemRenderer render = new  CustomRenderer(colors);
         		CategoryPlot plot =pbarChart.getCategoryPlot();
         		plot.setRenderer(render);
         		BarRenderer rend = (BarRenderer) pbarChart.getCategoryPlot().getRenderer() ;
         		rend.setDefaultToolTipGenerator(gen);
         		CategoryItemRenderer ci = pbarChart.getCategoryPlot().getRenderer() ;
                StandardCategoryItemLabelGenerator stditemlabel = new StandardCategoryItemLabelGenerator("{2} %", new DecimalFormat("0"));        
                ci.setDefaultItemLabelsVisible(true);
           ci.setDefaultItemLabelGenerator(stditemlabel);
           ci.setDefaultItemLabelFont(new Font("SansSerif", 0, 12));
           ci.setDefaultPositiveItemLabelPosition(new ItemLabelPosition(
                                   ItemLabelAnchor.CENTER, TextAnchor.CENTER));
return pbarChart;}

public 	JFreeChart AAPlot(){
	final DefaultPieDataset pieDataset = new DefaultPieDataset();
	RefValues ref=new RefValues();
    final DefaultCategoryDataset dataset = 
    new DefaultCategoryDataset( );  

		dataset.addValue( (double)0, "% apport recommand�" ,"" ); 

	
             		 JFreeChart pbarChart = ChartFactory.createBarChart(
             				 "Acides Amin�s",           
             		         "",            
             		         "% de couverture",                
	         dataset,          
	         PlotOrientation.HORIZONTAL,           
	         true, true, false);	
	return pbarChart;}

private JFreeChart style(JFreeChart ch, int k) {
	ch.setBackgroundPaint(Color.decode("#ddf1ee"));
	ch.setBorderPaint(Color.decode("#ddf1ee"));
	ch.getPlot().setBackgroundAlpha(0);
	ch.getPlot().setOutlineVisible(false);

	if (ch.getLegend()!=null) {
	ch.getLegend().setBackgroundPaint(Color.decode("#ddf1ee"));
	ch.getLegend().setItemFont(new Font("Arial",Font.PLAIN, 8*k));
	ch.getLegend().setPosition(RectangleEdge.LEFT);}
	ch.getTitle().setFont(new Font("Arial",Font.BOLD, 10*k));
	return ch;
}
public JFreeChart EnerOriChart(ObservableList<AlimP> ol, AlimP selected) {
	
	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYSeries line20 = new XYSeries("20% NFE" );     
	 XYSeries line40 = new XYSeries("40% NFE" );     
	 XYSeries line60 = new XYSeries("60% NFE" );     
	 XYSeries line80 = new XYSeries("80% NFE" );     
	
	 line20.add(0, 80);
	 line20.add(80, 0);
	 line40.add(0, 60);
	 line40.add(60, 0);
	 line60.add(0, 40);
	 line60.add(40, 0);
	 line80.add(0, 20);
	 line80.add(20, 0);
	 
	 
	 
	 
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
		XYSeriesCollection dataset = new XYSeriesCollection( );          
		 double minx=1000;
		 double miny=1000;
		 double maxx=0;
		 double maxy=0;
	 if(ol.size()>0) {
	 double x=0;
	 double y=0;
	for (AlimP al:ol ) {
		x=100*al.getAlimR().getNutrient(NutrientBase.PROTEINE)*3.5/al.getDE();
		y=100*al.getAlimR().getNutrient(NutrientBase.LIPIDE)*8.5/al.getDE();
		if(x!=0&y!=0) {
			maxx=(maxx<x)?x:maxx;
			maxy=(maxy<y)?y:maxy;
			minx=(minx>x)?x:minx;
			miny=(miny>y)?y:miny;}
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlimR().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}
if (selected!=null) {
	x=100*selected.getAlimR().getNutrient(NutrientBase.PROTEINE)*3.5/selected.getDE();
	y=100*selected.getAlimR().getNutrient(NutrientBase.LIPIDE)*8.5/selected.getDE();
	
	sel.add(x,y);
}

	  dataset.addSeries( sel );
    dataset.addSeries( home );          
    dataset.addSeries( sec );          
    dataset.addSeries( hum );
    dataset.addSeries( line20 );
    dataset.addSeries( line40 );
    dataset.addSeries( line60 );
    dataset.addSeries( line80 );
  
	 }
    JFreeChart chart = ChartFactory.createScatterPlot(  
            "",   
            "% from proteins", "% from fat", dataset,  PlotOrientation.VERTICAL, true, true, true);  
   
    if(minx<maxx&miny<maxy) {
    	  
    	  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
    	  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}
chart=style(chart,1);
if(minx<maxx&miny<maxy) {
	  
	  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
	  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}

XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
r.setSeriesLinesVisible(4, Boolean.TRUE);
r.setSeriesShapesVisible(4, Boolean.FALSE);
r.setSeriesLinesVisible(5, Boolean.TRUE);
r.setSeriesShapesVisible(5, Boolean.FALSE);
r.setSeriesLinesVisible(6, Boolean.TRUE);
r.setSeriesShapesVisible(6, Boolean.FALSE);
r.setSeriesLinesVisible(7, Boolean.TRUE);
r.setSeriesShapesVisible(7, Boolean.FALSE);
for ( XYTextAnnotation text:textAnnotation ) {
text.setFont(new Font("Arial",Font.BOLD, 20));

   chart.getXYPlot().addAnnotation(text);
   }
setPointRenderer(chart.getXYPlot()); 
    return chart;
}


public JFreeChart DensEnerChart(ObservableList<AlimP> ol, AlimP selected) {
	 final DefaultCategoryDataset dataset = 
			    new DefaultCategoryDataset( );
	
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
	 double x=0;
	 double y=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {

	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
			dataset.addValue( 0,bun.getString("Selected") ,al.getId()+"" ); 
			dataset.addValue( 0,bun.getString("home") ,al.getId()+"" ); 
					dataset.addValue( 0,bun.getString("sec") ,al.getId()+"" ); 
					dataset.addValue( (double)al.getDE()*100/(100-al.getAlim().getNutrient(NutrientBase.HUMIDITE)) ,bun.getString("humid") ,al.getId()+"" ); 

				 	
		}else {
			dataset.addValue( 0,bun.getString("Selected") ,al.getId()+"" ); 
			dataset.addValue( 0,bun.getString("home") ,al.getId()+"" ); 
			dataset.addValue( (double)al.getDE()*100/(100-al.getAlim().getNutrient(NutrientBase.HUMIDITE)),bun.getString("sec") ,al.getId()+"" ); 
			
			dataset.addValue( 0 ,bun.getString("humid") ,al.getId()+"" ); 
	}
	}else {
		dataset.addValue( 0,bun.getString("Selected") ,al.getId()+"" ); 
		dataset.addValue( (double)al.getDE()*100/(100-al.getAlim().getNutrient(NutrientBase.HUMIDITE)),bun.getString("home") ,al.getId()+"" ); 
		dataset.addValue( 0,bun.getString("sec") ,al.getId()+"" ); 
		dataset.addValue( 0 ,bun.getString("humid") ,al.getId()+"" ); 
		
		}
}
if (selected!=null) {
	dataset.addValue( (double)selected.getDE()*100/(100-selected.getAlim().getNutrient(NutrientBase.HUMIDITE)),bun.getString("Selected") ,selected.getId()+"" ); 
	dataset.addValue(0,bun.getString("home") ,selected.getId()+"" ); 
	dataset.addValue( 0,bun.getString("sec") ,selected.getId()+"" ); 
	dataset.addValue( 0 ,bun.getString("humid") ,selected.getId()+"" ); 

}}
	
JFreeChart chart = ChartFactory.createStackedBarChart(
       "Energy",           
        "",            
        "kcal/100g DM",            
        dataset,          
     
        PlotOrientation.VERTICAL,           
        false, true, false);	
   
	CategoryPlot plot =chart.getCategoryPlot();
		plot.getRenderer().setSeriesPaint(0, Color.red);
		plot.getRenderer().setSeriesPaint(1, Color.orange);
		plot.getRenderer().setSeriesPaint(2, Color.yellow);
		plot.getRenderer().setSeriesPaint(3, Color.cyan);
	//	plot.getRenderer().setDefaultToolTipGenerator(gen);
		plot.getRangeAxis().setLowerMargin(0);
		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


   
chart=style(chart,1);

   return chart;
   
}




public JFreeChart EPADHAChart(ObservableList<AlimP> ol, AlimP selected) {
	 final DefaultCategoryDataset dataset = 
			    new DefaultCategoryDataset( );

	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
	 double x=0;
	 double y=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {

		if(al.getAlim().isNutrient(NutrientLipid.AG205)|al.getAlim().isNutrient(NutrientLipid.AG226)) {
			dataset.addValue( 1000*al.getAlim().getNutrient(NutrientLipid.AG205)/al.getDE(),"EPA" ,al.getId()+"" ); 
			dataset.addValue( 1000* al.getAlim().getNutrient(NutrientLipid.AG226)/al.getDE(),"DHA" ,al.getId()+"" ); 
			dataset.addValue( 0,"EPA+DHA" ,al.getId()+"" );
		}else {
			dataset.addValue( 0 ,"EPA",al.getId()+"" ); 
			dataset.addValue(  0,"DHA" ,al.getId()+"" ); 
			dataset.addValue(1000* al.getAlim().getNutrient(NutrientLipid.EPADHA)/al.getDE(),"EPA+DHA" ,al.getId()+"" );
		}
	}
	}
JFreeChart chart = ChartFactory.createStackedBarChart(
       "",           
        "",            
        "g/Mcal",            
        dataset,          
     
        PlotOrientation.VERTICAL,           
        true, true, false);	
   
	CategoryPlot plot =chart.getCategoryPlot();
		plot.getRenderer().setSeriesPaint(0, Color.cyan);
		plot.getRenderer().setSeriesPaint(1, Color.yellow);
		plot.getRenderer().setSeriesPaint(2, Color.green);
		plot.getRenderer().setSeriesPaint(3, Color.cyan);
	//	plot.getRenderer().setDefaultToolTipGenerator(gen);
		plot.getRangeAxis().setLowerMargin(0);
		plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


   
chart=style(chart,1);

   return chart;
	
}
public JFreeChart versusChart(ObservableList<AlimP> ol, AlimP selected,  NutrientBase enux, NutrientBase enuy ) {
	
	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
	 XYSeriesCollection dataset = new XYSeriesCollection( );          
	 double x=0;
	 double y=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {
		x=1000*al.getAlim().getNutrient(enux)/al.getDE();
		y=1000*al.getAlim().getNutrient(enuy)/al.getDE();
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}
if (selected!=null) {
	x=1000*selected.getAlim().getNutrient(enux)/selected.getDE();
	y=1000*selected.getAlim().getNutrient(enuy)/selected.getDE();
	
	sel.add(x,y);
}
	
	  dataset.addSeries( sel );
   dataset.addSeries( home );          
   dataset.addSeries( sec );          
   dataset.addSeries( hum );
 
	 }
   JFreeChart chart = ChartFactory.createScatterPlot(  
           "",   
           bun.getString(enux.getLabel())+" "+enux.getUnite()+"/Mcal",
           bun.getString(enuy.getLabel())+" "+enux.getUnite()+"/Mcal",
           dataset,  PlotOrientation.VERTICAL, true, true, true);  
  
   
chart=style(chart,1);
for ( XYTextAnnotation text:textAnnotation ) {
text.setFont(new Font("Arial",Font.BOLD, 20));

  chart.getXYPlot().addAnnotation(text);
  }

   return chart;
   
}

public JFreeChart versusChart(ObservableList<AlimP> ol, AlimP selected,  NutrientMacro enux, NutrientBase enuy, float coef1, float coef2 ) {
	float n=1000;

	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYSeries line30 = new XYSeries( coef1+":1" );     
	 XYSeries line2 = new XYSeries( coef2+":1" );     
	
	
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
	 XYSeriesCollection dataset = new XYSeriesCollection( );          
	 double x=0;
	 double y=0;
	 
	 double minx=1000;
	 double miny=1000;
	 double maxx=0;
	 double maxy=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {
		x=1000*al.getAlim().getNutrient(enux)/al.getDE();
		y=1000*al.getAlim().getNutrient(enuy)/al.getDE();
		if(x!=0&y!=0) {
			maxx=(maxx<x)?x:maxx;
			maxy=(maxy<y)?y:maxy;
			minx=(minx>x)?x:minx;
			miny=(miny>y)?y:miny;}
		
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}
if (selected!=null) {
	x=1000*selected.getAlim().getNutrient(enux)/selected.getDE();
	y=1000*selected.getAlim().getNutrient(enuy)/selected.getDE();
	
	sel.add(x,y);
}
line30.add(minx, minx*coef1);
line30.add(maxx, maxx*coef1);
line2.add(minx, minx*coef2);
line2.add(maxx, maxx*coef2);

	
	  dataset.addSeries( sel );
  dataset.addSeries( home );          
  dataset.addSeries( sec );          
  dataset.addSeries( hum );
  dataset.addSeries(line30)
;; dataset.addSeries(line2)
;}

  
  
 
  
  JFreeChart chart = ChartFactory.createScatterPlot(  
          "",   
          bun.getString(enux.getLabel())+" "+enux.getUnite()+"/Mcal",
          bun.getString(enuy.getLabel())+" "+enux.getUnite()+"/Mcal",
          dataset,  PlotOrientation.VERTICAL, true, true, true);  
  XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
  r.setSeriesLinesVisible(4, Boolean.TRUE);
  r.setSeriesShapesVisible(4, Boolean.FALSE);
  r.setSeriesLinesVisible(5, Boolean.TRUE);
  r.setSeriesShapesVisible(5, Boolean.FALSE);
  
  if(minx<maxx&miny<maxy) {
  
  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}
chart=style(chart,1);
for ( XYTextAnnotation text:textAnnotation ) {
text.setFont(new Font("Arial",Font.BOLD, 20));

 chart.getXYPlot().addAnnotation(text);
 }
setPointRenderer(chart.getXYPlot()); 
  return chart;
	
}
public JFreeChart versusChart(ObservableList<AlimP> ol, AlimP selected,  NutrientLipid enux, NutrientLipid enuy, float coef1, float coef2 ) {
	float n=1000;
	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYSeries line30 = new XYSeries( coef1+":1" );     
	 XYSeries line2 = new XYSeries( coef2+":1" );     

	 XYSeriesCollection dataset = new XYSeriesCollection( );          
	
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];

	 double x=0;
	 double y=0;
	 
	 double minx=1000;
	 double miny=1000;
	 double maxx=0;
	 double maxy=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {
		x=1000*al.getAlim().getNutrient(enux)/al.getDE();
		y=1000*al.getAlim().getNutrient(enuy)/al.getDE();
		if(x!=0&y!=0) {
		maxx=(maxx<x)?x:maxx;
		maxy=(maxy<y)?y:maxy;
		minx=(minx>x)?x:minx;
		miny=(miny>y)?y:miny;}
		
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}
if (selected!=null) {
	x=1000*selected.getAlim().getNutrient(enux)/selected.getDE();
	y=1000*selected.getAlim().getNutrient(enuy)/selected.getDE();
	
	sel.add(x,y);
}
line30.add(minx, minx*coef1);
line30.add(maxx, maxx*coef1);
line2.add(minx, minx*coef2);
line2.add(maxx, maxx*coef2);

	  dataset.addSeries( sel );
  dataset.addSeries( home );          
  dataset.addSeries( sec );          
  dataset.addSeries( hum );
  dataset.addSeries(line30)
; dataset.addSeries(line2)
;
  
	 }
 
  
  JFreeChart chart = ChartFactory.createScatterPlot(  
          "",   
          bun.getString(enux.getLabel())+" "+enux.getUnite()+"/Mcal",
          bun.getString(enuy.getLabel())+" "+enux.getUnite()+"/Mcal",
          dataset,  PlotOrientation.VERTICAL, true, true, true);  
  XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
  r.setSeriesLinesVisible(4, Boolean.TRUE);
  r.setSeriesShapesVisible(4, Boolean.FALSE);
  r.setSeriesLinesVisible(5, Boolean.TRUE);
  r.setSeriesShapesVisible(5, Boolean.FALSE);
  
  
  
  if(minx<maxx&miny<maxy) {
	  
  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}
chart=style(chart,1);
for ( XYTextAnnotation text:textAnnotation ) {
text.setFont(new Font("Arial",Font.BOLD, 20));

 chart.getXYPlot().addAnnotation(text);
 }
setPointRenderer(chart.getXYPlot()); 
  return chart;
	 
	 
}



public JFreeChart versusChart(ObservableList<AlimP> ol, AlimP selected,  NutrientMacro enux, NutrientMacro enuy, float coef1, float coef2 ) {
	float n=1000;
	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYSeries line30 = new XYSeries( coef1+":1" );     
	 XYSeries line2 = new XYSeries( coef2+":1" );     
	 XYSeriesCollection dataset = new XYSeriesCollection( );          
	
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];
	
	 double x=0;
	 double y=0;
	 
	 double minx=1000;
	 double miny=1000;
	 double maxx=0;
	 double maxy=0;
	 if(ol.size()>0) {
	for (AlimP al:ol ) {
		x=1000*al.getAlim().getNutrient(enux)/al.getDE();
		y=1000*al.getAlim().getNutrient(enuy)/al.getDE();
		if(x!=0&y!=0) {
			maxx=(maxx<x)?x:maxx;
			maxy=(maxy<y)?y:maxy;
			minx=(minx>x)?x:minx;
			miny=(miny>y)?y:miny;}
		
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}
if (selected!=null) {
	x=1000*selected.getAlim().getNutrient(enux)/selected.getDE();
	y=1000*selected.getAlim().getNutrient(enuy)/selected.getDE();
	
	sel.add(x,y);
}
line30.add(minx, minx*coef1);
line30.add(maxx, maxx*coef1);
line2.add(minx, minx*coef2);
line2.add(maxx, maxx*coef2);
 
	  dataset.addSeries( sel );
  dataset.addSeries( home );          
  dataset.addSeries( sec );          
  dataset.addSeries( hum );
  dataset.addSeries(line30)
;; dataset.addSeries(line2)
;

	 }
  
 
  
  JFreeChart chart = ChartFactory.createScatterPlot(  
          "",   
          bun.getString(enux.getLabel())+" "+enux.getUnite()+"/Mcal",
          bun.getString(enuy.getLabel())+" "+enux.getUnite()+"/Mcal",
          dataset,  PlotOrientation.VERTICAL, true, true, true);  
  XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
  r.setSeriesLinesVisible(4, Boolean.TRUE);
  r.setSeriesShapesVisible(4, Boolean.FALSE);
  r.setSeriesLinesVisible(5, Boolean.TRUE);
  r.setSeriesShapesVisible(5, Boolean.FALSE);
  
  if(minx<maxx&miny<maxy) {
	  
  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}
chart=style(chart,1);
for ( XYTextAnnotation text:textAnnotation ) {
text.setFont(new Font("Arial",Font.BOLD, 20));

 chart.getXYPlot().addAnnotation(text);
 }
setPointRenderer(chart.getXYPlot()); 
  return chart;
	
}


public JFreeChart versusChart(ObservableList<AlimP> ol, AlimP selected,  NutrientAnalysis enux, NutrientAnalysis enuy, float coef1, float coef2 ) {
	float n=1000;
	XYSeries hum = new XYSeries( bun.getString("humid") );     
	 XYSeries sec = new XYSeries( bun.getString("sec") );     
	 XYSeries home = new XYSeries( bun.getString("home") );     
	 XYSeries sel = new XYSeries( "Selecetd" );     
	 XYSeries line30 = new XYSeries( coef1+":1" );     
	 XYSeries line2 = new XYSeries( coef2+":1" );     

	 XYSeriesCollection dataset = new XYSeriesCollection( );          
	
	 XYTextAnnotation textAnnotation []=  new XYTextAnnotation[ol.size()];

	 double x=0;
	 double y=0;
	 
	 double minx=1000;
	 double miny=1000;
	 double maxx=0;
	 double maxy=0;
	 if(ol.size()>0) {
			AlimentRation a;
	for (AlimP al:ol ) {
 a=new AlimentRation(al.getAlim());
		if (Double.isFinite(a.getNutrient(enuy)) &Double.isFinite(a.getNutrient(enux))) {
	
		x=a.getNutrient(enux);  
		y=a.getNutrient(enuy);
		System.out.println( al.getId()+" "+x);
		if(x!=0&y!=0) {
		maxx=(maxx<x)?x:maxx;
		maxy=(maxy<y)?y:maxy;
		minx=(minx>x)?x:minx;
		miny=(miny>y)?y:miny;}
		
		textAnnotation[al.getId()-1]=new XYTextAnnotation(al.getId()+"", x, y);
	if(al.getAlim().getTypeAliment().equals(FoodKind.COMPLET)) {
		if (al.getAlim().getNutrient(NutrientBase.HUMIDITE)>14) {
		hum.add(x,y);
		}else {
			sec.add(x,y);
		}
	}else {
		home.add(x,y);
		}
}}
if (selected!=null) {
	a=new AlimentRation(selected.getAlim());

	if (Double.isFinite(a.getNutrient(enuy)) &Double.isFinite(a.getNutrient(enux))) {
		
		x=a.getNutrient(enux);  
		y=a.getNutrient(enuy);

	
	sel.add(x,y);}
}
line30.add(minx, minx*coef1);
line30.add(maxx, maxx*coef1);
line2.add(minx, minx*coef2);
line2.add(maxx, maxx*coef2);

	  dataset.addSeries( sel );
  dataset.addSeries( home );          
  dataset.addSeries( sec );          
  dataset.addSeries( hum );

  
	 }
 
  
  JFreeChart chart = ChartFactory.createScatterPlot(  
          "",   
          bun.getString(enux.getLabel())+" "+enux.getUnite()+"",
          bun.getString(enuy.getLabel())+" "+enux.getUnite()+"",
          dataset,  PlotOrientation.VERTICAL, true, true, true);  
  XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
  r.setSeriesLinesVisible(4, Boolean.TRUE);
  r.setSeriesShapesVisible(4, Boolean.FALSE);
  r.setSeriesLinesVisible(5, Boolean.TRUE);
  r.setSeriesShapesVisible(5, Boolean.FALSE);
  
  
  
  if(minx<maxx&miny<maxy) {
	  
  chart.getXYPlot().getDomainAxis().setRange(minx-(maxx-minx)*0.05, maxx+(maxx-minx)*0.05);
  chart.getXYPlot().getRangeAxis().setRange(miny-(maxy-miny)*0.05, maxy+(maxy-miny)*0.05);}
chart=style(chart,1);
for ( XYTextAnnotation text:textAnnotation ) {
	if (text!=null) {
text.setFont(new Font("Arial",Font.BOLD, 20));
	
 chart.getXYPlot().addAnnotation(text);}
 }
setPointRenderer(chart.getXYPlot()); 
  return chart;
	 
	 
}


public JFreeChart comparechart(ObservableList<AlimP>ol, ObservableList<AlimP> sel, NutrientBase enu) {
	  final DefaultBoxAndWhiskerCategoryDataset dataset 
      = new DefaultBoxAndWhiskerCategoryDataset();
	   final List<Double> list = new ArrayList();
	for (AlimP al:ol) {
		if (al.getAlim().isNutrient(enu)) {
		list.add((double)al.getAlim().getNutrient(enu)*1000/al.getDE());
	}
	}
	   final List<Double> list2 = new ArrayList();
		for (AlimP al:sel) {
			if (al.getAlim().isNutrient(enu)) {
			list2.add((double)al.getAlim().getNutrient(enu)*1000/al.getDE());
		}
		}
		  dataset.add(list, "Reference", "");
		  dataset.add(list2, "Selected", "");
		

	        final CategoryAxis xAxis = new CategoryAxis("");
	        final NumberAxis yAxis = new NumberAxis(enu.getUnite()+"/Mcal");
	    
	        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
	     
	        renderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
	        renderer.setMeanVisible(false);
	        renderer.setUseOutlinePaintForWhiskers(false);
	        renderer.setMaxOutlierVisible(true);
	        renderer.setMinOutlierVisible(true);
	        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

	        final JFreeChart chart = new JFreeChart(
	        		bun.getString(enu.getLabel()),
	            new Font("SansSerif", Font.BOLD, 10),
	            plot,
	            true
	        );
	  return style(chart,1);
	   
}

public JFreeChart comparechart(ObservableList<AlimP>ol, ObservableList<AlimP> sel, NutrientMacro enu) {
	  final DefaultBoxAndWhiskerCategoryDataset dataset 
      = new DefaultBoxAndWhiskerCategoryDataset();
	   final List<Double> list = new ArrayList();
	for (AlimP al:ol) {
		if (al.getAlim().isNutrient(enu)) {
		list.add((double)al.getAlim().getNutrient(enu)*1000/al.getDE());
	}
	}
	   final List<Double> list2 = new ArrayList();
		for (AlimP al:sel) {
			if (al.getAlim().isNutrient(enu)) {
			list2.add((double)al.getAlim().getNutrient(enu)*1000/al.getDE());
		}
		}
		  dataset.add(list, "Reference", "");
		  dataset.add(list2, "Selected", "");
		

	        final CategoryAxis xAxis = new CategoryAxis("");
	        final NumberAxis yAxis = new NumberAxis(enu.getUnite()+"/Mcal");
	    
	        final BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
	     
	        renderer.setDefaultToolTipGenerator(new BoxAndWhiskerToolTipGenerator());
	        renderer.setMeanVisible(false);
	        renderer.setUseOutlinePaintForWhiskers(false);
	        renderer.setMaxOutlierVisible(true);
	        renderer.setMinOutlierVisible(true);
	        final CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);

	        final JFreeChart chart = new JFreeChart(
	        		bun.getString(enu.getLabel()),
	            new Font("SansSerif", Font.BOLD, 10),
	            plot,
	            true
	        );
	  return style(chart,1);
	   
}



private void setPointRenderer(XYPlot xyPlot) {
	 XYItemRenderer renderer = xyPlot.getRenderer();
	    renderer.setSeriesPaint(0, Color.RED);
	    renderer.setSeriesPaint(1, Color.ORANGE);
	    renderer.setSeriesPaint(2, Color.YELLOW);
	    renderer.setSeriesPaint(3, Color.cyan);
	    double size = 20.0;
	    double delta = size / 2.0;
	    
	    Shape shape2 = new Ellipse2D.Double(-delta, -delta, size, size);
	    
	    renderer.setSeriesShape(0, shape2);
	    renderer.setSeriesShape(1, shape2);
	    renderer.setSeriesShape(2, shape2);
	    renderer.setSeriesShape(3, shape2);
	    
}
}




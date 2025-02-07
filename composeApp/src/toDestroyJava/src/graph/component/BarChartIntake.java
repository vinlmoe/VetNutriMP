package graph.component;

import java.util.ResourceBundle;

import Enumerise.KindData;
import Enumerise.NutrientMacro;
import Enumerise.NutrientMin;
import Enumerise.NutrientVitam;
import Enumerise.Reflevel;
import equation.RequirementAnalyzer;
import javafx.scene.chart.Axis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import model.ConsultationEv;
import model.Ration;
import model.RationCalculator;
import model.RefValues;
import model.Reference;
import model.ReferenceEv;

public class BarChartIntake {

   	private ResourceBundle bundle;
	private RationCalculator calc=new RationCalculator();
	   RefValues refv=new RefValues();
	   
	   public void BarChartIntake() {
		   
	   }
	public StackedBarChart<String, Number> Initialize(StackedBarChart<String, Number> chart, NutrientMacro m, ResourceBundle bun){
	bundle=bun;
	XYChart.Series<String, Number> dataSeries1=new XYChart.Series<String, Number>();
	XYChart.Series<String, Number> dataSeries2=new XYChart.Series<String, Number>();
	XYChart.Series<String, Number> dataSeries3=new XYChart.Series<String, Number>();
		   dataSeries1.setName("Recommended");
	       dataSeries2.setName("Non Optimal");
	       dataSeries3.setName("Dangerous");
	       for(NutrientMacro enu: NutrientMacro.values()) {
			   dataSeries3.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
			   dataSeries2.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
			   dataSeries1.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
	       }
	       chart.getData().addAll( dataSeries1,dataSeries2,dataSeries3); 
	       return chart;
		// TODO Auto-generated constructor stub
	}
	
	public StackedBarChart<String, Number> Initialize(StackedBarChart<String, Number> chart, NutrientMin m, ResourceBundle bun){
		bundle=bun;
		XYChart.Series<String, Number> dataSeries1=new XYChart.Series<String, Number>();
		XYChart.Series<String, Number> dataSeries2=new XYChart.Series<String, Number>();
		XYChart.Series<String, Number> dataSeries3=new XYChart.Series<String, Number>();
			   dataSeries1.setName("Recommended");
		       dataSeries2.setName("Non Optimal");
		       dataSeries3.setName("Dangerous");
		       for(NutrientMin enu: NutrientMin.values()) {
				   dataSeries3.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
				   dataSeries2.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
				   dataSeries1.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
		       }
		       chart.getData().addAll( dataSeries1,dataSeries2,dataSeries3); 
		       return chart;
			// TODO Auto-generated constructor stub
		}
	public StackedBarChart<String, Number> Initialize(StackedBarChart<String, Number> chart, NutrientVitam m, ResourceBundle bun){
		bundle=bun;
		XYChart.Series<String, Number> dataSeries1=new XYChart.Series<String, Number>();
		XYChart.Series<String, Number> dataSeries2=new XYChart.Series<String, Number>();
		XYChart.Series<String, Number> dataSeries3=new XYChart.Series<String, Number>();
			   dataSeries1.setName("Recommended");
		       dataSeries2.setName("Non Optimal");
		       dataSeries3.setName("Dangerous");
		       if (m.equals(NutrientVitam.VITA)) {
		       for(NutrientVitam enu: NutrientVitam.values()) {
		    	   if(enu.equals(NutrientVitam.VITA)|enu.equals(NutrientVitam.VITD)|enu.equals(NutrientVitam.VITE)|enu.equals(NutrientVitam.VITK)) {
				   dataSeries3.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
				   dataSeries2.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
				   dataSeries1.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));}
		       }
	}else {
		   for(NutrientVitam enu: NutrientVitam.values()) {
			   if (enu.equals(NutrientVitam.VITB1)|enu.equals(NutrientVitam.VITB2)|enu.equals(NutrientVitam.VITB3)|enu.equals(NutrientVitam.VITB5)|enu.equals(NutrientVitam.VITB6)|enu.equals(NutrientVitam.VITB8)|enu.equals(NutrientVitam.VITB9)|enu.equals(NutrientVitam.VITB12)|enu.equals(NutrientVitam.CHOLINE)) { 
		    		
				   dataSeries3.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
			   dataSeries2.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));
			   dataSeries1.getData().add(new XYChart.Data<String, Number>(enu.getAbr(), 0));}
	       }
	}
		       chart.getData().addAll( dataSeries1,dataSeries2,dataSeries3); 
		       return chart;
			// TODO Auto-generated constructor stub
		}
public  StackedBarChart Update(StackedBarChart<String, Number> chart, Ration alim,ConsultationEv cons,  RequirementAnalyzer RA,ReferenceEv Ref,  RationCalculator cal, ResourceBundle bun, NutrientMacro enua) {
	calc=cal;
bundle=bun;
XYChart.Series<String, Number> dataSeries1=chart.getData().get(0);
XYChart.Series<String, Number> dataSeries2=chart.getData().get(1);
XYChart.Series<String, Number> dataSeries3=chart.getData().get(2);
	   for(NutrientMacro enu: NutrientMacro.values()) {
		   refv.setRef(alim.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(), enu );
		   String s = 	   refv.getColor();
					
		   if (s.equals("000000")   ){
			   
		
		for( int i=0; i<dataSeries1.getData().size(); i++) {

			if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
				
				if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else if (Ref.isNutrient(enu, Reflevel.MIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else {
				dataSeries1.getData().get(i).setYValue( 0);
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}
		}
					 
				}}
				else if (s.equals("1844F8")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else {
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							} }
				else if (s.equals("F81818")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else {
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							}
		       }
		       }
	        // Series 1 - Data of 2014
	
	

	        // Add Series to StackedBarChart.
		  
		      chart.autosize();
		 
		      NumberAxis yAxis =(NumberAxis)   chart.getYAxis();
		      chart.getYAxis().setAutoRanging(false);
		 yAxis.setTickUnit(100);
		      yAxis.setLowerBound(0);
		      yAxis.setUpperBound(300);
		      
		      return chart;
}

public  StackedBarChart Update(StackedBarChart<String, Number> chart, Ration alim,ConsultationEv cons,  RequirementAnalyzer RA,ReferenceEv Ref,RationCalculator cal, ResourceBundle bun, NutrientMin enua) {
	calc=cal;
bundle=bun;
XYChart.Series<String, Number> dataSeries1=chart.getData().get(0);
XYChart.Series<String, Number> dataSeries2=chart.getData().get(1);
XYChart.Series<String, Number> dataSeries3=chart.getData().get(2);
	   for(NutrientMin enu: NutrientMin.values()) {
		   refv.setRef(alim.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(),enu );
		   String s = 	   refv.getColor();
				
		   if (s.equals("000000")   ){
			   
		
		for( int i=0; i<dataSeries1.getData().size(); i++) {

			if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
				
				if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else if (Ref.isNutrient(enu, Reflevel.MIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else {
				dataSeries1.getData().get(i).setYValue( 0);
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}
		}
					 
				}}
				else if (s.equals("1844F8")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else {
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							} }
				else if (s.equals("F81818")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else {
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							}
		       }
		       }
	        // Series 1 - Data of 2014
	
	

	        // Add Series to StackedBarChart.
		  
		      chart.autosize();
		 
		      NumberAxis yAxis =(NumberAxis)   chart.getYAxis();
		      chart.getYAxis().setAutoRanging(false);
		 yAxis.setTickUnit(100);
		      yAxis.setLowerBound(0);
		      yAxis.setUpperBound(300);
		      
		      return chart;
}


public  StackedBarChart Update(StackedBarChart<String, Number> chart, Ration alim,ConsultationEv cons, RequirementAnalyzer RA,ReferenceEv Ref, RationCalculator cal, ResourceBundle bun, NutrientVitam enua) {
	calc=cal;
bundle=bun;
XYChart.Series<String, Number> dataSeries1=chart.getData().get(0);
XYChart.Series<String, Number> dataSeries2=chart.getData().get(1);
XYChart.Series<String, Number> dataSeries3=chart.getData().get(2);
	   for(NutrientVitam enu: NutrientVitam.values()) {
		   
		   refv.setRef(alim.getNutrient(enu),RA.getReferences(enu.getMNE().getCoef(), enu.getCoef()), calc.getBEE(), calc.getOptiPoids(), calc.getPMOpti(), KindData.SER, enu.getMNE(), enu);
		   String s = 	   refv.getColor();
		   if (s.equals("000000")   ){
			   
		
		for( int i=0; i<dataSeries1.getData().size(); i++) {

			if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
				
				if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else if (Ref.isNutrient(enu, Reflevel.MIN)){
				dataSeries1.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}else {
				dataSeries1.getData().get(i).setYValue( 0);
				dataSeries2.getData().get(i).setYValue( 0);
				dataSeries3.getData().get(i).setYValue( 0);
			}
		}
					 
				}}
				else if (s.equals("1844F8")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries2.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}else {
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							} }
				else if (s.equals("F81818")   ){
					for( int i=0; i<dataSeries1.getData().size(); i++) {
						
						if (enu.getAbr().equals(dataSeries1.getData().get(i).getXValue())) {
							
							if (Ref.isNutrient(enu, Reflevel.OPTIMIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.OPTIMIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else if (Ref.isNutrient(enu, Reflevel.MIN)){
							dataSeries3.getData().get(i).setYValue( 100000*alim.getNutrient(enu)/(calc.getBEE()*Ref.getNutrient(enu,Reflevel.MIN)));
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries1.getData().get(i).setYValue( 0);
						}else {
							dataSeries1.getData().get(i).setYValue( 0);
							dataSeries2.getData().get(i).setYValue( 0);
							dataSeries3.getData().get(i).setYValue( 0);
						}
					}
								 
							}
		       }
		       }
	        // Series 1 - Data of 2014
	
	

	        // Add Series to StackedBarChart.
		  
		      chart.autosize();
		 
		      NumberAxis yAxis =(NumberAxis)   chart.getYAxis();
		      chart.getYAxis().setAutoRanging(false);
		 yAxis.setTickUnit(100);
		      yAxis.setLowerBound(0);
		      yAxis.setUpperBound(300);
		      
		      return chart;
}
}


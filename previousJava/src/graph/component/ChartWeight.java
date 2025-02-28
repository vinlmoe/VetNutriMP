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
import org.jfree.chart.axis.NumberTickUnit;
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
import DataStruct.CurveP;
import DataStruct.LineWeight;
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
import application.GFun;


public class ChartWeight {
	private ResourceBundle bun;
	public ChartWeight(ResourceBundle bun){
		this.bun=bun;
	}
	

		
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
		
		public JFreeChart createGraphWeight(AnimalEv anim, LineWeight lw, CurveP c) {
			XYSeriesCollection dataset = new XYSeriesCollection();
			boolean lineOK=false;
			boolean week=false;
			if (lw!=null) {
		lineOK=lw.isOK();}
			XYSeries series3 = new XYSeries("weight");

			 XYSeries min= new XYSeries("min" );     
			 XYSeries max = new XYSeries("max" );     
			 
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
				week=true;
			if(lineOK) {
				min.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit()), lw.getWeightInit());
				min.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())+lw.minChangDuration(), lw.getWeightObj());
				max.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit()), lw.getWeightInit());
				max.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())+lw.maxChangDuration(), lw.getWeightObj());
			}
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(wdsd.getWeekFromBirth(anim.getDateNaiss()), wdsd.getWeight());
xax=bun.getString("AgeWeeks");
				
				}
			
			}else {
				week=false;
				if(lineOK) {
				min.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())/52, lw.getWeightInit());
				min.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())/52+lw.minChangDuration()/52, lw.getWeightObj());
				max.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())/52, lw.getWeightInit());
				max.add(GFun.getWeekFrom(anim.getDateNaiss(), lw.getDateInit())/52+lw.maxChangDuration()/52, lw.getWeightObj());
				}
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(wdsd.getYearFromBirth(anim.getDateNaiss()), wdsd.getWeight());
					  xax=bun.getString("AgeAnn");
				}
				
			}
			}
			
			dataset.addSeries(series3);
			dataset.addSeries(min);
			dataset.addSeries(max);
			
			if(c!=null) {
				for (XYSeries x:c.getSeries(week)) {
					dataset.addSeries(x);
				}
			}

			  JFreeChart chart = ChartFactory.createScatterPlot(
					  bun.getString("Weight"),
				         xax, bun.getString("Weight")+" (kg)",
				        dataset,
				         PlotOrientation.VERTICAL,
				         true,true,false);
				XYPlot plot =chart.getXYPlot();
				plot.getRenderer().setSeriesPaint(0, Color.black);
			
			
			//	plot.getRenderer().setDefaultToolTipGenerator(gen);
				plot.getRangeAxis().setLowerMargin(0);
				plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
				plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


		   
		chart=style(chart,1);
		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		
		for(int i=1; i<13;i++) {

		r.setSeriesLinesVisible(i, Boolean.TRUE);
		r.setSeriesShapesVisible(i, Boolean.FALSE);
		plot.getRenderer().setSeriesPaint(i, Color.GREEN);
		}
		   return chart;
		}

		

	
		
		public JFreeChart createGraphWeight(AnimalEv anim) {
			
				         boolean lineOK=false;
			XYSeries series3 = new XYSeries("weight");

			 XYSeries min= new XYSeries("min" );     
			 XYSeries max = new XYSeries("max" );     
			 
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
			dataset.addSeries(min);
			dataset.addSeries(max);

			  JFreeChart chart = ChartFactory.createScatterPlot(
					  bun.getString("Weight"),
				         xax, bun.getString("Weight")+" (kg)",
				        dataset,
				         PlotOrientation.VERTICAL,
				         true,true,false);
				XYPlot plot =chart.getXYPlot();
				plot.getRenderer().setSeriesPaint(0, Color.GREEN);
				plot.getRenderer().setSeriesPaint(1, Color.BLUE);
				plot.getRenderer().setSeriesPaint(2, Color.BLUE);
			
			//	plot.getRenderer().setDefaultToolTipGenerator(gen);
				plot.getRangeAxis().setLowerMargin(0);
				plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
				plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


		   
		chart=style(chart,1);
		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		r.setSeriesLinesVisible(1, Boolean.TRUE);
		r.setSeriesShapesVisible(1, Boolean.FALSE);
		r.setSeriesLinesVisible(2, Boolean.TRUE);
		r.setSeriesShapesVisible(2, Boolean.FALSE);
		
		   return chart;
		}

		

	
		
		public JFreeChart createGraphWeightBW(AnimalEv anim, LineWeight lw) {
			float dif=0;
				         boolean lineOK=lw.isOK();
			XYSeries series3 = new XYSeries("weight");

			 XYSeries min= new XYSeries("min" );     
			 XYSeries max = new XYSeries("max" );     
			 
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
			
			
				min.add(0, lw.getWeightInit());
				min.add(lw.minChangDuration(), lw.getWeightObj());
				max.add(0, lw.getWeightInit());
				max.add(lw.maxChangDuration(), lw.getWeightObj());
			
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(GFun.getWeekFrom(lw.getDateInit(), wdsd.getDate()), wdsd.getWeight());
xax=bun.getString("Weeks");
				
				}
				
		
			}
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(series3);
			dataset.addSeries(min);
			dataset.addSeries(max);

			  JFreeChart chart = ChartFactory.createScatterPlot(
					(anim.getNom()+ " "+lw.getDateInit().toString()),
				         xax, bun.getString("Weight")+" (kg)",
				        dataset,
				         PlotOrientation.VERTICAL,
				         true,true,false);
				XYPlot plot =chart.getXYPlot();
				plot.getRenderer().setSeriesPaint(0, Color.BLACK);
				plot.getRenderer().setSeriesPaint(1, Color.GRAY);
				plot.getRenderer().setSeriesPaint(2, Color.GRAY);
			
			//	plot.getRenderer().setDefaultToolTipGenerator(gen);
				
				if (lw.minChangDuration()<lw.maxChangDuration()) {
					plot.getDomainAxis().setRange(0.00,26);
				}else {
					plot.getDomainAxis().setRange(0.00,26);
				}
				if (lw.getWeightInit()>lw.getWeightObj()) {
					dif=lw.getWeightInit()-lw.getWeightObj();
						plot.getRangeAxis().setRange(lw.getWeightObj()-dif*0.2,lw.getWeightInit()+dif*0.4);
				}else {
					dif=lw.getWeightObj()-lw.getWeightInit();
					plot.getRangeAxis().setRange(lw.getWeightInit()-dif*0.2,lw.getWeightObj()+dif*0.4);
				}
			
				NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();  
				xAxis.setTickUnit(new NumberTickUnit(2));
				NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();  
			
				plot.getRangeAxis().setLowerMargin(0);

plot.setDomainGridlinesVisible( true );
plot.setDomainGridlinePaint(Color.BLACK);
plot.setRangeGridlinesVisible( true );
plot.setRangeGridlinePaint(Color.BLACK);
				plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
				plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


		   
		chart=styleBW(chart,1);
		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
		r.setSeriesLinesVisible(1, Boolean.TRUE);
		r.setSeriesShapesVisible(1, Boolean.FALSE);
		r.setSeriesLinesVisible(2, Boolean.TRUE);
		r.setSeriesShapesVisible(2, Boolean.FALSE);
		
		   return chart;
		}

		public JFreeChart createGraphWeightBW(AnimalEv anim, CurveP c) {
			float dif=0;
				        
			XYSeries series3 = new XYSeries("weight");

			 XYSeries min= new XYSeries("min" );     
			 XYSeries max = new XYSeries("max" );     
			 
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
			
			
				for (WeightDateSerieData wdsd : lisx) {
					  series3.add(GFun.getWeekFrom(anim.getDateNaiss(), wdsd.getDate()), wdsd.getWeight());
xax=bun.getString("Weeks");
				
				}
				
		
			}
			XYSeriesCollection dataset = new XYSeriesCollection();
			dataset.addSeries(series3);
			if(c!=null) {
				for (XYSeries x:c.getSeries(true)) {
					dataset.addSeries(x);
				}
			}


			  JFreeChart chart = ChartFactory.createScatterPlot(
					(anim.getNom()),
				         xax, bun.getString("Weight")+" (kg)",
				        dataset,
				         PlotOrientation.VERTICAL,
				         true,true,false);
				XYPlot plot =chart.getXYPlot();
				plot.getRenderer().setSeriesPaint(0, Color.BLACK);
				plot.getRenderer().setSeriesPaint(1, Color.GRAY);
				plot.getRenderer().setSeriesPaint(2, Color.GRAY);
			
			//	plot.getRenderer().setDefaultToolTipGenerator(gen);
				
			
					plot.getDomainAxis().setRange(10.00,70);
		
			
				NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();  
				xAxis.setTickUnit(new NumberTickUnit(2));
				NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();  
			
				plot.getRangeAxis().setLowerMargin(0);

plot.setDomainGridlinesVisible( true );
plot.setDomainGridlinePaint(Color.BLACK);
plot.setRangeGridlinesVisible( true );
plot.setRangeGridlinePaint(Color.BLACK);
				plot.getDomainAxis().setLabelFont(new Font("Arial",Font.PLAIN, 12));
				plot.getRangeAxis() .setLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getRangeAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));
		plot.getDomainAxis().setTickLabelFont(new Font("Arial",Font.PLAIN, 12));


		   
		chart=styleBW(chart,1);
		XYLineAndShapeRenderer r = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();
	for (int i=1; i<13; i++) {
		r.setSeriesLinesVisible(i, Boolean.TRUE);
		plot.getRenderer().setSeriesPaint(i, Color.GRAY);
		r.setSeriesShapesVisible(i, Boolean.FALSE);

	}
		   return chart;
		}

		
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


private JFreeChart styleBW(JFreeChart ch, int k) {
	ch.setBackgroundPaint(Color.WHITE);
	ch.setBorderPaint(Color.white);
	ch.getPlot().setBackgroundAlpha(0);
//	ch.getPlot().setOutlineVisible(false);

	if (ch.getLegend()!=null) {
	ch.getLegend().setBackgroundPaint(Color.WHITE);
	ch.getLegend().setItemFont(new Font("Arial",Font.PLAIN, 16*k));
	ch.getLegend().setPosition(RectangleEdge.LEFT);}
	ch.getTitle().setFont(new Font("Arial",Font.BOLD, 20*k));
	return ch;
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




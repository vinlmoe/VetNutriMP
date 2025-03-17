package DataStruct;

import java.time.LocalDate;
import  java.lang.Math;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jfree.data.xy.XYSeries;

import equation.Equation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.AlimentRation;
import model.BiblioRef;
import model.ConsultationEv;
import model.Espece;
import model.WeightDate;

public class CurveP {
	
	private String name;
	private List<CurveParamP> param= new ArrayList<CurveParamP>();


	private BiblioRef bib;
	private Espece Specie;
	private int begin=12;

	private String UUID="";
	


	
	public CurveP( String name,List<CurveParamP> param, BiblioRef bib, Espece esp, String uuid, int b) {
		this.name=name;
		this.param= param;
		this.bib=bib;
		this.Specie=esp;
		this.UUID=uuid;
		this.begin=b;
	}
	public CurveP( ) {
		this.name="";
		
		this.bib=new BiblioRef();
		this.Specie=Espece.CH;
		this.UUID=java.util.UUID.randomUUID().toString();;
	}

	
	public BiblioRef getBib() {
		return bib;
	}
	@Override
	public CurveP clone() throws CloneNotSupportedException {
		// TODO Auto-generated method stub
		CurveP o =(CurveP)super.clone();
		o.UUID=java.util.UUID.randomUUID().toString();
		return o;
	}
	public String getName() {
		return name;
	}
	
	
	public List<CurveParamP> getParam() {
		return param;
	}
	public Espece getSpecie() {
		return Specie;
	}
	public String getUUID() {
		return UUID;
	}
	public String getEspeceStr() {
		return Specie.nameToString();
	}
	public String getBiblioStr() {
		return bib.toString();
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getBegin() {
		return begin;
	}
	 public void setBib(BiblioRef bib) {
		this.bib = bib;
	}
	 public void setParam(List<CurveParamP> param) {
		this.param = param;
	}
	 public void setSpecie(Espece specie) {
		Specie = specie;
	}
	 
	 public List<XYSeries> getSeries(boolean b){
		 double weekDiv= b?1:52;
		 List<XYSeries> l=new ArrayList<XYSeries>();
		 XYSeries min= new XYSeries("min" );    
		 for (CurveParamP p:param) {
			
			 min=new XYSeries(p.getName());
			 for (int i=begin; i<75; i++) {
				 min.add(i/weekDiv, funGrowth(p, i));
			 }
			 l.add(min );		 
		 }
	 
	 return l;
	 }
	 private double funGrowth(CurveParamP p, double t) {
		 return p.getMax()-(p.getMax()  /(1+Math.pow (t/p.getHalf(),p.getSlope())));
	 }
@Override
public String toString() {
    return name;
}


}


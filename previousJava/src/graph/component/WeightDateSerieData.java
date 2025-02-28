package graph.component;

import java.time.LocalDate;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.joda.time.Weeks;

public class WeightDateSerieData {
	private LocalDate date;
	private float weight=0;
	private float variation=0;
	private float variationp=0;
	
	public WeightDateSerieData(LocalDate d, float w) {
		date =d;
		weight=w;
	}

	
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public LocalDate getDate() {
		return date;
	}public void setWeight(float weight) {
		this.weight = weight;
	}
	public float getWeight() {
		return weight;
	}
 public float getWeekFromBirth(LocalDate birthdate) {
	 if(birthdate!=null) {
	 return Days.daysBetween(new DateTime( birthdate.toString()), new DateTime(date.toString())).getDays()/7;}else {
		 return 0;
	 }
 }
 public float getMonthFromBirth(LocalDate birthdate) {
	 if(birthdate!=null) {	return getWeekFromBirth(birthdate)/4;}else {
		 return 0;
	 }
 }
 
 public float getYearFromBirth(LocalDate birthdate) {
	 if(birthdate!=null) {	return getWeekFromBirth(birthdate)/52;}else {
		 return 0;
	 }
 }
 
 /*public float getYearFromBirth(LocalDate birthdate) {
	 if(birthdate!=null) {	 return  Months.monthsBetween(new DateTime( birthdate.toString()), new DateTime(date.toString())).getMonths()/12;}else {
		 return 0;
	 }
 }*/
	public void setVariation(LocalDate d, float w) {
		this.variation=(weight-w)/(Days.daysBetween(new DateTime( d.toString()), new DateTime(date.toString())).getDays()/7);
		this.variationp=100*((weight-w)/(Days.daysBetween(new DateTime( d.toString()), new DateTime(date.toString())).getDays()/7))/w;
	}
	 public float getVariation() {
		return variation;
	}
	 public float getVariationp() {
		return variationp;
	} 
}

package application;

import java.time.LocalDate;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class GFun {
	public static String noPoint(String s){
		String ReP="";
		String t=".";
		String[] st = s.split(",");
		if(st.length==1){
			ReP = st[0];
		}else{
			ReP = st[0]+t+st[1];}
		try {
			Float.parseFloat(ReP);
		}catch (NumberFormatException e) {
			if (ReP!="") {
				ReP="0.0";}
		}
		return ReP;
	}
	

	 public static float getWeekFrom(LocalDate birthdate, LocalDate date) {
		 if(birthdate!=null) {
		 return Days.daysBetween(new DateTime( birthdate.toString()), new DateTime(date.toString())).getDays()/7;}else {
			 return 0;
		 }
	 }
	 public static float getMonthFrom(LocalDate birthdate, LocalDate date) {
		 if(birthdate!=null) {	return getWeekFrom(birthdate, date)/4;}else {
			 return 0;
		 }
	 }
	 
	 public static float getYearFrom(LocalDate birthdate, LocalDate date) {
		 if(birthdate!=null) {	return getWeekFrom(birthdate, date)/52;}else {
			 return 0;
		 }
	 }
	 

}

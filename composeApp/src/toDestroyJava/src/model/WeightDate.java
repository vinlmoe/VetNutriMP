package model;

import java.io.Serializable;
import java.time.LocalDate;

public class WeightDate implements Serializable{
	private String UUID;
	private LocalDate date; 
	private float value; 
	private float variation=0;
	private float veriationp=0;
	
	public WeightDate(LocalDate d, Float v) {
		this.UUID= java.util.UUID.randomUUID().toString();
		this.date=d;
		this.value=v;
		
				
	}

	public WeightDate(String uuid, LocalDate d, Float v) {
		this.UUID= uuid;
		this.date=d;
		this.value=v;
				
	}
	
	 public LocalDate getDate() {
		return date;
	}
	 public String getUUID() {
		return UUID;
	}
	 public float getValue() {
		return value;
	}
	 public void setDate(LocalDate date) {
		this.date = date;
	}public void setValue(float value) {
		this.value = value;
	}
}

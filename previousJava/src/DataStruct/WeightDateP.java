package DataStruct;

import java.time.LocalDate;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.ConsultationEv;
import model.WeightDate;

public class WeightDateP {
	
	private SimpleFloatProperty weight;
	private SimpleObjectProperty<LocalDate> dateCons;
	
	private String UUID;

	
	public WeightDateP(WeightDate wd) {
	
		this.weight=new SimpleFloatProperty(wd.getValue());
		this.dateCons= new SimpleObjectProperty(wd.getDate());
		this.UUID=wd.getUUID();
	}
	public void setDate(LocalDate date) {
		this.dateCons.set(date);
	}
	public LocalDate getDate() {
		return dateCons.get();
	}
	public ObjectProperty<LocalDate> getDateProp() {
		return dateCons;
	}
		public float getWeight() {
		return weight.get();
	}
		public void setWeight(float w) {
			this.weight.set(w);
		}
	public String getUUID() {
		return UUID;
	}
	

}


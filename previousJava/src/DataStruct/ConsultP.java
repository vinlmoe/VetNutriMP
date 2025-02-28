package DataStruct;

import java.time.LocalDate;
import java.util.Date;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.ConsultationEv;

public class ConsultP {
	
	private SimpleStringProperty subject;
	private SimpleObjectProperty<LocalDate> dateCons;
	
	private String UUID;
	private ConsultationEv cons;
	
	public ConsultP(ConsultationEv cons) {
	
		this.subject=new SimpleStringProperty(cons.getObjet());
		this.dateCons= new SimpleObjectProperty(cons.getDate());
		this.UUID=cons.getUUID();
	}
	public void setDate(LocalDate date) {
		this.dateCons.set(date);
	}
	public LocalDate getDateCons() {
		return dateCons.get();
	}
	public ObjectProperty<LocalDate> getDateConsProp() {
		return dateCons;
	}
		public String getSubject() {
		return subject.get();
	}
	public String getUUID() {
		return UUID;
	}
	public void setSubject(String objet) {
		this.subject.set(objet);
	}
}


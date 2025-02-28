package DataStruct;

import java.time.LocalDate;
import java.util.Date;

import equation.Equation;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import model.BiblioRef;
import model.ConsultationEv;
import model.WeightDate;

public class BiblioP {
	
	private SimpleStringProperty fAuthor;
	private SimpleObjectProperty<BiblioRef> biblio;
	private SimpleIntegerProperty year;
private SimpleIntegerProperty consistent;
	


	
	public BiblioP( BiblioRef eq) {
		this.fAuthor=new SimpleStringProperty(eq.getFirstAuthor());
		this.biblio= new SimpleObjectProperty<BiblioRef>(eq);
		this.year=new SimpleIntegerProperty(eq.getYear());
		this.consistent=new SimpleIntegerProperty(eq.getConsistent());
	
	}


public void setBiblio(BiblioRef eq) {
	this.fAuthor=new SimpleStringProperty(eq.getFirstAuthor());
	this.biblio= new SimpleObjectProperty<BiblioRef>(eq);
	this.year=new SimpleIntegerProperty(eq.getYear());
	this.consistent=new SimpleIntegerProperty(eq.getConsistent());
}

	public BiblioRef getBiblio() {
		return biblio.get();
	}
	public String getFAuthor() {
		return biblio.get().getFirstAuthor();
	}
	public Integer getYear() {
		return biblio.get().getYear();
	}
	
	public int getConsistent() {
		return  biblio.get().getConsistent();
	}

}


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

public class DescP {
	
	private SimpleStringProperty stringValue;
	private SimpleStringProperty description;
	


	
	public DescP(String desc, String c) {
	
		this.stringValue=new SimpleStringProperty(c);
		this.description= new SimpleStringProperty(desc);

	}
public String getStringValue() {
	return stringValue.get();
}
public String getDescription() {
	return description.get();
}
	

}


package DataStruct;

import java.util.Date;
import java.util.Locale;

import Enumerise.UnitEnum;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.Lang;
import model.targetAdjust;

public class UnitP {


	private SimpleStringProperty Nom;
	private SimpleObjectProperty<UnitEnum> targ;
	
	public  UnitP(UnitEnum ue) {
		this.targ=new SimpleObjectProperty<UnitEnum>(ue);
		this.Nom=new SimpleStringProperty(ue.getName());
	}
	public SimpleStringProperty getNom() {
		return Nom;
	}
	public String getNomS() {
		return Nom.get();
	}
	public UnitEnum getUnit() {
		return targ.get();
	}
	
}

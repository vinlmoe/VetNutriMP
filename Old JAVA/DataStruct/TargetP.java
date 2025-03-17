package DataStruct;

import java.util.Date;
import java.util.Locale;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import model.Lang;
import model.targetAdjust;

public class TargetP {
	private SimpleStringProperty Nom;
	private SimpleObjectProperty<targetAdjust> targ;
	
	public  TargetP(targetAdjust targo) {
		this.targ=new SimpleObjectProperty<targetAdjust>(targo);
		this.Nom=new SimpleStringProperty(targo.nameToString(Lang.EN));
	}
	public SimpleStringProperty getNom() {
		return Nom;
	}
	public String getNomS() {
		return Nom.get();
	}
	public targetAdjust getTarget() {
		return targ.get();
	}
	
}

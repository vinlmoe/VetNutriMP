package DataStruct;

import java.util.ResourceBundle;

import Enumerise.ConditionEnum;
import Enumerise.EquationKind;
import javafx.util.StringConverter;
import model.Espece;
import model.Species;

public class KindEnergyConverter extends StringConverter<EquationKind>
{
	
	private ResourceBundle bundle;
	public KindEnergyConverter(ResourceBundle rb) {
		bundle=rb;
	}
    // Method to convert a Person-Object to a String
    @Override
    public String toString(EquationKind person)
    {
        return person == null? null : bundle.getString(person.getName());
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public EquationKind fromString(String string)
    {
    
        return EquationKind.ENERGYDENSITY;
    }
}

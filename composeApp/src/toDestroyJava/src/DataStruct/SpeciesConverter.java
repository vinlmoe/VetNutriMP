package DataStruct;

import java.util.ResourceBundle;

import Enumerise.ConditionEnum;
import javafx.util.StringConverter;
import model.Espece;
import model.Species;

public class SpeciesConverter extends StringConverter<Espece>
{
	
	private ResourceBundle bundle;
	public SpeciesConverter(ResourceBundle rb) {
		bundle=rb;
	}
    // Method to convert a Person-Object to a String
    @Override
    public String toString(Espece person)
    {
        return person == null? null : bundle.getString(person.getName());
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public Espece fromString(String string)
    {
    
        return Espece.CH;
    }
}

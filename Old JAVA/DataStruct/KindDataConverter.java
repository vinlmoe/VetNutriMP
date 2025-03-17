package DataStruct;

import java.util.ResourceBundle;

import Enumerise.KindData;
import javafx.util.StringConverter;
import model.Espece;

public class KindDataConverter extends StringConverter<KindData>
{
	

	public KindDataConverter() {
	
	}
    // Method to convert a Person-Object to a String
    @Override
    public String toString(KindData person)
    {
        return person == null? null : (person.getUnit());
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public KindData fromString(String string)
    {
    
        return KindData.BW;
    }
}

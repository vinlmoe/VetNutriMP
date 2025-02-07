package DataStruct;

import java.util.ResourceBundle;

import javafx.util.StringConverter;
import model.Espece;

public class TranslateConverter extends StringConverter<String>
{
	
	private ResourceBundle bundle;
	public TranslateConverter(ResourceBundle rb) {
		bundle=rb;
	}
    // Method to convert a Person-Object to a String
    @Override
    public String toString(String person)
    {
        return person == null? null : bundle.getString(person);
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public String fromString(String string)
    {
    
        return "";
    }
}

package DataStruct;

import java.util.ResourceBundle;

import Enumerise.ConditionEnum;
import javafx.util.StringConverter;
import model.Espece;

public class ConditionConverter extends StringConverter<ConditionEnum>
{
	
	private ResourceBundle bundle;
	public ConditionConverter(ResourceBundle rb) {
		bundle=rb;
	}
    // Method to convert a Person-Object to a String
    @Override
    public String toString(ConditionEnum person)
    {
        return person == null? null : bundle.getString(person.getName());
    }
 
    // Method to convert a String to a Person-Object
    @Override
    public ConditionEnum fromString(String string)
    {
    
        return ConditionEnum.MORE;
    }
}

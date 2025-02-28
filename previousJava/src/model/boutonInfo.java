package model;

import java.awt.Button;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;

public class boutonInfo extends JLabel {
	private String wikipage=new String();
public boutonInfo(String wiki){
	wikipage=wiki;
	ImageIcon icone = new ImageIcon("resources/inter.jpg");
	ImageIcon icon=new ImageIcon(icone.getImage().getScaledInstance(15, 15, Image.SCALE_DEFAULT));
	this.setIcon(icon);
this.addMouseListener(new MouseAdapter()  
{  
    public void mouseClicked(MouseEvent e)  
    {  
    	if(Desktop.isDesktopSupported()){
    		if(Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
    			URI uri;
    			
    				try {
    					uri = new URI("http://vetbrain.fr/wiki/index.php?title="+wikipage);
    					Desktop.getDesktop().browse(uri);
    				} catch (URISyntaxException | IOException a) {
    					// TODO Auto-generated catch block
    					a.printStackTrace();
    				}
    			
    			
    		}
    	}

    }  
}); 	

	
}
public void setWikipage(String wikipage) {
	this.wikipage = wikipage;
}

}






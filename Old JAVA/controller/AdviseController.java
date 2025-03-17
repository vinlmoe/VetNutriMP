package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import application.VetNutri;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Advise;

public class AdviseController  implements Initializable {

@FXML
private TextField adviseNameText;
@FXML
private TextArea adviseArea;
@FXML
private ListView<Advise> adviseList;


@FXML
public void add() {
adviseList.getItems().add(new Advise("",""));
adviseList.refresh();
adviseList.getSelectionModel().selectLast();
}

@FXML
public void remove() {
if (adviseList.getSelectionModel().getSelectedItem()!=null) {
	adviseList.getItems().remove(adviseList.getSelectionModel().getSelectedItem());
	adviseList.refresh();
	actualise(null, null);
}
}



	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {

	adviseList.getSelectionModel().selectedItemProperty()
    .addListener((observable, oldValue, newValue) -> {
		if (newValue!=null) {
			actualise(newValue, oldValue);
			}
	});	
	adviseNameText.setVisible(false);
	adviseArea.setVisible(false);
	}

	
	public void setMainApp(   ArrayList<Advise> list) {
		
		list=(list==null?new ArrayList<Advise>():list);
	adviseList.getItems().addAll(list);
		
	}

public void actualise (Advise n, Advise o) {
	adviseNameText.commitValue();
	adviseArea.commitValue();
	if (o!=null) {
	o.setName(adviseNameText.getText());
	o.setText(adviseArea.getText());}
	if (n!=null) {
		adviseNameText.setVisible(true);
		adviseArea.setVisible(true);
		adviseNameText.setText(n.getName());
		adviseArea.setText(n.getText());
	}else {
		adviseNameText.setVisible(false);
		adviseArea.setVisible(false);
	}
	
}
public ArrayList<Advise> getItems(){
	if (adviseList.getSelectionModel().getSelectedItem()!=null) {
	adviseNameText.commitValue();
	adviseArea.commitValue();
	adviseList.getSelectionModel().getSelectedItem().setName(adviseNameText.getText());
	adviseList.getSelectionModel().getSelectedItem().setText(adviseArea.getText());
	}
	return  new ArrayList<Advise>(adviseList.getItems());
}
	

		
		}

package controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

public class ProgressController implements Initializable {
	@FXML
private	ProgressBar pb;
	
	private int max=100;
	private int curent;
	private Stage stage;
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		pb.setProgress(0);
	}
	public void setStage(Stage stage) {
		this.stage = stage;
	}
public void setProg(double d) {
	pb.setProgress(d/max);

System.out.println(pb.getProgress());
	if (d>=max) {
		stage.close();
	}
}
public ProgressBar getPb() {
	return pb;
}
public void setMax(int d) {
	max=d;
}
public void close() {
	stage.close();
}
}

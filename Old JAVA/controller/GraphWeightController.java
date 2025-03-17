package controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import org.jfree.chart.fx.ChartViewer;

import DataStruct.CurveP;
import DataStruct.LineWeight;
import DataStruct.curveList;
import application.GFun;
import application.VetNutri;
import graph.component.Chart;
import graph.component.ChartWeight;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Advise;
import model.Espece;
import model.PdfExport;

public class GraphWeightController  implements Initializable {
	private MainWinController mainApp;
ResourceBundle resource;
	private ChartWeight chartCreator;
	@FXML
private AnchorPane	weightAnchor;
@FXML
private TextField initialWText;
@FXML
private TextField finalWText;
@FXML
private TextField PercentText1;
@FXML
private TextField PercentText2;
@FXML 
private DatePicker dateBegin;
@FXML 
private CheckBox CheckPrint;
@FXML 
private CheckBox GrowthCheck;
@FXML 
private ComboBox<CurveP> growthCombo;

private ChartViewer WeightView=new ChartViewer();

@FXML
public void update() {
	
	System.out.println("up");
	if (mainApp.getAnim() !=null) {
		LineWeight lw=null;
		CurveP c=null;
		if (CheckPrint.isSelected()) {
		 lw=getLine();}
		if (GrowthCheck.isSelected()) {
			 c= growthCombo.getValue();}
				
			
	WeightView.setChart(
			chartCreator.createGraphWeight(mainApp.getAnim(), lw,c ));
		
	}
}

public LineWeight getLine() {
	if(! initialWText.getText().isBlank()&
		!	PercentText1.getText().isBlank()&
		!	PercentText2.getText().isBlank()	&
		! finalWText.getText().isBlank()&
		
		dateBegin.getValue()!=null) {
	
	LineWeight lw=new LineWeight(
	Float.parseFloat(GFun.noPoint(  initialWText.getText())),
	Float.parseFloat(GFun.noPoint(  PercentText1.getText())),
	Float.parseFloat(GFun.noPoint(  PercentText2.getText())),
	Float.parseFloat(GFun.noPoint(  finalWText.getText())),
	dateBegin.getValue(),
	1,
	1	);
	

	return lw;}
	
return null;
}




@FXML
public void createPDF() {
	update();
	PdfExport pe=new PdfExport();
	LineWeight lw =getLine();
	if (lw!=null) {
	pe.createcurvePdf(mainApp.getAnim(), lw, resource);}
}

@FXML
public void growthPdf() {
	update();
	PdfExport pe=new PdfExport();
	
	if (growthCombo.getValue()!=null) {
	pe.createcurvePdf(mainApp.getAnim(), growthCombo.getValue(), resource);}
}
	
	@Override 
	public void initialize(URL location, ResourceBundle resources) {
		chartCreator=new ChartWeight(resources);
		resource=resources;
		weightAnchor.getChildren().add(WeightView);
AnchorPane.setTopAnchor(WeightView,(double) 0);
AnchorPane.setLeftAnchor(WeightView,(double) 0);
AnchorPane.setRightAnchor(WeightView,(double) 0);
AnchorPane.setBottomAnchor(WeightView,(double) 0);
finalWText.focusedProperty().addListener((observable, oldValue, newValue) -> {
	if (newValue==false) {
			update();}});	
	initialWText.focusedProperty().addListener((observable, oldValue, newValue) -> {
		if (newValue==false) {
				update();}});	
PercentText1.focusedProperty().addListener((observable, oldValue, newValue) -> {
	if (newValue==false) {
			update();}});	
PercentText2.focusedProperty().addListener((observable, oldValue, newValue) -> {
	if (newValue==false) {
			update();}});		

dateBegin.valueProperty().addListener((observable, oldValue, newValue) -> {
	if (newValue!=null) {
		update();}});		
dateBegin.focusedProperty().addListener(new ChangeListener<Boolean>() {
    @Override
    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
        if (!newValue){
            dateBegin.setValue(dateBegin.getConverter().fromString(dateBegin.getEditor().getText()));
        }
    }
});
growthCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
	if (newValue!=null) {
		update();}});		

GrowthCheck.selectedProperty().addListener((observable, oldValue, newValue) -> {
	
		update();});		
	
CheckPrint.selectedProperty().addListener((observable, oldValue, newValue) -> {

		update();});		
	}
	public void setMainApp( MainWinController mwc ) {
	mainApp=mwc;
		
	}
	public void updateGrowth() {
		if(mainApp.getAnim()!=null) {
			growthCombo.setItems(
					curveList.getList(
							Espece.getEnumFromStringId(
									mainApp.getAnim().getEspece())));
			growthCombo.getSelectionModel().clearSelection();
		}else {
			growthCombo.getSelectionModel().clearSelection();
		}
		
	}


		}

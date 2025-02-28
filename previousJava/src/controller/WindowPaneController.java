package controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.commons.io.FilenameUtils;

import DataStruct.AlimP;
import application.DataConnector;
import application.TextConstant;
import application.VetNutri;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.stage.FileChooser.ExtensionFilter;
import model.AdjustSaveEv;
import model.AlimDBList;
import model.AlimSaver;
import model.AlimentEv;
import model.AlimentUnif;
import model.Animal;
import model.AnimalEv;
import model.DataAccess;
import model.DataPack;
import model.ListAdjust;
import model.ListRecette;
import model.Recette;
import model.ReferenceEv;
import model.alimDB;
import model.listAlim;
import model.listAnim;

public class WindowPaneController implements Initializable {
	private String RatUUID;
	private String ConsUUID;
	private AlimentUnif alimU;
	private Stage stage;
	private ResourceBundle bundle;
	private MainWinController mainWin;
	private VetNutri mainApp;

	@FXML
	private VBox MainPane;
	@FXML
	private Label versionLab;

	@FXML 
	private MenuItem newAnimalItem; 
	private ProgressController   AWCont=null;
	@FXML 
	private void handleNewAnimalItem() { 
		mainApp.nAnimWindow();
	} 
	@FXML 
	private void foodEditor() { 
		mainApp.editAlimWindow();;
	} 
	@FXML
	private void equationEditor() {
		mainApp.equationWindow();
	}
	@FXML
	private void analyzeFood() {

		mainApp.analyzeFoodWindow();

	}
	@FXML
	private void createOrdonnance() {
		mainApp.ordonnanceWindow();
	}
	@FXML
	private void createPDFCons() {
		mainApp.exportPDFConsult();
	}
	@FXML
	private void createPDFRat() {
		mainApp.exportPDFRationt();
	}

	@FXML
	private void Option() {
		mainApp.optionWindow();
	}
	@FXML
	private void referenceEditor() {
		mainApp.referenceWindow(false);
	}
	@FXML
	private void referenceDisEditor() {
		mainApp.referenceWindow(true);
	}
	@FXML
	private void biblioEditor() {
		mainApp.biblioWindow();
	}
	@FXML
	private void methodEditor() {
		mainApp.methodWindow();
	}
	@FXML
	private void handleSave() {
		if (mainApp!=null&mainWin!=null) {
			if (mainWin.prepaSave()) {
				DataConnector.UpdateConsultation(mainApp.getCurrentAnimal(),null); } 	}
	}

	@FXML
	private void FoodExport() {
		mainApp.ExportWindow("food");
	}
	@FXML
	private void MethodExport() {
		mainApp.ExportWindow("method");
	}
	@FXML
	private void AnimalExport() {
		mainApp.ExportWindow("animal");
	}
	@FXML
	private void RecipeExport() {
		mainApp.ExportWindow("recipe");
	}
	@FXML
	private void RefExport() {
		mainApp.ExportWindow("ref");
	}

	@FXML
	public void FoodImport() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		File file =fileChooser.showOpenDialog(stage);
		listAlim l;
		ArrayList<AlimentUnif>al;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);

				l = (listAlim) ois.readObject();
				DataConnector.UpdateListAlim(l);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block

				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();



			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
					}
				}
			}}
		mainApp.refreshAlimData();
	}

	@FXML
	public void AnimalImport() {
		FileChooser fileChooser = new FileChooser();

		
			fileChooser.setTitle("Open Resource File");
			File file =fileChooser.showOpenDialog(stage);

			if (file != null) {
				AWCont=mainApp.progressWindow();
				if (AWCont!=null) {

				try {
					Task<Void> task = new Task<Void>() {
						@Override
						public Void call() throws Exception {
							FileInputStream 	fis = null;
							ObjectInputStream 	ois = null;
							Connection conn=null;
							try {

								listAnim l;
								updateProgress(0,100);
								Thread.sleep(100);
								fis = null;
								ois = null;
								fis = new FileInputStream(file);
								ois = new ObjectInputStream(fis);

								l = (listAnim) ois.readObject();
								conn= DataConnector.connectAnim();
								updateProgress(30,100);
								Thread.sleep(100);

								int tot=l.size();
								int i=0;
								for (Animal an:l.getListAnim()) {
									DataConnector.UpdateConsultationOldUnique(an, conn);
									i++;


									updateProgress(30+(i), 30+tot);
								}
							}catch (ClassNotFoundException e) {
								e.printStackTrace();
								// TODO Auto-generated catch block
							throw e;
							} catch (ClassCastException e) {
								e.printStackTrace();
								// TODO Auto-generated catch block
								throw e;
							}catch (IOException e) {
								e.printStackTrace();
								// TODO Auto-generated catch block
								throw e;
							}
							finally {
								DataConnector.close(conn);
								ois.close();
								fis.close();
							}

							return null ;
						}
					};

					AWCont.getPb()
					.progressProperty().bind(task.progressProperty());
					task.setOnSucceeded(wse -> {
						AWCont.close();
						mainApp.refreshAnimData();
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setContentText("Importé avec succés ");
						alert.show();
					});
					task.setOnFailed(evt -> {
						 
						AWCont.close();
						mainApp.refreshAlimData();
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+task.getException().getMessage());

						alert.show();
					});
					new Thread(  task).start();



				} catch (ClassCastException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					// TODO Auto-generated catch block
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();





				}
			}
		}
	}
	@FXML
	public void RecipeImport() {
		FileChooser fileChooser = new FileChooser();
		ProgressController   AWCont=null;

		fileChooser.setTitle("Open Recipe File");
		File file =fileChooser.showOpenDialog(stage);
		ListRecette l;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				l = (ListRecette) ois.readObject();
				DataConnector.updateRecette(l);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();
					}
				}
			}}
		mainApp.refreshRefData();
	}

	@FXML
	public void MethodImport() {
		FileChooser fileChooser = new FileChooser();


		fileChooser.setTitle("Open Recipe File");
		File file =fileChooser.showOpenDialog(stage);
		ListAdjust l;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				l = (ListAdjust) ois.readObject();
				DataConnector.updateListMethod(l);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();
					}
				}
			}}
		mainApp.refreshRefData();
	}

	@FXML
	public void FoodImportEv() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Resource File");
		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("VetBrain food file", "*.vbrf"));
		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("Excel File ","*.xlsx"));
		File file =fileChooser.showOpenDialog(stage);

	

			if (file != null) {
				AWCont=mainApp.progressWindow();
				if (AWCont!=null) {
				if (FilenameUtils.getExtension(file.getAbsolutePath()).equals("xlsx")) {
					DataPack.createFileEvolve(file.getAbsolutePath(), mainApp.getVet());
					mainApp.refreshAlimData();
					for (alimDB a:mainApp.getVet().getAlDBL().values()) {
						DataConnector.addAlimDB(a, null);
					}
					mainApp.setVet(mainApp.getVet());
				}else {


					try {



						Task<Void> task = new Task<Void>() {
							@Override
							public Void call() throws Exception {
								FileInputStream 	fis = null;
								ObjectInputStream 	ois = null;
								Connection conn=null;
								AlimSaver as=null;
								try {

									ArrayList<AlimentEv>al;
									updateProgress(0,100);
									Thread.sleep(100);
									fis = null;
									ois = null;
									fis = new FileInputStream(file);
									ois = new ObjectInputStream(fis);
									updateProgress(15,100);

									as= ( AlimSaver) ois.readObject();
									if (as!=null) {
										AlimDBList adb=as.getDb();
										al=as.getListAl();
										conn= DataConnector.connect();
										for (alimDB db:adb.values()) {
										DataConnector.addAlimDB(db,conn);}
								mainApp.getVet().addAlDBL(adb);
									
									updateProgress(30,100);
									Thread.sleep(100);

									int tot=al.size();
									int i=0;
									for(AlimentEv alim:al) {
										i++;


										updateProgress(30+(i), 30+tot);

										DataConnector.UpdateAlim(alim, conn);
									}}
								}catch (ClassNotFoundException e) {
									// TODO Auto-generated catch block
									throw e;
									} catch (ClassCastException e) {
										// TODO Auto-generated catch block
										throw e;
									}catch (IOException e) {
										// TODO Auto-generated catch block
										throw e;
									}
								finally {
									DataConnector.close(conn);
									ois.close();
									fis.close();
								}

								return null ;
							}
						};

						AWCont.getPb()
						.progressProperty().bind(task.progressProperty());
						task.setOnSucceeded(wse -> {
							AWCont.close();
							mainApp.refreshAlimData();
							Alert alert = new Alert(AlertType.INFORMATION);
							alert.setTitle("Success");

							alert.setContentText("Importé avec succés ");

							alert.show();

						});
						task.setOnFailed(evt -> {
							 
									AWCont.close();
									mainApp.refreshAlimData();
									Alert alert = new Alert(AlertType.ERROR);
									alert.setTitle("Error");

									alert.setContentText(" "+task.getException().getMessage());

									alert.show();
								});
						

						new Thread(  task).start();
					} catch (ClassCastException e) {
						// TODO Auto-generated catch block

						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();




					}}
				}}

	}

	@FXML
	public void AnimalImportEv() {
		FileChooser fileChooser = new FileChooser();
		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("VetBrain animal file", "*.vbra"));

	
			fileChooser.setTitle("Open Resource File");
			File file =fileChooser.showOpenDialog(stage);
		
			FileInputStream 	fis = null;
			ObjectInputStream 	ois = null;
			if (file != null) {
				AWCont=mainApp.progressWindow();
				if (AWCont!=null) {
				try {
				


					Task<Void> task = new Task<Void>() {
						@Override
						public Void call() throws Exception {
							FileInputStream 	fis = null;
							ObjectInputStream 	ois = null;
							Connection conn=null;
							try {

								ArrayList<AnimalEv> l;
								updateProgress(0,100);
								Thread.sleep(100);
								fis = null;
								ois = null;
								fis = new FileInputStream(file);
								ois = new ObjectInputStream(fis);

								l = (ArrayList<AnimalEv>) ois.readObject();
								conn= DataConnector.connectAnim();
								updateProgress(30,100);
								Thread.sleep(100);

								int tot=l.size();
								int i=0;
								for (AnimalEv an:l) {
									i++;


									updateProgress(30+(i), 30+tot);
									DataConnector.UpdateConsultation(an, conn);
								}
							}catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
							throw e;
							} catch (ClassCastException e) {
								// TODO Auto-generated catch block
								throw e;
							}catch (IOException e) {
								// TODO Auto-generated catch block
								throw e;
							}
							finally {
								DataConnector.close(conn);
								ois.close();
								fis.close();
							}

							return null ;
						}
					};

					AWCont.getPb()
					.progressProperty().bind(task.progressProperty());
					task.setOnSucceeded(wse -> {
						AWCont.close();
						mainApp.refreshAnimData();
						Alert alert = new Alert(AlertType.INFORMATION);
						alert.setTitle("Success");
						alert.setContentText("Importé avec succés ");
						alert.show();
					});
					task.setOnFailed(evt -> {
						 
						AWCont.close();
						mainApp.refreshAlimData();
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+task.getException().getMessage());

						alert.show();

					});
					new Thread(  task).start();
			
					}finally {
				
			}
				}}
	}
	@FXML
	public void RecipeImportEv() {
		FileChooser fileChooser = new FileChooser();
		ProgressController   AWCont=null;
		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("VetBrain recipe file", "*.vbre"));
		fileChooser.setTitle("Open Recipe File");
		File file =fileChooser.showOpenDialog(stage);
		ArrayList<Recette> l;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				l = (ArrayList<Recette>) ois.readObject();
				DataConnector.updateRecette(l);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();
					}
				}
			}}
		mainApp.refreshRefData();
	}

	@FXML
	public void MethodImportEv() {
		FileChooser fileChooser = new FileChooser();

		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("VetBrain method file", "*.vbrm"));
		fileChooser.setTitle("Open Recipe File");
		File file =fileChooser.showOpenDialog(stage);
		ArrayList<AdjustSaveEv> l;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				l = (ArrayList<AdjustSaveEv>) ois.readObject();
				DataConnector.updateListMethod(l);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();
					}
				}
			}}
		mainApp.refreshRefData();
	}
	@FXML
	public void RefImportEv() {
		FileChooser fileChooser = new FileChooser();
		fileChooser .getExtensionFilters().addAll(new ExtensionFilter("VetBrain need file", "*.vbrr"));

		fileChooser.setTitle("Open Reference File");
		File file =fileChooser.showOpenDialog(stage);
		ArrayList<ReferenceEv> l;
		FileInputStream 	fis = null;
		ObjectInputStream 	ois = null;
		if (file != null) {

			try {
				fis = new FileInputStream(file);
				ois = new ObjectInputStream(fis);
				l = (ArrayList<ReferenceEv>) ois.readObject();
				DataConnector.updateListReference(l, null);
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Success");

				alert.setContentText("Importé avec succés ");

				alert.show();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassCastException e) {
				// TODO Auto-generated catch block
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
				e.printStackTrace();
			}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Error");

				alert.setContentText(" "+e.getMessage());

				alert.show();
			}  finally {
				try {
					ois.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Error");

					alert.setContentText(" "+e.getMessage());

					alert.show();
				} finally {
					try {
						fis.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						Alert alert = new Alert(AlertType.ERROR);
						alert.setTitle("Error");

						alert.setContentText(" "+e.getMessage());

						alert.show();
						e.printStackTrace();
					}
				}
			}}
		mainApp.refreshRefData();
	}
	@FXML
	private void handleOpenAnimal() {

		if (mainApp!=null) {
			mainApp.selectAnimWindow();}
	}
	@Override 
	public void initialize(URL location, ResourceBundle resources) { 
		versionLab.setText(TextConstant.VERSION.nameToString());


	} 


	public void SetPane(VetNutri mainApp, ResourceBundle bun) {

		FXMLLoader loader = new FXMLLoader();
		loader.setResources(bun);
		loader.setLocation(VetNutri.class.getResource("/view/MainWindow-bibis.fxml"));


		try {
			MainPane.getChildren().clear();
			MainPane.getChildren().add((SplitPane)loader.load());
			mainWin=loader.getController();
			mainWin.setMainApp(mainApp);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MainWinController getMainWin() {
		return mainWin;
	}


	public void setMainApp(VetNutri mainApp, Stage stage) {
		this.mainApp=mainApp;
		this.stage=stage;
		this.stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			public void handle(WindowEvent we) {
				mainApp.save();
				System.exit(0);
				mainApp.closeAll();
				stage. close();
			}
		});        
	}
}
package application;


import javafx.beans.binding.Bindings;
import java.util.ArrayList;
import java.util.Date;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.mariuszgromada.math.mxparser.License;

import DataStruct.AlimP;
import DataStruct.AnimP;
import DataStruct.BiblioP;
import DataStruct.EquationConsP;
import DataStruct.ReferenceP;
import Enumerise.EquationKind;
import controller.AnimalSelectorControler;
import controller.BiblioEditorControler;
import controller.EquationEditorControler;
import controller.ExportController;
import controller.FirstController;
import controller.FoodAnalyzerController;
import controller.FoodEditorControler;
import controller.FoodSelectorControler;
import controller.FormulationEditorControler;
import controller.MainWinController;
import controller.MethodEditorControler;
import controller.NewAnimalController;
import controller.OptionController;
import controller.OrdonnanceController;
import controller.ProgressController;
import controller.RecipeEditorControler;
import controller.ReferenceEditorControler;
import controller.WindowPaneController;
import equation.Equation;
import javafx.application.Application;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.AdjustSaveEv;
import model.Advise;
import model.AlimDBList;
import model.AlimentEv;
import model.AlimentUnif;
import model.AnimalEv;
import model.BiblioRef;
import model.ConsultationEv;
import model.DataAccess;
import model.DataPack;
import model.Espece;
import model.ListAdjust;
import model.ListReference;
import model.RationCalculator;
import model.Recette;
import model.Reference;
import model.ReferenceEv;
import model.RemplirForm;
import model.TypeAlim;
import model.Vet;
import model.listAlim;
import model.listAnim;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

public class VetNutri extends Application {
	private Stage primaryStage;
	private Stage recetteStage;
	private Stage SelectAlimStage;
	boolean maj=false;
	private Stage SelectAnimStage;
	private Stage EquationStage;
	private Stage FoodAnalStage;
	private Stage MethodStage;
	private Stage ReferenceStage;
	private Stage BiblioStage;
	private Stage nAnimStage;
	private ProgressController PCW;
	private ProgressController PCW2;
	private boolean first=false;
	private Vet vet;
	private ObservableList<AlimP> mAlimList= FXCollections.observableArrayList();
	private ObservableList<AnimP> mAnimList= FXCollections.observableArrayList();
	private VBox rootLayout;
	private static DataPack packe=new DataPack();
	private static  DataAccess dat=new DataAccess();
	private static ResourceBundle bun;

	private static ObservableList<Recette> mainListRecette;
	private static ObservableList<ReferenceP> mainListRefEv;
	private static ObservableList<EquationConsP> mainListEqu;
	private static ObservableList<BiblioP> mainListBiblio;
	private static ObservableList<AdjustSaveEv> mainListMethod;
	private  AnimalEv anim;
	private static WindowPaneController winCont;
	private static MainWinController mainCont;
	private static FoodAnalyzerController FAC;
	public AlimDBList alDB;
	private  SimpleFloatProperty font= new SimpleFloatProperty(12);

	public static void main(String[] args) {

		//	listAlim list=	dat.readAlimMAJ(); //si un fichier est selectionn�, r�cup�rer le fichier puis sont path et l'afficher dans le champs de texte

		/*		if (list!=null){
					listAlim listorig= dat.readAlim();
					try {
						dat.writeAlimentBack(listorig);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					for (int i=0; i<list.size(); i++){
						boolean touch =false;
						if (list.getAlim(i).getTypeAliment().equals(TypeAlim.COMPLET)||list.getAlim(i).getTypeAliment().equals(TypeAlim.COMPLEMENTAIRE)||list.getAlim(i).getTypeAliment().equals(TypeAlim.BARF)){
							listorig.addAlim(list.getAlim(i));

						}

					}
					try {
						dat.writeAliment(listorig);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}}*/
		/*ListReference l=dat.readReference();
for (Reference r:l.getListReference()) {
	DataConnector.UpdateReference(r);
}
		/*		ListAdjust listAd=dat.readAdjustMAJ();
				if (listAd!=null){
					ListAdjust listorigad= dat.readListAdjust();
					for (int i=0; i<listAd.size(); i++){
				listorigad.addAdjust(listAd.getAdjustByID(i));

						}

				try {
					dat.writeListAdjust(listorigad);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	}
		 */

		License.iConfirmNonCommercialUse("Sébastien Lefebvre");

		launch(VetNutri.class, args);}



	@Override
	public void start(Stage primaryStage) {
		try {
			PCW=progressWindow();
			if (PCW!=null) {

				


				Task<Void> task = new Task<Void>() {
					@Override
					public Void call() throws Exception {
					


						try {
							updateProgress(0,100);
							DataConnector.testAnim();
							updateProgress(10,100);
							DataConnector.testRef();
							updateProgress(20,100);
							DataConnector.testAlim();
							DataConnector.testAlim();
							updateProgress(30,100);
						 maj=DataConnector.MainUpdaterAlim();
							updateProgress(40,100);
							 DataConnector.MainUpdaterRecipe();
								updateProgress(50,100);
							DataConnector.MainUpdaterRef();;
							updateProgress(60,100);
							bun=ResourceBundle.getBundle("language/label", new Locale("FR", "fr"));
							updateProgress(70,100);
							alDB=DataConnector.getAlimDB();
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}


						updateProgress(100,100);
						Thread.sleep(100);


						return null ;
					}
				};

				PCW.getPb()
				.progressProperty().bind(task.progressProperty());
				task.setOnSucceeded(wse -> {
					PCW.close();
					this.vet=dat.readVet();
					if (vet==null) {
						first=true;
						vet=new Vet();
						vet.addAdvise(new Advise("Transition 6 jours", "Pour réaliser la transition alimentaire sur 6 jours :\n"
								+ "-	2 premiers jours : donner 75% de l’ancienne alimentation et 25% de la nouvelle\n"
								+ "-	2 jours suivants : donner 50% de chaque\n"
								+ "-	2 derniers jours : donner 25% de l’ancienne alimentation et 75% de la nouvelle\n"
								+ "-	Enfin ne donner que la nouvelle alimentation\n"
								+ ""));
						vet.addAdvise(new Advise("Avertissement", "En cas de diarrhées, vomissements, perte ou prise de poids non voulue, ou des refus de l’alimentation, après avoir prévenu votre vétérinaire traitant, vous pouvez nous contacter pour adapter la ration."
							
								));
						vet.getAlDBL().add(alDB.get("0"));
						vet.getAlDBL().add(alDB.get("2"));
						vet.getAlDBL().add(alDB.get("VF24"));
					}
					if (vet.getAlDBL()==null) {
						vet.setAlDBL(new AlimDBList());
						vet.getAlDBL().add(alDB.get("0"));
						vet.getAlDBL().add(alDB.get("2"));
						vet.getAlDBL().add(alDB.get("VF24"));
					}
					else if(maj) {
						System.out.println("maj in");
						vet.getAlDBL().add(alDB.get("VF24"));
					}
					setVet(vet);
					
					firstWindow( vet, alDB);
					PCW2=progressWindow();
					Task<Void> task2 = new Task<Void>() {
						@Override
						public Void call() throws Exception {



							try {
								updateProgress(0,100);
								mainListRefEv=DataConnector.getReferenceList(null, "");
								updateProgress(10,100);

								mainListEqu=DataConnector.getEquationList(null, "");
								updateProgress(20,100);
								mainListBiblio=DataConnector.getBiblioList(null,"");
								updateProgress(30,100);
								mainListMethod=DataConnector.getMethodList(null,"");
								updateProgress(40,100);

								mainListRecette=DataConnector.readAllRecette();
								updateProgress(50,100);

								mAlimList=DataConnector.getAllAlim(null,vet.getAlDBL().condition());
								updateProgress(60,100);
								mainListEqu=DataConnector.getEquationList(null, "");
								updateProgress(70,100);

						
								mAnimList=DataConnector.getAnimList(null,"", bun);
								updateProgress(90,100);
								mainListRefEv=DataConnector.getReferenceList(null, "");
								
								updateProgress(95,100);
								Equation equa;
								if(mainListEqu.size()>=1) {
									for (EquationConsP eq:mainListEqu) {
										if(eq.getEquation().getKind()==EquationKind.ENERGYDENSITY) {
											equa=eq.getEquation();
											mAlimList=equa.runAlim(mAlimList);
										}
										
									}
								}
								
								updateProgress(90,100);
								mainListRefEv=DataConnector.getReferenceList(null, "");
								updateProgress(100,100);

							} catch (Exception e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}


							updateProgress(100,100);
							Thread.sleep(100);


							return null ;
						}
					};




					//	DataConnector.eraseAll(mAnimList);

					PCW2.getPb()
					.progressProperty().bind(task2.progressProperty());
					task2.setOnSucceeded(wsa -> {
						PCW2.close();
						this.primaryStage=primaryStage;
						this.primaryStage.setTitle("VetNutri 2");
						initRootLayout();
						primaryStage.show();
						if (first) {
							this.optionWindow();
						}
						});
					new Thread(  task2).start();
				});

				new Thread(  task).start();

				//DataConnector.UpdateListAlim	(dat.readAlim());
				//	DataConnector.Initiate();
				/*	mainListAnim=dat.readAnim();
			mainListRef=dat.readReference();*/


			}
		}finally {

		}

	}


	public void initRootLayout() {
		try {
			// Load root layout from fxml file.
			FXMLLoader loader = new FXMLLoader();
			loader.setResources(bun);            
			URL fxmlLocation =getClass().getResource("/view/WindowPane.fxml");
			loader.setLocation(fxmlLocation);
			rootLayout = (VBox) loader.load();

			rootLayout .styleProperty().bind(Bindings.format("-fx-font-size: %.2fpt;", font)); 
			winCont=loader.getController();
			winCont.setMainApp(this, primaryStage);        
			// Show the scene containing the root layout.
			Scene scene = new Scene(rootLayout);
			//          jmetro.setScene(scene);      
			primaryStage.setScene(scene);
			primaryStage.show();
			//racourcis
			//Sauvegarde
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.S,
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {


						save();


						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});

			//Creer une nouvelle consultation 
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.N, 
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {
						System.out.println("Key Pressed: " + keyComb);
						if (mainCont!=null) {    	
							mainCont.NewConsultation(null);

						}
						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});
			//Creer une nouvelle ration
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.R, 
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {
						System.out.println("Key Pressed: " + keyComb);
						if (mainCont!=null) {
							mainCont.NewRation(null);
						}
						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});

			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.Q,
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {
						System.out.println("Key Pressed: " + keyComb);
						equationWindow();

						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.B,
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {
						System.out.println("Key Pressed: " + keyComb);          	        	
						biblioWindow();   	  
						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});

			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.E, 
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {
						System.out.println("Key Pressed: " + keyComb);

						editAlimWindow( );


						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});
			scene.addEventFilter(KeyEvent.KEY_PRESSED, new EventHandler<KeyEvent>() {
				final KeyCombination keyComb = new KeyCodeCombination(KeyCode.A, 
						KeyCombination.CONTROL_DOWN);
				public void handle(KeyEvent ke) {
					if (keyComb.match(ke)) {


						selectAnimWindow( );


						ke.consume(); // <-- stops passing the event to next node
					}
				}
			});


		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void SetMainCont() {
		winCont.SetPane(this,bun);
		mainCont=winCont.getMainWin();
	}

	public AnimalEv getCurrentAnimal() {
		return this.anim;
	}
	public ObservableList<AlimP> getListAlim() {
		return this.mAlimList;
	}
	public ConsultationEv getConsult(String UUID) {
		return this.anim.getList().getConsultByUUID(UUID);
	}
	public AlimentEv selectAlimWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/FoodView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			FoodEditorControler AWCont=loaderAW.getController();
			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);
			// Show the scene containing the root layout.
			SelectAlimStage=new Stage();
			AWCont.setMainApp(this, SelectAlimStage, false, vet);
			AWCont.Update();
			SelectAlimStage.setScene(scene);
			SelectAlimStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			SelectAlimStage.initOwner(primaryStage);

			SelectAlimStage.showAndWait();

			return AWCont.getData();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public void selectAlimWindow( RecipeEditorControler rec)  {
		try {
			if (rec!= null) {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/FoodView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			FoodEditorControler AWCont=loaderAW.getController();
			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);
			// Show the scene containing the root layout.
			SelectAlimStage=new Stage();
			AWCont.setMainApp(this, SelectAlimStage, false, vet, rec);
			AWCont.Update();
			SelectAlimStage.setScene(scene);
			SelectAlimStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			SelectAlimStage.initOwner(primaryStage);

			SelectAlimStage.showAndWait();

			}
		}
		catch (IOException e) {
			e.printStackTrace();
		
		}

	}
	public void selectAlimWindow(MainWinController rec)  {
		try {
			if (rec!= null) {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/FoodView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			FoodEditorControler AWCont=loaderAW.getController();
			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);
			// Show the scene containing the root layout.
			SelectAlimStage=new Stage();
			AWCont.setMainApp(this, SelectAlimStage, false, vet, rec);
			AWCont.Update();
			SelectAlimStage.setScene(scene);
			SelectAlimStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			SelectAlimStage.initOwner(primaryStage);

			SelectAlimStage.showAndWait();

			}
		}
		catch (IOException e) {
			e.printStackTrace();
		
		}

	}
	public void editAlimWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/FoodView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			FoodEditorControler AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			SelectAlimStage=new Stage();
			AWCont.setMainApp(this, SelectAlimStage, true, vet);
			AWCont.Update();
			SelectAlimStage.setScene(scene);
			SelectAlimStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			SelectAlimStage.initOwner(primaryStage);

			SelectAlimStage.showAndWait();
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}


	public void firstWindow(Vet vet, AlimDBList alDBL )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/firstView.fxml"));

			TabPane winLayout = (TabPane) loaderAW.load();
			FirstController AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			Stage    Stagef=new Stage();
			AWCont.setMainApp( Stagef, this, vet, alDBL);

			Stagef.setScene(scene);
			Stagef.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			Stagef.initOwner(primaryStage);

			Stagef.showAndWait();
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}
	public void optionWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/mOption.fxml"));

			BorderPane winLayout = (BorderPane) loaderAW.load();
			OptionController AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			Stage    Stagef=new Stage();
			AWCont.setMainApp( Stagef, this, vet);

			Stagef.setScene(scene);
			Stagef.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			Stagef.initOwner(primaryStage);

			Stagef.showAndWait();
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}

	public Recette selectRecetteWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/RecipeView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			RecipeEditorControler AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			recetteStage=new Stage();
			AWCont.setMainApp(this, recetteStage, true);
			AWCont.Update();
			recetteStage.setScene(scene);
			recetteStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			recetteStage.initOwner(primaryStage);

			recetteStage.showAndWait();
			
			mainListRecette=AWCont.getmRefList();

			return AWCont.getData();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	public Recette selectFormulaWindow(RationCalculator calc, ReferenceEv ref )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/FormulView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			FormulationEditorControler AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			recetteStage=new Stage();
			AWCont.setMainApp(this, recetteStage, true, calc, ref);
			AWCont.Update();
			recetteStage.setScene(scene);
			recetteStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			recetteStage.initOwner(primaryStage);

			recetteStage.showAndWait();
			Task<Void> task = new Task<Void>() {
				@Override
				public Void call() throws Exception {

					mainListRecette=DataConnector.readAllRecette();
					return null ;
				}
			};


			new Thread(task).run();
			return AWCont.getData();
		}
		catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}
	
	public void editRecetteWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/RecipeView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			RecipeEditorControler AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			recetteStage=new Stage();
			AWCont.setMainApp(this, recetteStage, false);
			AWCont.Update();
			recetteStage.setScene(scene);
			recetteStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			recetteStage.initOwner(primaryStage);

			recetteStage.showAndWait();
			Task<Void> task = new Task<Void>() {
				@Override
				public Void call() throws Exception {

					mainListRecette=DataConnector.readAllRecette();
					return null ;
				}
			};


			new Thread(task).run();
			}
		catch (IOException e) {
			e.printStackTrace();

		}

	}
	public void analyzeFoodWindow( )  {
		try {
			if (FoodAnalStage==null  ) {

				FoodAnalStage=new Stage();}
			if (!FoodAnalStage.isShowing()){
				FXMLLoader loaderAW = new FXMLLoader();
				loaderAW.setResources(bun);
				loaderAW.setLocation(VetNutri.class.getResource("/view/FoodAnalyzer.fxml"));

				AnchorPane winLayout = (AnchorPane) loaderAW.load();
				FAC=loaderAW.getController();

				// Show the scene containing the root layout.
				Scene scene = new Scene(winLayout);

				// Show the scene containing the root layout.
				FoodAnalStage=new Stage();
				FAC.setMainApp(this, FoodAnalStage, false, vet);
				//  AWCont.Update();
				FoodAnalStage.setScene(scene);
				FoodAnalStage.initModality(Modality.WINDOW_MODAL);

				// Specifies the owner Window (parent) for new window
				//FoodAnalStage.initOwner(primaryStage);             
				FoodAnalStage.show();

			}

		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}
	public void selectAnimWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/animalSelectView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			AnimalSelectorControler AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			SelectAnimStage=new Stage();
			AWCont.setMainApp(this, SelectAnimStage, false);
			AWCont.Update();
			SelectAnimStage.setScene(scene);
			SelectAnimStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			SelectAnimStage.initOwner(primaryStage);

			SelectAnimStage.showAndWait();}

		catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void equationWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/EquaEditor.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			EquationEditorControler   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			EquationStage=new Stage();
			AWCont.setMainApp(this, EquationStage, false);
			AWCont.Update();
			EquationStage.setScene(scene);
			EquationStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			EquationStage.initOwner(primaryStage);

			EquationStage.showAndWait();
			ObservableList<EquationConsP>bib=DataConnector.getEquationList(null,"");
			if(bib!=null) {
				mainListEqu=bib;
			}
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void ordonnanceWindow( )  {
		if (anim!=null ) {
			if (mainCont!=null) {	
				ConsultationEv cons=	mainCont.getConsultation();
				if(cons!=null) {
					try {

						FXMLLoader loaderAW = new FXMLLoader();
						loaderAW.setResources(bun);
						loaderAW.setLocation(VetNutri.class.getResource("/view/ordonnanceView.fxml"));

						BorderPane winLayout = (BorderPane) loaderAW.load();
						OrdonnanceController   AWCont=loaderAW.getController();

						// Show the scene containing the root layout.
						Scene scene = new Scene(winLayout);

						// Show the scene containing the root layout.
						EquationStage=new Stage();
						AWCont.setMainApp(anim, cons, vet, this, EquationStage);

						EquationStage.setScene(scene);
						EquationStage.initModality(Modality.WINDOW_MODAL);

						// Specifies the owner Window (parent) for new window
						EquationStage.initOwner(primaryStage);

						EquationStage.showAndWait();

					}
					catch (IOException e) {
						e.printStackTrace();

					}
				}}}

	}

	public void referenceWindow(boolean dis )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/RefEditor.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			ReferenceEditorControler   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			ReferenceStage=new Stage();
			AWCont.setMainApp(this, ReferenceStage, dis);
			AWCont.Update();
			ReferenceStage.setScene(scene);
			ReferenceStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			ReferenceStage.initOwner(primaryStage);

			ReferenceStage.showAndWait();
			ObservableList<ReferenceP>bib=DataConnector.getReferenceList(null,"");

			if(bib!=null) {
				mainListRefEv=bib;
			}
			if(mainCont!=null) {
				mainCont.SaveData();
				mainCont.UpdateAnimal();
			}
		}
		catch (IOException e) {
			e.printStackTrace();

		}


	}
	public void biblioWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/BiblioEditor.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			BiblioEditorControler   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			BiblioStage=new Stage();
			AWCont.setMainApp(this, BiblioStage, false);
			AWCont.Update();
			BiblioStage.setScene(scene);
			BiblioStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			BiblioStage.initOwner(primaryStage);

			BiblioStage.showAndWait();
			ObservableList<BiblioP>bib=DataConnector.getBiblioList(null,"");
			if(bib!=null) {
				mainListBiblio=bib;
			}
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}
	public ProgressController progressWindow( )  {
		ProgressController   AWCont=null;
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/progress.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			Stage stage=new Stage();

			AWCont.setStage(stage);
			stage.setScene(scene);
			stage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			stage.initOwner(primaryStage);

			stage.show();

		}
		catch (IOException e) {
			e.printStackTrace();

		}
		return AWCont;
	}
	public void methodWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/MethodView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			MethodEditorControler   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			MethodStage=new Stage();
			AWCont.setMainApp(this, MethodStage, false);
			AWCont.Update();
			MethodStage.setScene(scene);
			MethodStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window
			MethodStage.initOwner(primaryStage);

			MethodStage.showAndWait();
			ObservableList<AdjustSaveEv>bib=DataConnector.getMethodList(null,"");
			if(bib!=null) {
				mainListMethod=bib;
			}
		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void ExportWindow( String kind)  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/Export.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			ExportController   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			MethodStage=new Stage();
			AWCont.setMainApp(this, MethodStage, false,kind, vet);

			MethodStage.setScene(scene);
			MethodStage.initModality(Modality.WINDOW_MODAL);

			// Specifies the owner Window (parent) for new window

			MethodStage.showAndWait();

		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}
	public void nAnimWindow( )  {
		try {
			FXMLLoader loaderAW = new FXMLLoader();
			loaderAW.setResources(bun);
			loaderAW.setLocation(VetNutri.class.getResource("/view/newAnimView.fxml"));

			AnchorPane winLayout = (AnchorPane) loaderAW.load();
			NewAnimalController   AWCont=loaderAW.getController();

			// Show the scene containing the root layout.
			Scene scene = new Scene(winLayout);

			// Show the scene containing the root layout.
			nAnimStage=new Stage();
			AWCont.setMainApp( nAnimStage);

			nAnimStage.setScene(scene);
			nAnimStage.initModality(Modality.WINDOW_MODAL);
			// Specifies the owner Window (parent) for new window
			nAnimStage.initOwner(primaryStage);
			nAnimStage.showAndWait();
			if (AWCont.getAnim()!=null) {
				DataConnector.UpdateConsultation(AWCont.getAnim(),null);
				save();
				refreshAnimData();
				this.setAnimal(AWCont.getAnim().getUUID());
			}

		}
		catch (IOException e) {
			e.printStackTrace();

		}

	}

	public void save() {
		if(this.mainCont!=null) {
			if (mainCont.prepaSave()) {
				mainCont.SaveData();
		

				DataConnector.UpdateConsultation(getCurrentAnimal(),null);
				mAnimList=DataConnector.getAnimList(null,"", bun);
			} 	}
	}
	public void setAnimal(String animUUID) {
		
		this.anim=DataConnector.readAnimal(animUUID);
	
		if(mainCont==null) {
			winCont.SetPane(this, bun);
			mainCont=winCont.getMainWin();
			mainCont.setAnimal();
		}else {
			mainCont.setAnimal();}
	}
	public ObservableList<AnimP> getmAnimList() {
		return mAnimList;
	}
	public ObservableList<AlimP> getmAlimList() {
		return mAlimList;
	}
	public void removeAlim(AlimP al) {
		mAlimList.remove(al);
	}
	public void addAlim(AlimP al) {
		mAlimList.add(al);
	}
	public void addAlimCons(AlimentEv alimentEv, boolean newRat) {
		if(mainCont!=null) {
			if (newRat) {
				mainCont.newAlimInNewRat(alimentEv);
			}else {
				mainCont.newAlimInOldRat(alimentEv); 
			}

		}
	}
	public ObservableList<EquationConsP> getmEquationList() {

		return mainListEqu;
	}
	public ObservableList<ReferenceP> getmReferenceList() {


		return mainListRefEv;
	}
	public ObservableList<BiblioP> getmBiblioList() {

		return mainListBiblio;
	}
	public ObservableList<Recette> getmRecetteList() {

		return mainListRecette;
	}
	public ObservableList<AdjustSaveEv> getmMethodList() {
		ObservableList<AdjustSaveEv> ol=FXCollections.observableArrayList();
		if (anim!=null) {
			for( AdjustSaveEv as:mainListMethod) {
				if (as.getEsp().equals(Espece.CH)|as.getEsp().getUUID().equals(anim.getEspece()))
					ol.add(as);
			}
			return ol;
		}
		return mainListMethod;
	}
	public ObservableList<AdjustSaveEv> getMethodList() {


		return mainListMethod;
	}
	public static ObservableList<ReferenceEv> getMainListRef(String esp, boolean dis ) {
		ObservableList<ReferenceEv>response= FXCollections.observableArrayList();
		for(ReferenceP re: mainListRefEv) {

			if (re.getReference().getConsistent()==1&re.getReference().isDisease()==dis &(  re.getReference().getSpecies().equals(esp)|re.getEspeceStr().equals(Espece.CH.nameToString()))) {
				response.add(re.getReference());
			}
		}
		return response;
	}
	public static ObservableList<ReferenceEv> getMainListRef() {
		ObservableList<ReferenceEv>response= FXCollections.observableArrayList();
		for(ReferenceP re: mainListRefEv) {

			response.add(re.getReference());

		}
		return response;
	}
	
	
	public void refreshAlimData() {
		mAlimList=DataConnector.getAllAlim(null, vet.getAlDBL().condition());}

	public void refreshAnimData() {

		mAnimList=DataConnector.getAnimList(null,"", bun);
	}
	public void refreshRefData() {
		mainListRefEv=DataConnector.getReferenceList(null, "");
		mainListEqu=DataConnector.getEquationList(null, "");
		mainListBiblio=DataConnector.getBiblioList(null,"");
		mainListMethod=DataConnector.getMethodList(null,"");

		mainListRecette=DataConnector.readAllRecette();


	}
	public void closeAll() {
		if (FoodAnalStage!=null) {
			FoodAnalStage.close();
		}
	}


	public float getFont() {
		return font.get();
	}
	public void setFont(float font) {
		this.font.set(font);;
	}

	public ResourceBundle getBun() {
		return bun;
	}

	public void setVet(Vet vet) {
		this.vet=vet;
		setFont(vet.getScale());
		try {
			dat.writeVet(vet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public Vet getVet(){
		return vet;
	}
public void exportPDFConsult() {
	if (mainCont!=null) {
		mainCont.exportPP();
	}
}
public void exportPDFRationt() {
	if (mainCont!=null) {
		mainCont.exportRat();
	}
}
	public void setBun(ResourceBundle b) {
		bun=b;
	}
	
}

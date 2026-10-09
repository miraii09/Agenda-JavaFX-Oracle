package monagenda;

import monagenda.gui.AgendaView;
import monagenda.gui.CalendrierView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        AgendaView agendaView =new AgendaView();
        CalendrierView calendrierView =new CalendrierView();
        
        Tab tabAgenda =new Tab("  📋  Mes Événements  ");
        tabAgenda.setClosable(false);
        tabAgenda.setContent(agendaView.getRoot());
        
        Tab tabCalendrier=new Tab("  📅  Calendrier  ");
        tabCalendrier.setClosable(false);
        tabCalendrier.setContent(calendrierView.getRoot());
        
        TabPane tabPane= new TabPane(tabAgenda, tabCalendrier);
        tabPane.setTabMinWidth(160);
        tabPane.setStyle(
            "-fx-tab-min-height: 40px;" +
            "-fx-font-size: 13px;" +
            "-fx-font-family: 'Segoe UI';" +
            "-fx-background-color: #1a1a2e;");
        //fenetre
        Scene scene= new Scene(tabPane, 1280, 750);
        primaryStage.setTitle("Mon Agenda Personnel");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1000);
        primaryStage.setMinHeight(600);
        primaryStage.setOnCloseRequest(e-> monagenda.util.DBConnection.close());
        primaryStage.show();

        Platform.runLater(() -> calendrierView.afficherRappels());
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }
}
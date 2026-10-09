package monagenda.gui;

import monagenda.gestion.GestionEvenement;
import monagenda.gestion.GestionEvenementImpl;
import monagenda.model.Evenement;
import monagenda.model.Evenement.TypeEvenement;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AgendaView {
    private final GestionEvenement gest= new GestionEvenementImpl();

    private TableView<Evenement> table;
    private ObservableList<Evenement> data;
    private TextField champRecherche;
    private ComboBox<String> comboCritere;
    private ComboBox<TypeEvenement> comboType;
    private Label labelStatut;
    private BorderPane root;

    private static final DateTimeFormatter FMT_DATE= DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE= DateTimeFormatter.ofPattern("HH:mm");

    // Palette Rose
    private static final String C_ROSE_CLAIR  = "#fce4ec";
    private static final String C_BLANC       = "#ffffff";
    private static final String GRAD_ENTETE   = "linear-gradient(to right, #880e4f, #c2185b, #e91e8c)";
    private static final String GRAD_SIDEBAR  = "linear-gradient(to bottom, #ad1457, #c2185b, #e91e8c, #f06292)";

    public AgendaView(){
        construireInterface();
        chargerEvenements();
    }

    public BorderPane getRoot(){ return root; }

    private void construireInterface() {
        root= new BorderPane();
        root.setStyle("-fx-background-color:" + C_ROSE_CLAIR + ";");
        root.setTop(creerEntete());
        root.setLeft(creerPanneauGauche());
        root.setCenter(creerPanneauCentral());
        root.setBottom(creerStatut());
    }

    private HBox creerEntete() {
        HBox entete= new HBox(12);
        entete.setStyle(
            "-fx-background-color:" + GRAD_ENTETE + ";" +
            "-fx-effect: dropshadow(gaussian, rgba(194,24,91,0.45), 12, 0, 0, 3);"
        );
        entete.setPadding(new Insets(18, 28, 18, 28));
        entete.setAlignment(Pos.CENTER_LEFT);

        Label titre= new Label("Mon Agenda Personnel");
        titre.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        titre.setTextFill(Color.WHITE);

        Label trait= new Label("|");
        trait.setTextFill(Color.web("#ffffff55"));
        trait.setFont(Font.font(20));

        Label sousTitre= new Label("Gérez vos événements avec élégance");
        sousTitre.setFont(Font.font("Segoe UI", 13));
        sousTitre.setTextFill(Color.web("#f8bbd0"));

        Region espace= new Region();
        HBox.setHgrow(espace, Priority.ALWAYS);

        Label date= new Label(LocalDate.now().format(
            DateTimeFormatter.ofPattern("EEEE dd MMMM yyyy", java.util.Locale.FRENCH)));
        date.setFont(Font.font("Segoe UI", 12));
        date.setTextFill(Color.web("#f8bbd0"));

        entete.getChildren().addAll(titre, trait, sousTitre, espace, date);
        return entete;
    }

    private VBox creerPanneauGauche() {
        VBox panneau= new VBox(10);
        panneau.setPrefWidth(200);
        panneau.setMinWidth(200);
        panneau.setMaxWidth(200);
        panneau.setPadding(new Insets(20, 14, 20, 14));
        panneau.setStyle(
            "-fx-background-color:" + GRAD_SIDEBAR + ";" +
            "-fx-border-color:#880e4f;" +
            "-fx-border-width:0 2 0 0;"
        );

        Label secActions= sectionLabel("--- ACTIONS ---");
        Button btnAjouter= boutonGauche("+ Ajouter",    "#e91e8c", "#c2185b");
        Button btnModifier= boutonGauche("/ Modifier",  "#f06292", "#e91e8c");
        Button btnSupprimer= boutonGauche("x Supprimer","#ad1457", "#880e4f");

        btnAjouter.setOnAction(e -> ouvrirFormulaireAjout());
        btnModifier.setOnAction(e -> ouvrirFormulaireModification());
        btnSupprimer.setOnAction(e -> supprimerEvenement());

        Separator sep= new Separator();
        sep.setStyle("-fx-background-color:#ffffff44;");

        Label secFiltres = sectionLabel("--- FILTRES ---");
        Button btnTous      = boutonGauche("Tous",         "#f48fb1", "#f06292");
        Button btnAujourdhui= boutonGauche("Aujourd'hui",  "#e91e8c", "#c2185b");
        Button btnAVenir    = boutonGauche("A venir",      "#f06292", "#e91e8c");
        Button btnImportants= boutonGauche("Importants",   "#c2185b", "#ad1457");

        btnTous.setOnAction(e -> chargerEvenements());
        btnAujourdhui.setOnAction(e -> afficherAujourdhui());
        btnAVenir.setOnAction(e -> afficherAVenir());
        btnImportants.setOnAction(e -> afficherImportants());

        Region espace= new Region();
        VBox.setVgrow(espace, Priority.ALWAYS);

        Label version = new Label("Mon Agenda");
        version.setFont(Font.font("Georgia", 11));
        version.setTextFill(Color.web("#ffffff66"));
        version.setAlignment(Pos.CENTER);
        version.setMaxWidth(Double.MAX_VALUE);

        panneau.getChildren().addAll(
            secActions,
            btnAjouter, btnModifier, btnSupprimer,
            sep,
            secFiltres,
            btnTous, btnAujourdhui, btnAVenir, btnImportants,
            espace, version
        );
        return panneau;
    }

    private VBox creerPanneauCentral() {
        VBox centre= new VBox(10);
        centre.setPadding(new Insets(16, 16, 0, 16));
        VBox.setVgrow(centre, Priority.ALWAYS);

        TableView<Evenement> tableau= creerTableau();
        VBox.setVgrow(tableau, Priority.ALWAYS);

        centre.getChildren().addAll(creerBarreRecherche(), tableau);
        return centre;
    }

    private HBox creerBarreRecherche() {
        HBox barre= new HBox(10);
        barre.setAlignment(Pos.CENTER_LEFT);
        barre.setPadding(new Insets(12, 14, 12, 14));
        barre.setStyle(
            "-fx-background-color:" + C_BLANC + ";" +
            "-fx-background-radius:12;" +
            "-fx-border-color:#f8bbd0;" +
            "-fx-border-radius:12;" +
            "-fx-border-width:1.5;" +
            "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.12),10,0,0,3);"
        );

        comboCritere= new ComboBox<>();
        comboCritere.getItems().addAll("Titre", "Date (jj/mm/aaaa)", "Categorie");
        comboCritere.setValue("Titre");
        comboCritere.setPrefWidth(175);
        comboCritere.setStyle("-fx-font-size:13px;");

        champRecherche= new TextField();
        champRecherche.setPromptText("Rechercher un evenement...");
        champRecherche.setStyle(
            "-fx-font-size:13px;" +
            "-fx-border-color:#f8bbd0;" +
            "-fx-border-radius:6;" +
            "-fx-background-radius:6;"
        );
        HBox.setHgrow(champRecherche, Priority.ALWAYS);

        comboType= new ComboBox<>();
        comboType.getItems().addAll(TypeEvenement.values());
        comboType.setValue(TypeEvenement.REUNION);
        comboType.setPrefWidth(160);
        comboType.setVisible(false);
        comboType.setStyle("-fx-font-size:13px;");

        comboCritere.setOnAction(e ->{
            boolean cat = comboCritere.getValue().equals("Categorie");
            champRecherche.setVisible(!cat);
            comboType.setVisible(cat);
        });

        Button btnRechercher = boutonAction("Rechercher", "#e91e8c", "#c2185b");
        Button btnReset      = boutonAction("Effacer",    "#f48fb1", "#f06292");

        btnRechercher.setOnAction(e -> rechercherEvenements());
        btnReset.setOnAction(e -> { champRecherche.clear(); chargerEvenements(); });
        champRecherche.setOnAction(e -> rechercherEvenements());

        barre.getChildren().addAll(comboCritere, champRecherche, comboType, btnRechercher, btnReset);
        return barre;
    }

    private TableView<Evenement> creerTableau() {
        if (table!=null) return table;

        table= new TableView<>();
        table.setStyle("-fx-font-size:13px; -fx-font-family:'Segoe UI';");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Evenement, Number> colId = new TableColumn<>("#");
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()));
        colId.setPrefWidth(45);
        colId.setMaxWidth(50);

        TableColumn<Evenement, String> colTitre = new TableColumn<>("Titre");
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colTitre.setPrefWidth(200);

        TableColumn<Evenement, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDate().format(FMT_DATE)));
        colDate.setPrefWidth(95);

        TableColumn<Evenement, String> colHeure = new TableColumn<>("Heure");
        colHeure.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getHeure().format(FMT_HEURE)));
        colHeure.setPrefWidth(65);

        TableColumn<Evenement, String> colType = new TableColumn<>("Categorie");
        colType.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getType().getLibelle()));
        colType.setPrefWidth(115);

        TableColumn<Evenement, String> colPrio = new TableColumn<>("Priorite");
        colPrio.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getPriorite().getLibelle()));
        colPrio.setPrefWidth(90);
        colPrio.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || getTableRow() == null
                        || getTableRow().getItem() == null) {
                    setText(null); setStyle(""); return;
                }
                setText(item);
                switch (((Evenement) getTableRow().getItem()).getPriorite()) {
                    case HAUTE:   setStyle("-fx-text-fill:#880e4f;-fx-font-weight:bold;"); break;
                    case MOYENNE: setStyle("-fx-text-fill:#e91e8c;-fx-font-weight:bold;"); break;
                    case BASSE:   setStyle("-fx-text-fill:#f48fb1;"); break;
                    default:      setStyle("");
                }
            }
        });

        TableColumn<Evenement, String> colDesc = new TableColumn<>("Description");
        colDesc.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDescription()));
        colDesc.setPrefWidth(230);

        TableColumn<Evenement, String> colImp = new TableColumn<>("Imp.");
        colImp.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().isImportant() ? "(*)" : ""));
        colImp.setPrefWidth(55);

        table.getColumns().addAll(
            colId, colTitre, colDate, colHeure, colType, colPrio, colDesc, colImp);

        table.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) ouvrirFormulaireModification();
        });

        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Evenement item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty)   setStyle("");
                else if (item.isImportant()) setStyle("-fx-background-color:#fce4ec;");
                else                         setStyle("");
            }
        });

        data = FXCollections.observableArrayList();
        table.setItems(data);
        return table;
    }

    private HBox creerStatut() {
        HBox barre = new HBox(10);
        barre.setStyle("-fx-background-color:" + GRAD_ENTETE + ";");
        barre.setPadding(new Insets(7, 20, 7, 20));
        barre.setAlignment(Pos.CENTER_LEFT);

        Label point = new Label("●");
        point.setTextFill(Color.web("#f8bbd0"));

        labelStatut= new Label("Pret");
        labelStatut.setFont(Font.font("Segoe UI", 12));
        labelStatut.setTextFill(Color.web("#fce4ec"));

        barre.getChildren().addAll(point, labelStatut);
        return barre;
    }

    private Button boutonGauche(String texte, String couleur, String hover) {
        Button btn = new Button(texte);
        String s = "-fx-background-color:" + couleur + ";-fx-text-fill:white;" +
                   "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;" +
                   "-fx-background-radius:8;-fx-alignment:CENTER-LEFT;-fx-padding:8 12 8 12;" +
                   "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.15),4,0,0,1);";
        String h = "-fx-background-color:" + hover + ";-fx-text-fill:white;" +
                   "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;" +
                   "-fx-background-radius:8;-fx-alignment:CENTER-LEFT;-fx-padding:8 12 8 12;" +
                   "-fx-effect:dropshadow(gaussian,rgba(0,0,0,0.25),6,0,0,2);";
        btn.setStyle(s);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(36);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e -> btn.setStyle(s));
        return btn;
    }

    private Button boutonAction(String texte, String couleur, String hover) {
        Button btn = new Button(texte);
        String s = "-fx-background-color:" + couleur + ";-fx-text-fill:white;" +
                   "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;" +
                   "-fx-background-radius:8;-fx-padding:7 14 7 14;" +
                   "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.3),6,0,0,2);";
        String h = "-fx-background-color:" + hover + ";-fx-text-fill:white;" +
                   "-fx-font-size:12px;-fx-font-weight:bold;-fx-cursor:hand;" +
                   "-fx-background-radius:8;-fx-padding:7 14 7 14;" +
                   "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.5),8,0,0,3);";
        btn.setStyle(s);
        btn.setPrefHeight(36);
        btn.setOnMouseEntered(e -> btn.setStyle(h));
        btn.setOnMouseExited(e -> btn.setStyle(s));
        return btn;
    }

    private Label sectionLabel(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#ffffff99"));
        l.setPadding(new Insets(6, 0, 2, 0));
        l.setMaxWidth(Double.MAX_VALUE);
        return l;
    }

    private void chargerEvenements() {
        try {
            List<Evenement> liste = gest.listerTous();
            data.setAll(liste);
            statut("Tous les evenements", liste.size());
        } catch (Exception ex) { erreur(ex.getMessage()); }
    }

    private void statut(String ctx, int nb) {
        labelStatut.setText(ctx + "  -  " + nb + " evenement(s) affiche(s)");
    }

    private void rechercherEvenements() {
        try {
            List<Evenement> res;
            String critere = comboCritere.getValue();
            if (critere.equals("Categorie")) {
                TypeEvenement t = comboType.getValue();
                res = gest.rechercherParType(t);
                statut("Categorie : " + t.getLibelle(), res.size());
            } else {
                String mot = champRecherche.getText().trim();
                if (mot.isEmpty()) { chargerEvenements(); return; }
                if (critere.equals("Titre")) {
                    res = gest.rechercherParTitre(mot);
                    statut("Titre : " + mot, res.size());
                } else {
                    LocalDate d = LocalDate.parse(mot, FMT_DATE);
                    res = gest.rechercherParDate(d);
                    statut("Date : " + mot, res.size());
                }
            }
            data.setAll(res);
        } catch (Exception ex) { erreur("Format de date invalide.\nUtilisez le format : jj/mm/aaaa\nExemple : 15/06/2025"); }
    }

    private void afficherImportants() {
        try { List<Evenement> l = gest.listerImportants(); data.setAll(l); statut("Importants", l.size()); }
        catch (Exception ex) { erreur(ex.getMessage()); }
    }

    private void afficherAVenir() {
        try { List<Evenement> l = gest.listerAVenir(); data.setAll(l); statut("A venir", l.size()); }
        catch (Exception ex) { erreur(ex.getMessage()); }
    }

    private void afficherAujourdhui() {
        try { List<Evenement> l = gest.listerAujourdhui(); data.setAll(l); statut("Aujourd'hui", l.size()); }
        catch (Exception ex) { erreur(ex.getMessage()); }
    }

    private void ouvrirFormulaireAjout() {
        new EvenementDialog(null).showAndWait().ifPresent(evt -> {
            try { gest.ajouter(evt); chargerEvenements(); info("Evenement ajoute !"); }
            catch (Exception ex) { erreur("Ajout : " + ex.getMessage()); }
        });
    }

    private void ouvrirFormulaireModification() {
        Evenement sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Selectionnez un evenement a modifier."); return; }
        new EvenementDialog(sel).showAndWait().ifPresent(evt -> {
            try { gest.modifier(evt); chargerEvenements(); info("Evenement modifie !"); }
            catch (Exception ex) { erreur("Modification : " + ex.getMessage()); }
        });
    }

    private void supprimerEvenement() {
        Evenement sel = table.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Selectionnez un evenement a supprimer."); return; }
        Alert c = new Alert(Alert.AlertType.CONFIRMATION);
        c.setTitle("Confirmation");
        c.setHeaderText("Supprimer cet evenement ?");
        c.setContentText("« " + sel.getTitre() + " »");
        c.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try { gest.supprimer(sel.getId()); chargerEvenements(); info("Evenement supprime."); }
                catch (Exception ex) { erreur("Suppression : " + ex.getMessage()); }
            }
        });
    }

    private void info(String m)  {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private void alert(String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setHeaderText(null); a.setContentText(m); a.showAndWait();
    }
    private void erreur(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setHeaderText(null); a.setContentText(m); a.showAndWait();
        labelStatut.setText("Erreur : " + m);
    }
}

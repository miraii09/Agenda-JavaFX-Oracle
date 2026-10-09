package monagenda.gui;

import monagenda.gestion.GestionEvenement;
import monagenda.gestion.GestionEvenementImpl;
import monagenda.model.Evenement;
import monagenda.model.Evenement.Priorite;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;


public class CalendrierView {

    private static final DateTimeFormatter FMT_MOIS  = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.FRENCH);
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH);

    private static final String[] JOURS_SEMAINE = {"Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim"};

    // Palette Rose
    private static final String GRAD_NAV      = "linear-gradient(to right, #880e4f, #c2185b, #e91e8c)";
    private static final String C_FOND        = "#fce4ec";
    private static final String C_LEGENDE_BG  = "#fce4ec";
    private static final String C_CELLULE_BG  = "#ffffff";
    private static final String C_AUJOURD_BG  = "#fce4ec";
    private static final String C_AUJOURD_BOR = "#e91e8c";

    private final GestionEvenement gest = new GestionEvenementImpl();

    private BorderPane root;
    private GridPane   grille;
    private Label      labelMois;
    private YearMonth  moisCourant;

    private Map<LocalDate, List<Evenement>> evenementsParJour = new HashMap<>();

    public CalendrierView() {
        moisCourant = YearMonth.now();
        construireInterface();
        chargerEtAfficher();
    }

    public BorderPane getRoot() {
        return root;
    }

    private void construireInterface() {
        root = new BorderPane();
        root.setStyle("-fx-background-color:" + C_FOND + ";");
        root.setTop(creerBarreNavigation());
        root.setCenter(creerZoneCalendrier());
        root.setBottom(creerLegende());
    }

    private HBox creerBarreNavigation() {
        HBox barre = new HBox(20);
        barre.setAlignment(Pos.CENTER);
        barre.setPadding(new Insets(12, 20, 12, 20));
        barre.setStyle(
            "-fx-background-color:" + GRAD_NAV + ";" +
            "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.4),10,0,0,3);"
        );

        Button btnPrecedent = creerBoutonNav("< Mois precedent");
        Button btnSuivant   = creerBoutonNav("Mois suivant >");
        Button btnAujourdhui= creerBoutonNav("Aujourd'hui");
        btnAujourdhui.setStyle(btnAujourdhui.getStyle() + "-fx-background-color:#f48fb1;");

        labelMois = new Label();
        labelMois.setFont(Font.font("Georgia", FontWeight.BOLD, 18));
        labelMois.setTextFill(Color.WHITE);
        labelMois.setMinWidth(220);
        labelMois.setAlignment(Pos.CENTER);

        btnPrecedent.setOnAction(e -> { moisCourant = moisCourant.minusMonths(1); chargerEtAfficher(); });
        btnSuivant.setOnAction(e ->   { moisCourant = moisCourant.plusMonths(1);  chargerEtAfficher(); });
        btnAujourdhui.setOnAction(e ->{ moisCourant = YearMonth.now();            chargerEtAfficher(); });

        barre.getChildren().addAll(btnPrecedent, btnAujourdhui, labelMois, btnSuivant);
        return barre;
    }

    private Button creerBoutonNav(String texte) {
        Button btn = new Button(texte);
        btn.setStyle(
            "-fx-background-color:rgba(255,255,255,0.2);-fx-text-fill:white;" +
            "-fx-font-weight:bold;-fx-font-size:12px;-fx-cursor:hand;" +
            "-fx-background-radius:8;-fx-border-color:rgba(255,255,255,0.4);" +
            "-fx-border-radius:8;-fx-border-width:1;"
        );
        btn.setPrefHeight(32);
        return btn;
    }

    private ScrollPane creerZoneCalendrier() {
        VBox zone = new VBox(0);
        zone.setPadding(new Insets(10, 15, 10, 15));

        GridPane entetes = new GridPane();
        entetes.setHgap(4);
        for (int i = 0; i < 7; i++) {
            Label lbl = new Label(JOURS_SEMAINE[i]);
            lbl.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
            lbl.setTextFill(Color.web(i >= 5 ? "#c2185b" : "#880e4f"));
            lbl.setAlignment(Pos.CENTER);
            lbl.setMaxWidth(Double.MAX_VALUE);
            lbl.setPadding(new Insets(4, 0, 4, 0));
            GridPane.setHgrow(lbl, Priority.ALWAYS);
            entetes.add(lbl, i, 0);
        }

        grille = new GridPane();
        grille.setHgap(4);
        grille.setVgap(4);
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 7);
            cc.setHgrow(Priority.ALWAYS);
            grille.getColumnConstraints().add(cc);
        }

        zone.getChildren().addAll(entetes, grille);

        ScrollPane scroll = new ScrollPane(zone);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:" + C_FOND + ";-fx-background:" + C_FOND + ";");
        return scroll;
    }

    private HBox creerLegende() {
        HBox legende = new HBox(20);
        legende.setAlignment(Pos.CENTER);
        legende.setPadding(new Insets(8, 15, 8, 15));
        legende.setStyle(
            "-fx-background-color:" + C_LEGENDE_BG + ";" +
            "-fx-border-color:#f8bbd0;-fx-border-width:1 0 0 0;"
        );

        legende.getChildren().addAll(
            legendeItem("●", "#880e4f", "Priorite Haute"),
            legendeItem("●", "#e91e8c", "Priorite Moyenne"),
            legendeItem("●", "#f48fb1", "Priorite Basse"),
            legendeItem("●", "#f8bbd0", "Aucun evenement")
        );
        return legende;
    }

    private HBox legendeItem(String symbole, String couleur, String texte) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER_LEFT);
        Label point = new Label(symbole);
        point.setTextFill(Color.web(couleur));
        point.setFont(Font.font(16));
        Label desc = new Label(texte);
        desc.setFont(Font.font("Segoe UI", 11));
        desc.setTextFill(Color.web("#4a0e2a"));
        item.getChildren().addAll(point, desc);
        return item;
    }

    private void chargerEtAfficher() {
        String nomMois = moisCourant.format(FMT_MOIS);
        labelMois.setText(nomMois.substring(0, 1).toUpperCase() + nomMois.substring(1));

        evenementsParJour.clear();
        try {
            List<Evenement> tous = gest.listerTous();
            for (Evenement evt : tous) {
                if (YearMonth.from(evt.getDate()).equals(moisCourant)) {
                    evenementsParJour
                        .computeIfAbsent(evt.getDate(), k -> new ArrayList<>())
                        .add(evt);
                }
            }
        } catch (Exception ex) {
            afficherErreur("Impossible de charger les evenements : " + ex.getMessage());
        }

        construireGrille();
    }

    private void construireGrille() {
        grille.getChildren().clear();

        LocalDate premier  = moisCourant.atDay(1);
        LocalDate aujourd  = LocalDate.now();

        int debutColonne = premier.getDayOfWeek().getValue() - 1;
        int nbJours      = moisCourant.lengthOfMonth();

        int col = debutColonne;
        int row = 0;

        for (int i = 0; i < debutColonne; i++) {
            grille.add(cellulaVide(), i, 0);
        }

        for (int jour = 1; jour <= nbJours; jour++) {
            LocalDate date = moisCourant.atDay(jour);
            List<Evenement> evts = evenementsParJour.getOrDefault(date, Collections.emptyList());

            grille.add(creerCellule(date, evts, date.equals(aujourd)), col, row);

            col++;
            if (col == 7) { col = 0; row++; }
        }
    }

    private VBox creerCellule(LocalDate date, List<Evenement> evts, boolean estAujourdhui) {
        VBox cellule = new VBox(2);
        cellule.setAlignment(Pos.TOP_CENTER);
        cellule.setPrefHeight(80);
        cellule.setPadding(new Insets(5));
        cellule.setCursor(javafx.scene.Cursor.HAND);

        String styleFond = estAujourdhui
            ? "-fx-background-color:" + C_AUJOURD_BG + ";" +
              "-fx-border-color:" + C_AUJOURD_BOR + ";-fx-border-width:2;" +
              "-fx-border-radius:8;-fx-background-radius:8;" +
              "-fx-effect:dropshadow(gaussian,rgba(233,30,140,0.2),6,0,0,2);"
            : "-fx-background-color:" + C_CELLULE_BG + ";" +
              "-fx-border-color:#f8bbd0;-fx-border-width:1;" +
              "-fx-border-radius:8;-fx-background-radius:8;";
        cellule.setStyle(styleFond);

        Label lblJour = new Label(String.valueOf(date.getDayOfMonth()));
        lblJour.setFont(Font.font("Georgia", estAujourdhui ? FontWeight.BOLD : FontWeight.NORMAL, 13));
        lblJour.setTextFill(estAujourdhui ? Color.web("#880e4f") : Color.web("#4a0e2a"));

        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
            lblJour.setTextFill(Color.web("#c2185b"));
        }

        cellule.getChildren().add(lblJour);

        if (!evts.isEmpty()) {
            HBox points = new HBox(3);
            points.setAlignment(Pos.CENTER);

            List<Evenement> tries = evts.stream()
                .sorted(Comparator.comparingInt(e -> e.getPriorite().ordinal()))
                .collect(Collectors.toList());

            int max = Math.min(tries.size(), 3);
            for (int i = 0; i < max; i++) {
                Label pt = new Label("●");
                pt.setFont(Font.font(10));
                pt.setTextFill(couleurPriorite(tries.get(i).getPriorite()));
                points.getChildren().add(pt);
            }
            if (evts.size() > 3) {
                Label plus = new Label("+" + (evts.size() - 3));
                plus.setFont(Font.font("Segoe UI", 9));
                plus.setTextFill(Color.web("#c2185b"));
                points.getChildren().add(plus);
            }
            cellule.getChildren().add(points);

            if (!evts.isEmpty()) {
                Evenement premier = evts.get(0);
                String apercu = premier.getTitre();
                if (apercu.length() > 12) apercu = apercu.substring(0, 11) + "…";
                Label lblApercu = new Label(apercu);
                lblApercu.setFont(Font.font("Segoe UI", 10));
                lblApercu.setTextFill(Color.web("#880e4f"));
                cellule.getChildren().add(lblApercu);
            }
        }

        final String styleBase = styleFond;
        cellule.setOnMouseEntered(e ->
            cellule.setStyle(styleBase + "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.25),8,0,0,3);")
        );
        cellule.setOnMouseExited(e -> cellule.setStyle(styleBase));
        cellule.setOnMouseClicked(e -> afficherPopupJour(date, evts));

        return cellule;
    }

    private VBox cellulaVide() {
        VBox v = new VBox();
        v.setPrefHeight(80);
        v.setStyle("-fx-background-color:#fef7f9;-fx-border-radius:8;-fx-background-radius:8;");
        return v;
    }

    private void afficherPopupJour(LocalDate date, List<Evenement> evts) {
        Dialog<Void> popup = new Dialog<>();
        popup.setTitle("Evenements du " + date.format(FMT_DATE));
        popup.setHeaderText(null);
        popup.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        popup.getDialogPane().lookupButton(ButtonType.CLOSE).setStyle(
            "-fx-background-color:#e91e8c;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:6;");

        VBox contenu = new VBox(0);

        HBox entete = new HBox();
        entete.setStyle("-fx-background-color:linear-gradient(to right,#880e4f,#e91e8c);");
        entete.setPadding(new Insets(10, 20, 10, 20));
        String nomJour = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        nomJour = nomJour.substring(0, 1).toUpperCase() + nomJour.substring(1);
        Text titrePopup = new Text(nomJour + " " + date.format(FMT_DATE));
        titrePopup.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
        titrePopup.setFill(Color.WHITE);
        entete.getChildren().add(titrePopup);
        contenu.getChildren().add(entete);

        if (evts.isEmpty()) {
            Label vide = new Label("Aucun evenement ce jour.");
            vide.setFont(Font.font("Segoe UI", 13));
            vide.setPadding(new Insets(20));
            vide.setTextFill(Color.web("#c2185b"));
            contenu.getChildren().add(vide);
        } else {
            List<Evenement> tries = evts.stream()
                .sorted(Comparator.comparing(Evenement::getHeure))
                .collect(Collectors.toList());

            for (Evenement evt : tries) {
                contenu.getChildren().add(creerCarteEvenement(evt));
            }
        }

        ScrollPane scroll = new ScrollPane(contenu);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color:#fce4ec;-fx-background:#fce4ec;");
        scroll.setPrefHeight(Math.min(tries_height(evts), 400));

        popup.getDialogPane().setContent(scroll);
        popup.getDialogPane().setPrefWidth(420);
        popup.getDialogPane().setStyle("-fx-background-color:#fce4ec;");
        popup.showAndWait();
    }

    private int tries_height(List<Evenement> evts) {
        return 80 + evts.size() * 100;
    }

    private VBox creerCarteEvenement(Evenement evt) {
        VBox carte = new VBox(4);
        carte.setPadding(new Insets(10, 15, 10, 15));
        carte.setStyle("-fx-border-color:#f8bbd0;-fx-border-width:0 0 1 0;");

        String couleur = couleurPrioriteHex(evt.getPriorite());

        HBox ligne1 = new HBox(8);
        ligne1.setAlignment(Pos.CENTER_LEFT);

        Label barre = new Label("  ");
        barre.setStyle("-fx-background-color:" + couleur + ";-fx-background-radius:2;");
        barre.setMinWidth(4);
        barre.setPrefHeight(40);

        VBox infos = new VBox(3);

        HBox heureEtTitre = new HBox(8);
        heureEtTitre.setAlignment(Pos.CENTER_LEFT);
        Label lblHeure = new Label(evt.getHeure().format(FMT_HEURE));
        lblHeure.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblHeure.setTextFill(Color.web("#c2185b"));
        lblHeure.setStyle("-fx-background-color:#fce4ec;-fx-padding:2 6 2 6;-fx-background-radius:3;");

        Label lblTitre = new Label(evt.getTitre());
        lblTitre.setFont(Font.font("Georgia", FontWeight.BOLD, 13));
        lblTitre.setTextFill(Color.web("#4a0e2a"));
        if (evt.isImportant()) {
            lblTitre.setText("(*) " + evt.getTitre());
        }
        heureEtTitre.getChildren().addAll(lblHeure, lblTitre);

        HBox details = new HBox(10);
        Label lblType = new Label(evt.getType().getLibelle());
        lblType.setFont(Font.font("Segoe UI", 11));
        lblType.setTextFill(Color.web("#c2185b"));

        Label lblPrio = new Label("● " + evt.getPriorite().getLibelle());
        lblPrio.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lblPrio.setTextFill(Color.web(couleur));

        details.getChildren().addAll(lblType, lblPrio);
        infos.getChildren().addAll(heureEtTitre, details);

        if (evt.getDescription() != null && !evt.getDescription().isBlank()) {
            Label lblDesc = new Label(evt.getDescription());
            lblDesc.setFont(Font.font("Segoe UI", 11));
            lblDesc.setTextFill(Color.web("#880e4f"));
            lblDesc.setWrapText(true);
            infos.getChildren().add(lblDesc);
        }

        ligne1.getChildren().addAll(barre, infos);
        carte.getChildren().add(ligne1);
        return carte;
    }

    public void afficherRappels() {
        try {
            List<Evenement> aujourd = gest.listerAujourdhui();
            if (aujourd.isEmpty()) return;

            Dialog<Void> popup = new Dialog<>();
            popup.setTitle("Rappels du jour");
            popup.setHeaderText(null);
            popup.getDialogPane().getButtonTypes().add(ButtonType.OK);
            popup.getDialogPane().lookupButton(ButtonType.OK).setStyle(
                "-fx-background-color:#e91e8c;-fx-text-fill:white;-fx-font-weight:bold;-fx-background-radius:6;");

            VBox contenu = new VBox(0);

            HBox entete = new HBox();
            entete.setStyle("-fx-background-color:linear-gradient(to right,#880e4f,#e91e8c);");
            entete.setPadding(new Insets(12, 20, 12, 20));
            Text titre = new Text("  " + aujourd.size() + " evenement(s) aujourd'hui");
            titre.setFont(Font.font("Georgia", FontWeight.BOLD, 14));
            titre.setFill(Color.WHITE);
            entete.getChildren().add(titre);
            contenu.getChildren().add(entete);

            List<Evenement> tries = aujourd.stream()
                .sorted(Comparator.comparing(Evenement::getHeure))
                .collect(Collectors.toList());

            for (Evenement evt : tries) {
                contenu.getChildren().add(creerCarteEvenement(evt));
            }

            popup.getDialogPane().setContent(contenu);
            popup.getDialogPane().setPrefWidth(400);
            popup.getDialogPane().setStyle("-fx-background-color:#fce4ec;");
            popup.showAndWait();

        } catch (Exception ex) {
            afficherErreur("Erreur rappels : " + ex.getMessage());
        }
    }

    private Color couleurPriorite(Priorite p) {
        switch (p) {
            case HAUTE:   return Color.web("#880e4f");
            case MOYENNE: return Color.web("#e91e8c");
            case BASSE:   return Color.web("#f48fb1");
            default:      return Color.web("#f8bbd0");
        }
    }

    private String couleurPrioriteHex(Priorite p) {
        switch (p) {
            case HAUTE:   return "#880e4f";
            case MOYENNE: return "#e91e8c";
            case BASSE:   return "#f48fb1";
            default:      return "#f8bbd0";
        }
    }

    private void afficherErreur(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Erreur"); a.setHeaderText(null); a.setContentText(msg); a.showAndWait();
    }
}

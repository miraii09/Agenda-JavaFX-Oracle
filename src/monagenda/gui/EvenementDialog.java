package monagenda.gui;

import monagenda.model.Evenement;
import monagenda.model.Evenement.TypeEvenement;
import monagenda.model.Evenement.Priorite;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class EvenementDialog extends Dialog<Evenement> {

    private static final DateTimeFormatter FMT_DATE  = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FMT_HEURE = DateTimeFormatter.ofPattern("HH:mm");

    // Styles champs - thème rose
    private static final String S_NORMAL =
        "-fx-border-color:#f8bbd0;-fx-border-radius:5;-fx-background-radius:5;-fx-padding:5;";
    private static final String S_ERR =
        "-fx-border-color:#c2185b;-fx-border-width:2;-fx-border-radius:5;-fx-background-radius:5;-fx-padding:5;";
    private static final String S_OK =
        "-fx-border-color:#e91e8c;-fx-border-width:1.5;-fx-border-radius:5;-fx-background-radius:5;-fx-padding:5;";

    private TextField               champTitre;
    private TextField               champDate;
    private TextField               champHeure;
    private TextArea                areaDescription;
    private ComboBox<TypeEvenement> comboType;
    private ComboBox<Priorite>      comboPriorite;
    private CheckBox                checkImportant;
    private Label                   lblErrTitre, lblErrDate, lblErrHeure;

    private final Evenement existant;

    public EvenementDialog(Evenement evt) {
        this.existant = evt;
        setTitle(evt == null ? "Nouvel evenement" : "Modifier l'evenement");
        setHeaderText(null);
        construireFormulaire();
        configurerBoutons();
    }

    private void construireFormulaire() {

        // En-tête avec dégradé rose
        HBox entete = new HBox(10);
        entete.setStyle(
            "-fx-background-color:linear-gradient(to right,#880e4f,#c2185b,#e91e8c);" +
            "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.4),8,0,0,2);"
        );
        entete.setPadding(new Insets(14, 22, 14, 22));
        entete.setAlignment(Pos.CENTER_LEFT);
        Label icone = new Label(existant == null ? "+" : "/");
        icone.setFont(Font.font(20));
        icone.setTextFill(Color.WHITE);
        Text lblH = new Text(existant == null ? "Nouvel evenement" : "Modifier l'evenement");
        lblH.setFont(Font.font("Georgia", FontWeight.BOLD, 16));
        lblH.setFill(Color.WHITE);
        entete.getChildren().addAll(icone, lblH);

        // Grille formulaire
        GridPane g = new GridPane();
        g.setHgap(14);
        g.setVgap(5);
        g.setPadding(new Insets(18, 24, 12, 24));
        g.setStyle("-fx-background-color:#fef7f9;");

        ColumnConstraints c0 = new ColumnConstraints(140);
        ColumnConstraints c1 = new ColumnConstraints();
        c1.setHgrow(Priority.ALWAYS);
        c1.setMinWidth(280);
        g.getColumnConstraints().addAll(c0, c1);

        int r = 0;

        // Titre
        champTitre = champ(existant != null ? existant.getTitre() : "", "Ex : Reunion d'equipe");
        champTitre.textProperty().addListener((o, a, n) -> {
            if (n.length() > 100) champTitre.setText(a);
            else { String f = n.replaceAll("[<>{}\\[\\]\\\\]",""); if (!f.equals(n)) champTitre.setText(f); }
        });
        champTitre.focusedProperty().addListener((o,a,f) -> { if(!f) validerTitre(); });
        lblErrTitre = errLbl();
        g.add(lbl("Titre *"), 0, r); g.add(champTitre, 1, r++);
        g.add(lblErrTitre, 1, r++);

        // Date
        champDate = champ(existant != null ? existant.getDate().format(FMT_DATE) : "", "jj/mm/aaaa");
        champDate.textProperty().addListener((o, a, n) -> {
            String f = n.replaceAll("[^0-9/]","");
            if (f.length() > 10) f = f.substring(0,10);
            if (!f.equals(n)) { champDate.setText(f); return; }
            if (n.length() > a.length()) {
                if (n.length()==2 && !n.contains("/")) champDate.setText(n+"/");
                else if (n.length()==5 && n.chars().filter(c->c=='/').count()==1) champDate.setText(n+"/");
            }
        });
        champDate.focusedProperty().addListener((o,a,f) -> { if(!f) validerDate(); });
        lblErrDate = errLbl();
        g.add(lbl("Date * (jj/mm/aaaa)"), 0, r); g.add(champDate, 1, r++);
        g.add(lblErrDate, 1, r++);

        // Heure
        champHeure = champ(existant != null ? existant.getHeure().format(FMT_HEURE) : "", "hh:mm");
        champHeure.textProperty().addListener((o, a, n) -> {
            String f = n.replaceAll("[^0-9:]","");
            if (f.length() > 5) f = f.substring(0,5);
            if (!f.equals(n)) { champHeure.setText(f); return; }
            if (n.length() > a.length() && n.length()==2 && !n.contains(":"))
                champHeure.setText(n+":");
        });
        champHeure.focusedProperty().addListener((o,a,f) -> { if(!f) validerHeure(); });
        lblErrHeure = errLbl();
        g.add(lbl("Heure * (hh:mm)"), 0, r); g.add(champHeure, 1, r++);
        g.add(lblErrHeure, 1, r++);

        // Catégorie
        comboType = new ComboBox<>();
        comboType.getItems().addAll(TypeEvenement.values());
        comboType.setValue(existant != null ? existant.getType() : TypeEvenement.REUNION);
        comboType.setMaxWidth(Double.MAX_VALUE);
        comboType.setStyle("-fx-font-size:13px;");
        g.add(lbl("Categorie *"), 0, r); g.add(comboType, 1, r++); r++;

        // Priorité - avec couleurs roses
        comboPriorite = new ComboBox<>();
        comboPriorite.getItems().addAll(Priorite.values());
        comboPriorite.setValue(existant != null ? existant.getPriorite() : Priorite.MOYENNE);
        comboPriorite.setMaxWidth(Double.MAX_VALUE);
        comboPriorite.setStyle("-fx-font-size:13px;");
        comboPriorite.setCellFactory(lv -> new ListCell<>() {
            @Override protected void updateItem(Priorite p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); setStyle(""); return; }
                setText(p.getLibelle());
                switch(p) {
                    case HAUTE:   setStyle("-fx-background-color:#fce4ec;-fx-text-fill:#880e4f;-fx-font-weight:bold;"); break;
                    case MOYENNE: setStyle("-fx-background-color:#f8bbd0;-fx-text-fill:#c2185b;-fx-font-weight:bold;"); break;
                    case BASSE:   setStyle("-fx-background-color:#fef7f9;-fx-text-fill:#e91e8c;"); break;
                }
            }
        });
        comboPriorite.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Priorite p, boolean empty) {
                super.updateItem(p, empty);
                if (empty || p == null) { setText(null); return; }
                setText(p.getLibelle());
                switch(p) {
                    case HAUTE:   setStyle("-fx-text-fill:#880e4f;-fx-font-weight:bold;"); break;
                    case MOYENNE: setStyle("-fx-text-fill:#c2185b;-fx-font-weight:bold;"); break;
                    case BASSE:   setStyle("-fx-text-fill:#e91e8c;"); break;
                }
            }
        });
        g.add(lbl("Priorite *"), 0, r); g.add(comboPriorite, 1, r++); r++;

        // Description
        areaDescription = new TextArea(existant != null ? existant.getDescription() : "");
        areaDescription.setPromptText("Description optionnelle (max 500 caracteres)");
        areaDescription.setPrefRowCount(3);
        areaDescription.setWrapText(true);
        areaDescription.setStyle(
            "-fx-font-size:13px;" +
            "-fx-border-color:#f8bbd0;-fx-border-radius:5;"
        );
        areaDescription.textProperty().addListener((o,a,n) -> { if (n.length()>500) areaDescription.setText(a); });
        g.add(lbl("Description"), 0, r); g.add(areaDescription, 1, r++); r++;

        // Important
        checkImportant = new CheckBox("Marquer comme important");
        checkImportant.setSelected(existant != null && existant.isImportant());
        checkImportant.setFont(Font.font("Segoe UI", 13));
        checkImportant.setTextFill(Color.web("#c2185b"));
        checkImportant.setStyle("-fx-mark-color:#e91e8c;");
        g.add(new Label(""), 0, r); g.add(checkImportant, 1, r);

        VBox contenu = new VBox(0);
        contenu.getChildren().addAll(entete, g);
        getDialogPane().setContent(contenu);
        getDialogPane().setPrefWidth(540);
        getDialogPane().setStyle("-fx-background-color:#fef7f9;");
    }

    private boolean validerTitre() {
        String t = champTitre.getText().trim();
        if (t.isEmpty())        { err(champTitre, lblErrTitre, "Le titre est obligatoire."); return false; }
        if (t.length() < 3)     { err(champTitre, lblErrTitre, "Minimum 3 caracteres."); return false; }
        if (t.matches("[0-9]+")){ err(champTitre, lblErrTitre, "Le titre ne peut pas etre purement numerique."); return false; }
        ok(champTitre, lblErrTitre); return true;
    }
    private boolean validerDate() {
        try { LocalDate.parse(champDate.getText().trim(), FMT_DATE); ok(champDate, lblErrDate); return true; }
        catch (DateTimeParseException e) { err(champDate, lblErrDate, "Format invalide. Ex : 25/12/2025"); return false; }
    }
    private boolean validerHeure() {
        try { LocalTime.parse(champHeure.getText().trim(), FMT_HEURE); ok(champHeure, lblErrHeure); return true; }
        catch (DateTimeParseException e) { err(champHeure, lblErrHeure, "Format invalide. Ex : 14:30"); return false; }
    }
    private Evenement valider() {
        if (!(validerTitre() & validerDate() & validerHeure())) return null;
        return new Evenement(
            existant != null ? existant.getId() : 0,
            champTitre.getText().trim(),
            LocalDate.parse(champDate.getText().trim(), FMT_DATE),
            LocalTime.parse(champHeure.getText().trim(), FMT_HEURE),
            areaDescription.getText().trim(),
            comboType.getValue(),
            checkImportant.isSelected(),
            comboPriorite.getValue()
        );
    }

    private void configurerBoutons() {
        ButtonType ok  = new ButtonType(existant == null ? "Ajouter" : "Enregistrer", ButtonBar.ButtonData.OK_DONE);
        ButtonType ann = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        getDialogPane().getButtonTypes().addAll(ok, ann);
        getDialogPane().lookupButton(ok).setStyle(
            "-fx-background-color:linear-gradient(to right,#c2185b,#e91e8c);" +
            "-fx-text-fill:white;-fx-font-weight:bold;-fx-font-size:13px;" +
            "-fx-background-radius:8;" +
            "-fx-effect:dropshadow(gaussian,rgba(194,24,91,0.4),6,0,0,2);"
        );
        getDialogPane().lookupButton(ann).setStyle(
            "-fx-background-color:#f8bbd0;-fx-text-fill:#880e4f;" +
            "-fx-font-weight:bold;-fx-font-size:13px;-fx-background-radius:8;"
        );
        setResultConverter(b -> b == ok ? valider() : null);
    }

    private TextField champ(String val, String prompt) {
        TextField t = new TextField(val);
        t.setPromptText(prompt);
        t.setStyle(S_NORMAL);
        t.setFont(Font.font("Segoe UI", 13));
        return t;
    }
    private Label lbl(String texte) {
        Label l = new Label(texte);
        l.setFont(Font.font("Georgia", FontWeight.BOLD, 12));
        l.setTextFill(Color.web("#880e4f"));
        l.setWrapText(true);
        return l;
    }
    private Label errLbl() {
        Label l = new Label(" ");
        l.setFont(Font.font("Segoe UI", 11));
        l.setTextFill(Color.web("#c2185b"));
        l.setStyle("-fx-font-style:italic;");
        l.setMinHeight(14);
        return l;
    }
    private void err(TextField f, Label l, String m) { f.setStyle(S_ERR); l.setText(m); }
    private void ok(TextField f,  Label l)           { f.setStyle(S_OK);  l.setText(" "); }
}

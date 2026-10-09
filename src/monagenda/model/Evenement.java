package monagenda.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Evenement {

    public enum TypeEvenement {REUNION("Réunion"),
                               RENDEZ_VOUS("Rendez-vous"),
                               ANNIVERSAIRE("Anniversaire"),
                               RAPPEL("Rappel"),
                               CONGE("Congé"),
                               COURS("Cours"),
                               AUTRE("Autre");

        private final String libelle; //att
        TypeEvenement(String libelle){
            this.libelle = libelle; 
        } //constr
        public String getLibelle(){
            return libelle; 
        } //meth
        @Override public String toString(){
            return libelle; 
        } 
    }

    public enum Priorite {HAUTE("Haute"),
                          MOYENNE("Moyenne"),
                          BASSE("Basse");

        private final String libelle;
        Priorite(String libelle){ 
            this.libelle = libelle;
        }
        public String getLibelle(){ 
            return libelle; 
        }
        @Override public String toString(){ 
            return libelle; 
        }
    }

    private int id;
    private String titre;
    private LocalDate date;
    private LocalTime heure;
    private String description;
    private TypeEvenement type;
    private boolean important;
    private Priorite priorite;

    //const complet
    public Evenement(int id, String titre, LocalDate date, LocalTime heure,
                     String description, TypeEvenement type,
                     boolean important, Priorite priorite) {
        this.id= id;
        this.titre= titre;
        this.date= date;
        this.heure= heure;
        this.description= description;
        this.type= type;
        this.important= important;
        if(priorite != null){
            this.priorite= priorite;
        } else {
            this.priorite= Priorite.MOYENNE;
        }
    }

    //constr sans id ->ajout
    public Evenement(String titre, LocalDate date, LocalTime heure,
                     String description, TypeEvenement type,
                     boolean important, Priorite priorite) {
        this(0, titre, date, heure, description, type, important, priorite);
    }

    //constr sans priorité ->compatibilité
    public Evenement(int id, String titre, LocalDate date, LocalTime heure,
                     String description, TypeEvenement type, boolean important) {
        this(id, titre, date, heure, description, type, important, Priorite.MOYENNE);
    }

    public int getId(){ 
        return id; }
    public void setId(int id){ 
        this.id = id; }
    public String getTitre(){ 
        return titre; }
    public void setTitre(String t){ 
        this.titre = t; }
    public LocalDate getDate(){ 
        return date; }
    public void setDate(LocalDate d){ 
        this.date = d; }
    public LocalTime getHeure(){ 
        return heure; }
    public void setHeure(LocalTime h){ 
        this.heure = h; }
    public String getDescription(){ 
        return description; }
    public void setDescription(String d){ 
        this.description = d; }
    public TypeEvenement  getType(){ 
        return type; }
    public void setType(TypeEvenement t){
        this.type = t; }
    public boolean isImportant(){
        return important; }
    public void setImportant(boolean i){
        this.important = i; }
    public Priorite getPriorite(){ 
        return priorite; }
    public void setPriorite(Priorite p){
        if (p != null) {
            this.priorite= p;
        } else {
            this.priorite= Priorite.MOYENNE;
        }
    }

    @Override
    public String toString() {
        return "["+ type +"] "+ titre +" - "+ date
             +" à "+ heure +" ("+ priorite +")";
    }
}
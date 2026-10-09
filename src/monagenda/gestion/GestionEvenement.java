package monagenda.gestion;

import monagenda.model.Evenement;
import monagenda.model.Evenement.TypeEvenement;
import monagenda.model.Evenement.Priorite;
import java.time.LocalDate;
import java.util.List;

public interface GestionEvenement {
    List<Evenement> listerTous() throws Exception;
    int ajouter(Evenement e) throws Exception;
    void modifier(Evenement e) throws Exception;
    void supprimer(int id)throws Exception;
    List<Evenement> rechercherParTitre(String mot) throws Exception;
    List<Evenement> rechercherParDate(LocalDate date) throws Exception;
    List<Evenement> rechercherParType(TypeEvenement type) throws Exception;
    List<Evenement> listerImportants() throws Exception;
    List<Evenement> listerAVenir() throws Exception;
    List<Evenement> listerAujourdhui() throws Exception;
    List<Evenement> rechercherParPriorite(Priorite p) throws Exception;
}
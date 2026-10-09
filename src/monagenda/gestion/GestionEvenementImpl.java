package monagenda.gestion;

import monagenda.model.Evenement;
import monagenda.model.Evenement.TypeEvenement;
import monagenda.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class GestionEvenementImpl implements GestionEvenement {

    private Evenement mapper(ResultSet rs) throws SQLException{
        int id= rs.getInt("ID");
        String titre= rs.getString("TITRE");
        LocalDate date= rs.getDate("DATE_EVENEMENT").toLocalDate();
        LocalTime heure= rs.getTime("HEURE_EVENEMENT").toLocalTime();
        String description= rs.getString("DESCRIPTION");
        TypeEvenement type = TypeEvenement.valueOf(rs.getString("TYPE_EVENEMENT"));
        boolean important= "1".equals(rs.getString("IMPORTANT"));
        Evenement.Priorite priorite = Evenement.Priorite.MOYENNE;
        try{
            String p= rs.getString("PRIORITE");
            if(p!=null){
                priorite= Evenement.Priorite.valueOf(p);
            }
        }catch(SQLException ignored){}

        return new Evenement(id, titre, date, heure, description, type, important, priorite);
    }

    @Override
    public int ajouter(Evenement e) throws Exception {
        String sql = "INSERT INTO EVENEMENTS "
           + "(TITRE, DATE_EVENEMENT, HEURE_EVENEMENT, DESCRIPTION, TYPE_EVENEMENT, IMPORTANT, PRIORITE) "
           + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        Connection conn=DBConnection.getInstance();
        try (PreparedStatement ps=conn.prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, Date.valueOf(e.getDate()));
            ps.setTime(3, Time.valueOf(e.getHeure()));
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getType().name());
            ps.setString(6, e.isImportant() ? "1" : "0");
            ps.setString(7, e.getPriorite().name());
            ps.executeUpdate();
            try (ResultSet keys=ps.getGeneratedKeys()) {
                if(keys.next()) {
                    int newId=keys.getInt(1);
                    e.setId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    @Override
    public void modifier(Evenement e) throws Exception{
        String sql = "UPDATE EVENEMENTS SET TITRE=?, DATE_EVENEMENT=?, HEURE_EVENEMENT=?, "
           + "DESCRIPTION=?, TYPE_EVENEMENT=?, IMPORTANT=?, PRIORITE=? WHERE ID=?";
        try (PreparedStatement ps=DBConnection.getInstance().prepareStatement(sql)) {
            ps.setString(1, e.getTitre());
            ps.setDate(2, Date.valueOf(e.getDate()));
            ps.setTime(3, Time.valueOf(e.getHeure()));
            ps.setString(4, e.getDescription());
            ps.setString(5, e.getType().name());
            ps.setString(6, e.isImportant() ? "1" : "0");
            ps.setString(7, e.getPriorite().name());
            ps.setInt (8, e.getId()); 
            ps.executeUpdate();
        }
    }

    @Override
    public void supprimer(int id) throws Exception {
        String sql="DELETE FROM EVENEMENTS WHERE ID = ?";
        try(PreparedStatement ps=DBConnection.getInstance().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Evenement> listerTous() throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS ORDER BY DATE_EVENEMENT, HEURE_EVENEMENT",
            ps -> {}
        );
    }

    @Override
    public List<Evenement> rechercherParDate(LocalDate date) throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE DATE_EVENEMENT = ? ORDER BY HEURE_EVENEMENT",
            ps -> ps.setDate(1, Date.valueOf(date))
        );
    }

    @Override
    public List<Evenement> rechercherParTitre(String motCle) throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE UPPER(TITRE) LIKE ? ORDER BY DATE_EVENEMENT",
            ps -> ps.setString(1, "%" + motCle.toUpperCase() + "%")
        );
    }

    @Override
    public List<Evenement> rechercherParType(TypeEvenement type) throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE TYPE_EVENEMENT = ? ORDER BY DATE_EVENEMENT",
            ps -> ps.setString(1, type.name())
        );
    }

    @Override
    public List<Evenement> listerImportants() throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE IMPORTANT = '1' ORDER BY DATE_EVENEMENT",
            ps -> {}
        );
    }

    @Override
    public List<Evenement> listerAVenir() throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE DATE_EVENEMENT >= TRUNC(SYSDATE) "
          + "ORDER BY DATE_EVENEMENT, HEURE_EVENEMENT",
            ps -> {}
        );
    }

    @FunctionalInterface
    private interface ParamSetter {
        void set(PreparedStatement ps) throws SQLException;
    }

    private List<Evenement> executeQuery(String sql, ParamSetter setter) throws Exception {
        List<Evenement> liste=new ArrayList<>();
        try (PreparedStatement ps=DBConnection.getInstance().prepareStatement(sql)) {
            setter.set(ps);
            try (ResultSet rs=ps.executeQuery()) {
                while(rs.next()){
                    liste.add(mapper(rs));
                }
            }
        }
        return liste;
    }
    
    @Override
    public List<Evenement> listerAujourdhui() throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE DATE_EVENEMENT = TRUNC(SYSDATE) ORDER BY HEURE_EVENEMENT",
            ps -> {}
        );
    }

    @Override
    public List<Evenement> rechercherParPriorite(Evenement.Priorite priorite) throws Exception {
        return executeQuery(
            "SELECT * FROM EVENEMENTS WHERE PRIORITE = ? ORDER BY DATE_EVENEMENT, HEURE_EVENEMENT",
            ps -> ps.setString(1, priorite.name())
        );
    }
}
package monagenda.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    // Fichier de configuration local (non versionné), placé à la racine du projet
    private static final String CONFIG_FILE = "db.properties";

    private static Connection instance = null;

    private static Properties chargerConfiguration() throws SQLException {
        Properties p = new Properties();
        try (InputStream in = new FileInputStream(CONFIG_FILE)) {
            p.load(in);
        } catch (IOException e) {
            throw new SQLException(
                "Fichier " + CONFIG_FILE + " introuvable ou illisible. "
                + "Copiez db.properties.example en db.properties et renseignez vos paramètres Oracle.", e);
        }
        return p;
    }

    public static Connection getInstance() throws SQLException {
        if (instance == null || instance.isClosed()) {
            Properties p = chargerConfiguration();
            try {
                Class.forName("oracle.jdbc.OracleDriver");
                instance = DriverManager.getConnection(
                    p.getProperty("db.url"),
                    p.getProperty("db.user"),
                    p.getProperty("db.password"));
                System.out.println("Connexion Oracle établie avec succès.");
            } catch (ClassNotFoundException e) {
                throw new SQLException(
                    "Driver Oracle introuvable. Ajoutez ojdbc11.jar au classpath.\n"
                    + e.getMessage());
            }
        }
        return instance;
    }

    public static void close() {
        try {
            if (instance != null && !instance.isClosed()) {
                instance.close();
                System.out.println("Connexion Oracle fermée.");
            }
        } catch (SQLException e) {
            System.err.println("Erreur fermeture : " + e.getMessage());
        }
    }

    private DBConnection() {}
}
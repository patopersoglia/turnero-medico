package Logica;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Conexion {
    private static Connection con = null;
    private static final String URL = "jdbc:mysql://localhost/turnero1?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(Conexion.class.getName());

    public static Connection conectar() throws SQLException {
        // Si la conexión ya existe y no está cerrada, reutilizarla
        if (con != null) {
            try {
                if (!con.isClosed()) {
                    LOGGER.log(Level.INFO, "Reutilizando conexión existente a la base de datos.");
                    return con;
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "La conexión existente no es válida: {0}", e.getMessage());
            }
        }

        // Si no hay conexión o está cerrada, crear una nueva
        try {
            // Cargar el driver (mantenido por compatibilidad con versiones antiguas de JDBC)
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.log(Level.INFO, "Conectando a la base de datos...");
            con = DriverManager.getConnection(URL, USER, PASSWORD);
            LOGGER.log(Level.INFO, "Conexión exitosa a la base de datos.");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "No se encontró el driver JDBC: {0}", e.getMessage());
            throw new SQLException("No se encontró el driver JDBC", e);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error al conectar a la base de datos: {0}", e.getMessage());
            throw e; // Relanzar la excepción para que el código que llama pueda manejarla
        }
        return con;
    }

    // Método para cerrar la conexión explícitamente
    public static void cerrar() {
        if (con != null) {
            try {
                con.close();
                LOGGER.log(Level.INFO, "Conexión a la base de datos cerrada.");
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error al cerrar la conexión: {0}", e.getMessage());
            } finally {
                con = null;
            }
        }
    }
}
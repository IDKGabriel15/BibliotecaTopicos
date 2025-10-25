package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class ConexionBD {

    private static final String SERVIDOR = "localhost"; 
    private static final String PUERTO = "1433"; 
    private static final String NOMBRE_BD = "Biblioteca"; 
    private static final String USUARIO = "sa";
    private static final String CONTRASENA = "Roman_3119";
    
    private static final String URL_CONEXION = String.format(
            "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=true;trustServerCertificate=true;",
            SERVIDOR, PUERTO, NOMBRE_BD
    );

    public static Connection getConexion() throws SQLException {
        try {
            return DriverManager.getConnection(URL_CONEXION, USUARIO, CONTRASENA);
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos SQL Server:");
            e.printStackTrace();
            throw e;
        }
    }

    public static void cerrar(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void cerrar(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void cerrar(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
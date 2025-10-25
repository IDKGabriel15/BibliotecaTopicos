package DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entidades.PrestamoVista; 

public class PrestamoDAO {
    public List<PrestamoVista> obtenerPrestamosActivos() {
        List<PrestamoVista> listaPrestamos = new ArrayList<>();
        String sql = """
            SELECT 
                p.isbn, p.copy_no, p.prestamo_no, p.usuario_no,
                t.titulo, 
                (u.nombre + ' ' + u.apellido) AS nombre_usuario,
                p.fecha_pres, p.fecha_reg
            FROM Prestamo p
            JOIN Titulo t ON p.titulo_no = t.titulo_no
            JOIN Usuario u ON p.usuario_no = u.usuario_no
            ORDER BY p.fecha_pres DESC;
        """;

        try (Connection con = ConexionBD.getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                PrestamoVista p = new PrestamoVista(
                    rs.getString("prestamo_no"),
                    rs.getString("isbn"),
                    rs.getInt("copy_no"),
                    rs.getString("usuario_no"),
                    rs.getString("titulo"),
                    rs.getString("nombre_usuario"),
                    rs.getDate("fecha_pres"),
                    rs.getDate("fecha_reg") 
                );
                listaPrestamos.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al leer los préstamos activos.");
        }
        return listaPrestamos;
    }

    // Llama al SP para realizar un préstamo
    public String realizarPrestamo(String isbn, int copyNo, String usuarioNo, java.util.Date fechaPres) throws SQLException {
        String prestamoNoGenerado = null;
        String sql = "{CALL sp_RealizarPrestamo(?, ?, ?, ?)}";

        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {

            cstmt.setString(1, isbn);
            cstmt.setInt(2, copyNo);
            cstmt.setString(3, usuarioNo);
            cstmt.setDate(4, new java.sql.Date(fechaPres.getTime()));

            // El SP sp_RealizarPrestamo devuelve un ResultSet con el mensaje
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    // "Préstamo realizado con éxito. ID de préstamo: 2510220001"
                    String mensaje = rs.getString("Mensaje");
                    if (mensaje.contains("ID de préstamo:")) {
                         prestamoNoGenerado = mensaje.substring(mensaje.indexOf(":") + 2);
                    } else {
                        // Si el SP lanza un RAISERROR, se captura en el CATCH
                        // Pero si devuelve un mensaje de error (ej. "ya está en préstamo"), lo lanzamos
                        throw new SQLException(mensaje); 
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_RealizarPrestamo: " + e.getMessage());
            // Relanzamos la excepción para que la GUI la atrape
            throw e; 
        }
        return prestamoNoGenerado;
    }

    // Llama al SP para realizar una devolución
    public String realizarDevolucion(String isbn, int copyNo) throws SQLException {
        String mensajeExito = "";
        String sql = "{CALL sp_RealizarDevolucion(?, ?)}";

        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {

            cstmt.setString(1, isbn);
            cstmt.setInt(2, copyNo);
            
            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    // "Devolución realizada con éxito. Multa generada: $0.00"
                    mensajeExito = rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_RealizarDevolucion: " + e.getMessage());
            throw e;
        }
        return mensajeExito;
    }

    // Los métodos antiguos de archivos .dat (guardarTodos, obtenerTodos) deben ser eliminados.
}
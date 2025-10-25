// Reemplaza el contenido de DAO.LibroDAO.java
package DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import entidades.LibroVista;
import entidades.CopiaVista;

public class LibroDAO {

    /**
     * Busca copias DISPONIBLES y ACTIVAS para prestar (GUI Préstamos)
     */
	public List<CopiaVista> obtenerCopiasDisponibles() {
	    List<CopiaVista> listaCopias = new ArrayList<>();
	    // QUERY ACTUALIZADO: Chequea el estatus de la Copia (c.activo) y del ISBN (dl.activo)
	    String sql = """
	        SELECT 
	            c.isbn, c.copy_no, t.titulo
	        FROM Copia c
	        JOIN Titulo t ON c.titulo_no = t.titulo_no
	        JOIN Descripcion_Libro dl ON c.isbn = dl.isbn
	        WHERE 
	            c.en_prestamo = 'N' AND -- No prestada
	            c.activo = 'S' AND      -- Copia activa
	            dl.activo = 'S'         -- ISBN activo
	        ORDER BY t.titulo, c.copy_no;
	    """;
	    
	    try (Connection con = ConexionBD.getConexion();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            CopiaVista c = new CopiaVista(
	                rs.getString("isbn"),
	                rs.getInt("copy_no"),
	                rs.getString("titulo")
	            );
	            listaCopias.add(c);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error al leer las copias disponibles de SQL.");
	    }
	    return listaCopias;
	}
    
    // ============================================================
    // MÉTODOS NUEVOS (SQL Server) - Para GestionLibrosUI
    // ============================================================

    /**
     * Obtiene la lista de libros (activos E inactivos) desde la VIEW.
     * La GUI se encargará de filtrar.
     */
	public List<LibroVista> obtenerLibrosVista() {
	    List<LibroVista> listaLibros = new ArrayList<>();
	    // QUERY ACTUALIZADO: La VISTA ahora se llama 'estatus_isbn'
	    String sql = "SELECT isbn, titulo, autor, anio, disponibles, en_prestamo, estatus_isbn FROM VistaDisponibilidadLibros ORDER BY titulo";

	    try (Connection con = ConexionBD.getConexion();
	         Statement stmt = con.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            LibroVista lv = new LibroVista(
	                rs.getString("isbn"),
	                rs.getString("titulo"),
	                rs.getString("autor"),
	                rs.getInt("anio"),
	                rs.getInt("disponibles"),
	                rs.getInt("en_prestamo"),
	                rs.getString("estatus_isbn") // Columna actualizada
	            );
	            listaLibros.add(lv);
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	        System.err.println("Error al leer la VistaDisponibilidadLibros.");
	    }
	    return listaLibros;
	}

    /**
     * Llama al SP para registrar un libro completamente nuevo.
     * (El SP ya lo pone como 'S' por defecto)
     */
    public String registrarNuevoLibro(String isbn, String titulo, String autor, String idioma, String pasta, char prestable, int numCopias) throws SQLException {
        String sql = "{CALL sp_RegistrarLibroCompleto(?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, titulo);
            cstmt.setString(2, autor);
            cstmt.setString(3, isbn);
            cstmt.setString(4, idioma);
            cstmt.setString(5, pasta);
            cstmt.setString(6, String.valueOf(prestable));
            cstmt.setInt(7, numCopias);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_RegistrarLibroCompleto: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al registrar el libro.";
    }

    /**
     * Llama al SP para modificar Título y Autor.
     */
    public String modificarTitulo(String isbn, String nuevoTitulo, String nuevoAutor) throws SQLException {
        String sql = "{CALL sp_ModificarTitulo(?, ?, ?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, isbn);
            cstmt.setString(2, nuevoTitulo);
            cstmt.setString(3, nuevoAutor);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_ModificarTitulo: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al modificar el título.";
    }

    /**
     * Llama al SP para agregar o quitar copias.
     */
    public String ajustarExistencia(String isbn, int nuevaExistencia) throws SQLException {
        String sql = "{CALL sp_AjustarExistencia(?, ?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, isbn);
            cstmt.setInt(2, nuevaExistencia);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_AjustarExistencia: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al ajustar existencia.";
    }

    /**
     * Llama al SP para DESACTIVAR un libro (soft delete).
     */
    public String desactivarLibroPorISBN(String isbn) throws SQLException {
        String sql = "{CALL sp_DesactivarLibroPorISBN(?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, isbn);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_DesactivarLibroPorISBN: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al desactivar el libro.";
    }
    
    /**
     * Llama al SP para REACTIVAR un libro.
     */
    public String reactivarLibroPorISBN(String isbn) throws SQLException {
        String sql = "{CALL sp_ReactivarLibroPorISBN(?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, isbn);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_ReactivarLibroPorISBN: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al reactivar el libro.";
    }
}
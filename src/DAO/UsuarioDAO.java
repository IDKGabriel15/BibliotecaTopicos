package DAO;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import entidades.UsuarioVista;
import entidades.UsuarioTablaVista;

public class UsuarioDAO {

    // (Para el ComboBox de Préstamos - sin cambios)
    public List<UsuarioVista> obtenerUsuariosParaCombo() {
        List<UsuarioVista> listaUsuarios = new ArrayList<>();
        String sql = "SELECT usuario_no, (nombre + ' ' + apellido) AS nombre_completo FROM Usuario ORDER BY apellido, nombre;";
        
        try (Connection con = ConexionBD.getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UsuarioVista u = new UsuarioVista(
                    rs.getString("usuario_no"),
                    rs.getString("nombre_completo")
                );
                listaUsuarios.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al leer los usuarios de SQL para Combo.");
        }
        return listaUsuarios;
    }

    /**
     * Obtiene usuarios (Adultos o Jóvenes) desde la VIEW.
     * @param tipoUsuario '01' para Adultos, '02' para Jóvenes.
     */
    public List<UsuarioTablaVista> obtenerUsuariosVista(String tipoUsuario) {
        List<UsuarioTablaVista> listaUsuarios = new ArrayList<>();
        // La VISTA ya trae todas las columnas (incl. fecha_nac y tutor_usuario_no)
        String sql = "SELECT * FROM VistaUsuarios WHERE tipo_usuario = '" + tipoUsuario + "' ORDER BY apellido, nombre";

        try (Connection con = ConexionBD.getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // <<< CORRECCIÓN AQUÍ >>>
                // Ahora llamamos al constructor de 14 campos
                UsuarioTablaVista uv = new UsuarioTablaVista(
                    rs.getString("usuario_no"),
                    rs.getString("nombre"),
                    rs.getString("apellido"),
                    rs.getString("inicial"),
                    rs.getDate("fecha_registro"),
                    rs.getString("tipo_descripcion"),
                    rs.getString("calle"),
                    rs.getString("ciudad"),
                    rs.getString("estado"),
                    rs.getString("cpostal"),
                    rs.getString("telefono"),
                    rs.getDate("fecha_exp"),
                    // Campos de Joven añadidos
                    rs.getDate("fecha_nac"),
                    rs.getString("tutor_usuario_no")
                );
                listaUsuarios.add(uv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al leer la VistaUsuarios para tipo: " + tipoUsuario);
        }
        return listaUsuarios;
    }

    // ============================================================
    // MÉTODOS PARA ADULTOS
    // ============================================================

    public String registrarAdulto(String apellido, String nombre, String inicial,
                                  String calle, String ciudad, String estado,
                                  String cpostal, String telefono, Date fechaExp) throws SQLException {
        
        String sql = "{CALL sp_RegistrarAdulto(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, apellido);
            cstmt.setString(2, nombre);
            cstmt.setString(3, inicial);
            cstmt.setString(4, calle);
            cstmt.setString(5, ciudad);
            cstmt.setString(6, estado);
            cstmt.setString(7, cpostal);
            cstmt.setString(8, telefono);
            cstmt.setDate(9, new java.sql.Date(fechaExp.getTime()));

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_RegistrarAdulto: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al registrar adulto.";
    }

    public String modificarAdulto(String usuarioNo, String apellido, String nombre, String inicial,
                                  String calle, String ciudad, String estado,
                                  String cpostal, String telefono, Date fechaExp) throws SQLException {
        
        String sql = "{CALL sp_ModificarUsuarioAdulto(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, usuarioNo);
            cstmt.setString(2, apellido);
            cstmt.setString(3, nombre);
            cstmt.setString(4, inicial);
            cstmt.setString(5, calle);
            cstmt.setString(6, ciudad);
            cstmt.setString(7, estado);
            cstmt.setString(8, cpostal);
            cstmt.setString(9, telefono);
            cstmt.setDate(10, new java.sql.Date(fechaExp.getTime()));

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_ModificarUsuarioAdulto: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al modificar adulto.";
    }

    // ============================================================
    // MÉTODOS PARA JÓVENES (NUEVOS)
    // ============================================================

    /**
     * Obtiene solo Adultos para usarlos como Tutores en el JComboBox.
     */
    public List<UsuarioVista> obtenerTutoresPosibles() {
        List<UsuarioVista> listaTutores = new ArrayList<>();
        // Reutilizamos UsuarioVista para el combo
        String sql = "SELECT usuario_no, (nombre + ' ' + apellido) AS nombre_completo FROM Usuario WHERE tipo_usuario = '01' ORDER BY apellido, nombre;";
        
        try (Connection con = ConexionBD.getConexion();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                UsuarioVista u = new UsuarioVista(
                    rs.getString("usuario_no"),
                    rs.getString("nombre_completo")
                );
                listaTutores.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error al leer los tutores (Adultos).");
        }
        return listaTutores;
    }

    /**
     * Llama al SP para registrar un nuevo Joven.
     */
    public String registrarJoven(String apellido, String nombre, String inicial,
                                 Date fechaNac, String tutorUsuarioNo) throws SQLException {
        
        String sql = "{CALL sp_RegistrarJoven(?, ?, ?, ?, ?)}"; // 5 parámetros
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, apellido);
            cstmt.setString(2, nombre);
            cstmt.setDate(3, new java.sql.Date(fechaNac.getTime()));
            cstmt.setString(4, tutorUsuarioNo);
            cstmt.setString(5, inicial);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje"); // "Joven registrado con éxito. ID: ..."
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_RegistrarJoven: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al registrar joven.";
    }

    /**
     * Llama al SP para modificar un Joven existente.
     */
    public String modificarJoven(String usuarioNo, String apellido, String nombre, String inicial,
                                 Date fechaNac, String tutorUsuarioNo) throws SQLException {
        
        String sql = "{CALL sp_ModificarUsuarioJoven(?, ?, ?, ?, ?, ?)}"; // 6 parámetros
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, usuarioNo);
            cstmt.setString(2, apellido);
            cstmt.setString(3, nombre);
            cstmt.setString(4, inicial);
            cstmt.setDate(5, new java.sql.Date(fechaNac.getTime()));
            cstmt.setString(6, tutorUsuarioNo);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_ModificarUsuarioJoven: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al modificar joven.";
    }

    // ============================================================
    // MÉTODO COMÚN (ELIMINAR)
    // ============================================================

    public String eliminarUsuario(String usuarioNo) throws SQLException {
        String sql = "{CALL sp_EliminarUsuario(?)}";
        
        try (Connection con = ConexionBD.getConexion();
             CallableStatement cstmt = con.prepareCall(sql)) {
            
            cstmt.setString(1, usuarioNo);

            try (ResultSet rs = cstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("Mensaje");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en SP sp_EliminarUsuario: " + e.getMessage());
            throw e;
        }
        return "Error desconocido al eliminar usuario.";
    }
}
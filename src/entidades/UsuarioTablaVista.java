// Archivo: entidades/UsuarioTablaVista.java
package entidades;

import java.io.Serializable;
import java.util.Date;

// Esta clase representa la VIEW 'VistaUsuarios' para la JTable
public class UsuarioTablaVista implements Serializable {
    private static final long serialVersionUID = 1L;

    private String usuarioNo;
    private String nombre;
    private String apellido;
    private String inicial;
    private Date fechaRegistro;
    private String tipoDescripcion; // "Adulto" o "Joven"
    
    // Campos de Adulto (pueden ser null)
    private String calle;
    private String ciudad;
    private String estado;
    private String cpostal;
    private String telefono;
    private Date fechaExp;
    
    // <<< CAMPOS AÑADIDOS PARA JOVEN >>>
    private Date fechaNac;
    private String tutorUsuarioNo;


    // Constructor completo (ahora con los 14 campos)
    public UsuarioTablaVista(String usuarioNo, String nombre, String apellido, String inicial, 
                             Date fechaRegistro, String tipoDescripcion, String calle, 
                             String ciudad, String estado, String cpostal, String telefono, Date fechaExp,
                             Date fechaNac, String tutorUsuarioNo) { // <-- Campos añadidos
        this.usuarioNo = usuarioNo;
        this.nombre = nombre;
        this.apellido = apellido;
        this.inicial = inicial;
        this.fechaRegistro = fechaRegistro;
        this.tipoDescripcion = tipoDescripcion;
        this.calle = calle;
        this.ciudad = ciudad;
        this.estado = estado;
        this.cpostal = cpostal;
        this.telefono = telefono;
        this.fechaExp = fechaExp;
        this.fechaNac = fechaNac; // <-- Asignación
        this.tutorUsuarioNo = tutorUsuarioNo; // <-- Asignación
    }

    // --- Getters ---
    public String getUsuarioNo() { return usuarioNo; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getInicial() { return inicial; }
    public Date getFechaRegistro() { return fechaRegistro; }
    public String getTipoDescripcion() { return tipoDescripcion; }
    // Getters Adulto
    public String getCalle() { return calle; }
    public String getCiudad() { return ciudad; }
    public String getEstado() { return estado; }
    public String getCpostal() { return cpostal; }
    public String getTelefono() { return telefono; }
    public Date getFechaExp() { return fechaExp; }
    
    // <<< GETTERS AÑADIDOS PARA JOVEN >>>
    public Date getFechaNac() { return fechaNac; }
    public String getTutorUsuarioNo() { return tutorUsuarioNo; }
}
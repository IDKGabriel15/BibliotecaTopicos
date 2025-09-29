package entidades;

import java.io.Serializable;

public class Usuario implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// ATRIBUTOS DEL USUARIO
	private int idUsuario;
    private String nombre;
    private String apellido;
    private String telefono;

    // CONSTRUCTOR DONDE SE INICIALIZAN TODOS LOS CAMPOS
    public Usuario(int idUsuario, String nombre, String apellido, String telefono) {
        this.idUsuario = idUsuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
    }

    // GETTERS Y SETTERS PARA ACCEDER Y MODIFICAR DATOS DEL USUARIO
    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getTelefono() { return telefono; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setApellido(String apellido) { this.apellido = apellido; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
}
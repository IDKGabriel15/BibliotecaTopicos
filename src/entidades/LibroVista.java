// Reemplaza el contenido de entidades.LibroVista.java
package entidades;

import java.io.Serializable;

public class LibroVista implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String isbn;
    private String titulo;
    private String autor;
    private int anioRegistro;
    private int disponibles;
    private int enPrestamo;
    private String estatus; // 'S' o 'N'

    public LibroVista(String isbn, String titulo, String autor, int anioRegistro, int disponibles, int enPrestamo, String estatus) {
        this.isbn = isbn;
        this.titulo = titulo;
        this.autor = autor;
        this.anioRegistro = anioRegistro;
        this.disponibles = disponibles;
        this.enPrestamo = enPrestamo;
        this.estatus = estatus;
    }

    // --- Getters ---
    public String getIsbn() { return isbn; }
    public String getTitulo() { return titulo; }
    public String getAutor() { return autor; }
    public int getAnioRegistro() { return anioRegistro; }
    public int getDisponibles() { return disponibles; }
    public int getEnPrestamo() { return enPrestamo; }
    public String getEstatus() { return estatus; }
    
    // Calculado
    public int getTotalExistencia() {
        return disponibles + enPrestamo;
    }
    
    public String getEstatusDescripcion() {
        return "S".equals(estatus) ? "Activo" : "Inactivo";
    }

    // --- Setters (para modificar en la GUI) ---
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setAutor(String autor) { this.autor = autor; }
}
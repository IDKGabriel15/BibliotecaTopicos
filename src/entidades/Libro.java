package entidades;


import java.io.Serializable;


public class Libro implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// ATRIBUTOS DEL LIBRO
	private int id;
    private String titulo;
    private String autor;
    private int anioPublicacion;
    private int existencia;
    
    // CONSTRUCTOR DONDE SE INICIALIZAN TODOS LOS CAMPOS
    public Libro(int id, String titulo, String autor, int anioPublicacion, int existencia) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.anioPublicacion = anioPublicacion;
        this.existencia = existencia; // NÚMERO DE COPIAS DISPONIBLES

    }

    // GETTERS Y SETTERS PARA ACCEDER Y MODIFICAR ATRIBUTOS
    public int getid() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getAutor() {
        return autor;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }
    
    public int getExistencia() {
        return existencia;
    }

    public void setid(int id) {
        this.id = id;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public void setExistencia(int existencia) {
        this.existencia = existencia;
    }
 
    // REPRESENTACIÓN EN TEXTO DEL OBJETO LIBRO
    @Override
    public String toString() {
        return "Título: " + titulo + ", Autor: " + autor + ", id: " + id;
    }
}
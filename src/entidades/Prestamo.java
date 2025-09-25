package entidades;

import java.io.Serializable;

public class Prestamo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int idPrestamo;
    private int idLibro;
    private int idUsuario;
    private String fechaPrestamo;
    private String fechaDevolucion;

    public Prestamo(int idPrestamo, int idLibro, int idUsuario, String fechaPrestamo, String fechaDevolucion) {
        this.idPrestamo = idPrestamo; // Asigna un ID único y lo incrementa
        this.idLibro = idLibro;
        this.idUsuario = idUsuario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaDevolucion = fechaDevolucion;
    }

    // Métodos Getter
    public int getIdPrestamo() {
        return idPrestamo;
    }

    public int getIdLibro() {
        return idLibro;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getFechaPrestamo() {
        return fechaPrestamo;
    }

    public String getFechaDevolucion() {
        return fechaDevolucion;
    }
    
    // Método Setter (solo para la fecha de devolución, ya que las otras no deben cambiar)
    public void setFechaDevolucion(String fechaDevolucion) {
        this.fechaDevolucion = fechaDevolucion;
    }

    @Override
    public String toString() {
        return "Préstamo ID: " + idPrestamo + ", Libro ID: " + idLibro + ", Usuario ID: " + idUsuario;
    }
}
package entidades;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Prestamo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int idPrestamo;
	private int idLibro;
	private int idUsuario;
	private String fechaPrestamo;
	private Date fechaEntrega;
	private String fechaDevolucion;

	public Prestamo(int idPrestamo, int idLibro, int idUsuario, String fechaPrestamo, Date fechaEntrega, String fechaDevolucion) {
	    this.idPrestamo = idPrestamo;
	    this.idLibro = idLibro;
	    this.idUsuario = idUsuario;
	    this.fechaPrestamo = fechaPrestamo;
	    this.fechaEntrega = fechaEntrega;
	    this.fechaDevolucion = fechaDevolucion;
	}
	
	// Getters
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
	
	public String getFechaEntregaFormateada() {
	    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
	    return formato.format(fechaEntrega);
	}
	
	public String getFechaDevolucion() {
	    return fechaDevolucion;
	}
	
	// Setters (solo para devolver libro)
	public void setFechaDevolucion(String fechaDevolucion) {
	    this.fechaDevolucion = fechaDevolucion;
	}
	
	@Override
	public String toString() {
	    return "Préstamo{" +
	            "idPrestamo=" + idPrestamo +
	            ", idLibro=" + idLibro +
	            ", idUsuario=" + idUsuario +
	            ", fechaPrestamo='" + fechaPrestamo + '\'' +
	            ", fechaEntrega='" + fechaEntrega + '\'' +
	            ", fechaDevolucion='" + fechaDevolucion + '\'' +
	            '}';
	}

}

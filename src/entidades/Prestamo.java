package entidades;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;


public class Prestamo implements Serializable {
	private static final long serialVersionUID = 1L;
	
	// ATRIBUTOS DEL PRÉSTAMO
	private int idPrestamo;
	private int idLibro;
	private int idUsuario;
	private String fechaPrestamo; // FECHA EN QUE SE HIZO EL PRÉSTAMO
	private Date fechaEntrega; // FECHA COMPROMISO DE ENTREGAR EL LIBRO
	private String fechaDevolucion;// FECHA REAL DE DEVOLUCIÓN

	// CONSTRUCTOR DONDE SE INICIALIZAN TODOS LOS CAMPOS
	public Prestamo(int idPrestamo, int idLibro, int idUsuario, String fechaPrestamo, Date fechaEntrega, String fechaDevolucion) {
	    this.idPrestamo = idPrestamo;
	    this.idLibro = idLibro;
	    this.idUsuario = idUsuario;
	    this.fechaPrestamo = fechaPrestamo;
	    this.fechaEntrega = fechaEntrega;
	    this.fechaDevolucion = fechaDevolucion;
	}
	
	 // GETTERS
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
	
	// FORMATEA LA FECHA DE ENTREGA EN TEXTO 
	public String getFechaEntregaFormateada() {
	    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
	    return formato.format(fechaEntrega);
	}
	
	public String getFechaDevolucion() {
	    return fechaDevolucion;
	}
	
	// SETTERS (SOLO PARA DEVOLVER LIBRO)
	public void setFechaDevolucion(String fechaDevolucion) {
	    this.fechaDevolucion = fechaDevolucion;
	}
	
	// GENERA UNA CADENA CON LOS ATRIBUTOS DEL PRÉSTAMO
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

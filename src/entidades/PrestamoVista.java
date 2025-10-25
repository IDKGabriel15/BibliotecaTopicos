package entidades;

import java.io.Serializable;
import java.util.Date;

public class PrestamoVista implements Serializable {
    private static final long serialVersionUID = 1L;

    private String prestamoNo;
    private String isbn;
    private int copyNo;
    private String usuarioNo;
    private String tituloLibro;
    private String nombreUsuario;
    private Date fechaPrestamo;
    private Date fechaRegistro;

    public PrestamoVista(String prestamoNo, String isbn, int copyNo, String usuarioNo, 
                         String tituloLibro, String nombreUsuario, Date fechaPrestamo, Date fechaRegistro) {
        this.prestamoNo = prestamoNo;
        this.isbn = isbn;
        this.copyNo = copyNo;
        this.usuarioNo = usuarioNo;
        this.tituloLibro = tituloLibro;
        this.nombreUsuario = nombreUsuario;
        this.fechaPrestamo = fechaPrestamo;
        this.fechaRegistro = fechaRegistro;
    }

    // --- Getters ---
    public String getPrestamoNo() { return prestamoNo; }
    public String getIsbn() { return isbn; }
    public int getCopyNo() { return copyNo; }
    public String getUsuarioNo() { return usuarioNo; }
    public String getTituloLibro() { return tituloLibro; }
    public String getNombreUsuario() { return nombreUsuario; }
    public Date getFechaPrestamo() { return fechaPrestamo; }
    public Date getFechaRegistro() { return fechaRegistro; }
}
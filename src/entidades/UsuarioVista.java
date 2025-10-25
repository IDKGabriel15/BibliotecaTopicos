package entidades;

import java.io.Serializable;

public class UsuarioVista implements Serializable {
    private static final long serialVersionUID = 1L;

    private String usuarioNo; 
    private String nombreCompleto;

    public UsuarioVista(String usuarioNo, String nombreCompleto) {
        this.usuarioNo = usuarioNo;
        this.nombreCompleto = nombreCompleto;
    }

    public String getUsuarioNo() { return usuarioNo; }
    public String getNombreCompleto() { return nombreCompleto; }

    @Override
    public String toString() {
        return String.format("%s [%s]", nombreCompleto, usuarioNo);
    }
}
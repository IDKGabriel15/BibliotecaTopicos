package entidades;

import java.io.Serializable;

public class CopiaVista implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String isbn;
    private int copyNo;
    private String titulo;

    public CopiaVista(String isbn, int copyNo, String titulo) {
        this.isbn = isbn;
        this.copyNo = copyNo;
        this.titulo = titulo;
    }

    public String getIsbn() { return isbn; }
    public int getCopyNo() { return copyNo; }
    public String getTitulo() { return titulo; }

    @Override
    public String toString() {
        return String.format("(%d) %s [ISBN: %s]", copyNo, titulo, isbn);
    }
}
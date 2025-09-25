package DAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import entidades.Libro;

public class LibroDAO {

    private static final String NOMBRE_ARCHIVO = "assets/libros.dat";

    public void guardarTodos(List<Libro> libros) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(libros);
            System.out.println("Libros guardados en " + NOMBRE_ARCHIVO);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al guardar los libros.");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Libro> obtenerTodos() {
        List<Libro> listaLibros = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);

        if (archivo.exists() && archivo.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                listaLibros = (List<Libro>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error al leer los libros.");
            }
        }
        return listaLibros;
    }

    
}
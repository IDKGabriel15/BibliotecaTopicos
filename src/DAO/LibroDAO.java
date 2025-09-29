package DAO;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import entidades.Libro;


public class LibroDAO {

    private static final String NOMBRE_ARCHIVO = "assets/libros.dat";

    // MÉTODO PARA GUARDAR LA LISTA COMPLETA DE LIBROS EN EL ARCHIVO
    public void guardarTodos(List<Libro> libros) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(libros);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al guardar los libros.");
        }
    }

    // MÉTODO PARA LEER TODOS LOS LIBROS DESDE EL ARCHIVO
    @SuppressWarnings("unchecked")
    public List<Libro> obtenerTodos() {
        List<Libro> listaLibros = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);

        // VALIDAR QUE EL ARCHIVO EXISTA Y NO ESTÉ VACÍO
        if (archivo.exists() && archivo.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                listaLibros = (List<Libro>) ois.readObject(); // DESERIALIZA EL ARCHIVO
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error al leer los libros.");
            }
        }
        return listaLibros;
    }

    
}
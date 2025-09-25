package DAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import entidades.Prestamo;

public class PrestamoDAO {
    private static final String NOMBRE_ARCHIVO = "assets/prestamos.dat";

    public void guardarTodos(List<Prestamo> prestamos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(prestamos);
            System.out.println("Préstamos guardados en " + NOMBRE_ARCHIVO);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al guardar los préstamos.");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Prestamo> obtenerTodos() {
        List<Prestamo> listaPrestamos = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);
        
        if (archivo.exists() && archivo.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                listaPrestamos = (List<Prestamo>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error al leer los préstamos.");
            }
        }
        return listaPrestamos;
    }
        
}
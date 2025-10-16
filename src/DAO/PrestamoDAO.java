package DAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import entidades.Prestamo;

public class PrestamoDAO {
    private static final String NOMBRE_ARCHIVO = "assets/prestamos.dat";

    // MÉTODO PARA GUARDAR TODOS LOS PRÉSTAMOS EN EL ARCHIVO
    public void guardarTodos(List<Prestamo> prestamos) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(prestamos);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al guardar los préstamos.");
        }
    }

    // MÉTODO PARA LEER TODOS LOS PRÉSTAMOS DESDE EL ARCHIVO
    @SuppressWarnings("unchecked")
    public List<Prestamo> obtenerTodos() {
        List<Prestamo> listaPrestamos = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);
    
        // VALIDAR QUE EL ARCHIVO EXISTA Y TENGA DATOS
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
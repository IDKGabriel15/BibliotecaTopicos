package DAO;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import entidades.Usuario;

public class UsuarioDAO {

    private static final String NOMBRE_ARCHIVO = "assets/usuarios.dat";

    // MÉTODO PARA GUARDAR TODOS LOS USUARIOS EN EL ARCHIVO
    public void guardarTodos(List<Usuario> usuarios) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOMBRE_ARCHIVO))) {
            oos.writeObject(usuarios);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al guardar los usuarios.");
        }
    }

    // MÉTODO PARA LEER TODOS LOS USUARIOS DESDE EL ARCHIVO
    @SuppressWarnings("unchecked")
    public List<Usuario> obtenerTodos() {
        List<Usuario> listaUsuarios = new ArrayList<>();
        File archivo = new File(NOMBRE_ARCHIVO);
        
        // VALIDAR QUE EL ARCHIVO EXISTA Y TENGA DATOS 
        if (archivo.exists() && archivo.length() > 0) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                listaUsuarios = (List<Usuario>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.err.println("Error al leer los usuarios.");
            }
        }
        return listaUsuarios;
    }
}
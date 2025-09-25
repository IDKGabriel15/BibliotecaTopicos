package gUILayer;

import java.awt.*;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import DAO.*;
import entidades.*;
//import java.util.ArrayList;


public class GestionPrestamosUI extends JPanel implements Buscable {
	private static final long serialVersionUID = 1L;
	
	private JComboBox<String> cmbLibros;
    private JComboBox<String> cmbUsuarios;
    private JButton btnPrestar, btnDevolver, btnLimpiar;
    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;

    private PrestamoDAO prestamoDAO;
    private LibroDAO libroDAO;
    private UsuarioDAO usuarioDAO;

    private List<Prestamo> listaPrestamos;
    private List<Libro> listaLibros;
    private List<Usuario> listaUsuarios;

    public GestionPrestamosUI() {
        prestamoDAO = new PrestamoDAO();
        libroDAO = new LibroDAO();
        usuarioDAO = new UsuarioDAO();

        listaPrestamos = prestamoDAO.obtenerTodos();
        listaLibros = libroDAO.obtenerTodos();
        listaUsuarios = usuarioDAO.obtenerTodos();

        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Realizar Préstamo"));
        panelFormulario.add(new JLabel("Seleccionar Libro:"));
        cmbLibros = new JComboBox<>();
        panelFormulario.add(cmbLibros);
        panelFormulario.add(new JLabel("Seleccionar Usuario:"));
        cmbUsuarios = new JComboBox<>();
        panelFormulario.add(cmbUsuarios);
        

        cargarComboBoxes();

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPrestar = new JButton("Prestar Libro");
        btnDevolver = new JButton("Devolver Libro");
        btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnPrestar);
        panelBotones.add(btnDevolver);
        panelBotones.add(btnLimpiar);

        String[] columnas = { "ID Préstamo", "Libro", "Usuario", "Fecha Préstamo", "Fecha Devolución" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaPrestamos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);

        this.actualizarDatos();
        cargarDatosTabla();

        // --- Eventos ---
        btnPrestar.addActionListener(e -> prestarLibro());
        btnDevolver.addActionListener(e -> devolverLibro());
        btnLimpiar.addActionListener(e-> limpiarCampos());

        // --- Contenedor Principal ---
        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarComboBoxes() {
        cmbLibros.removeAllItems();
        cmbUsuarios.removeAllItems();

        for (Libro libro : listaLibros) {
            if (libro.isDisponible()) {
                cmbLibros.addItem(libro.getid() + " - " + libro.getTitulo());
            }
        }
        for (Usuario usuario : listaUsuarios) {
            cmbUsuarios.addItem(usuario.getIdUsuario() + " - " + usuario.getNombre() + " " + usuario.getApellido());
        }
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        for (Prestamo prestamo : listaPrestamos) {
            Libro libro = buscarLibroPorId(prestamo.getIdLibro());
            Usuario usuario = buscarUsuarioPorId(prestamo.getIdUsuario());
            Object[] fila = { prestamo.getIdPrestamo(), libro.getTitulo(), usuario.getNombre(), prestamo.getFechaPrestamo(), prestamo.getFechaDevolucion() };
            modeloTabla.addRow(fila);
        }
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        for (Prestamo prestamo : listaPrestamos) {
            Libro libro = buscarLibroPorId(prestamo.getIdLibro());
            Usuario usuario = buscarUsuarioPorId(prestamo.getIdUsuario());
            Object[] fila = { prestamo.getIdPrestamo(), libro.getTitulo(), usuario.getNombre(), prestamo.getFechaPrestamo(), prestamo.getFechaDevolucion()};
            for(Object elemento : fila){
                if(elemento != null && elemento.toString().toLowerCase().contains(busqueda.toLowerCase())){
                    modeloTabla.addRow(fila);
                    break;
                }
            }
        }
    }
    
    // --- Lógica de Negocio ---
    
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    public void actualizarDatos() {
        // Recarga los datos de los 3 DAOs
        this.listaLibros = this.libroDAO.obtenerTodos();
        this.listaUsuarios = this.usuarioDAO.obtenerTodos();
        this.listaPrestamos = this.prestamoDAO.obtenerTodos();

        // Actualiza la tabla y los ComboBoxes
        this.cargarDatosTabla();
        this.cargarComboBoxes();
    }

    private void prestarLibro() {
        if (cmbLibros.getSelectedIndex() == -1 || cmbUsuarios.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro y un usuario.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idLibroSeleccionado = obtenerIdDesdeComboBox(cmbLibros);
        int idUsuarioSeleccionado = obtenerIdDesdeComboBox(cmbUsuarios);

        // Simular fecha actual
        String fechaPrestamo = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());

        // Actualizar el estado del libro a no disponible
        Libro libro = buscarLibroPorId(idLibroSeleccionado);
        if (libro != null) {
            libro.setDisponible(false);
            new LibroDAO().guardarTodos(listaLibros);
        }

        Prestamo nuevoPrestamo = new Prestamo(generarNuevoId(), idLibroSeleccionado, idUsuarioSeleccionado, fechaPrestamo, "Pendiente");
        listaPrestamos.add(nuevoPrestamo);
        prestamoDAO.guardarTodos(listaPrestamos);

        cargarDatosTabla();
        cargarComboBoxes(); // Actualiza el JComboBox de libros para reflejar el cambio de disponibilidad
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Préstamo realizado exitosamente.", "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void devolverLibro() {
        int filaSeleccionada = tablaPrestamos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para devolver.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idPrestamoADevolver = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Prestamo prestamo = buscarPrestamoPorId(idPrestamoADevolver);
        if (prestamo.getFechaDevolucion().equals("Pendiente")) {
            int idLibroADevolver = prestamo.getIdLibro();
            Libro libro = buscarLibroPorId(idLibroADevolver);

            // Actualizar el estado del libro a disponible
            if (libro != null) {
                libro.setDisponible(true);
                new LibroDAO().guardarTodos(listaLibros);
            }

            // Actualizar la fecha de devolución del préstamo
            String fechaDevolucion = new java.text.SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
            prestamo.setFechaDevolucion(fechaDevolucion);

            prestamoDAO.guardarTodos(listaPrestamos);
            cargarDatosTabla();
            cargarComboBoxes(); // Actualiza el JComboBox de libros
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro devuelto exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este libro ya ha sido devuelto.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void limpiarCampos() {
        cmbLibros.setSelectedIndex(-1);
        cmbUsuarios.setSelectedIndex(-1);
        tablaPrestamos.clearSelection();
    }

    // --- Métodos de Utilidad ---

    private int obtenerIdDesdeComboBox(JComboBox<String> cmb) {
        String item = (String) cmb.getSelectedItem();
        if (item != null) {
            String[] partes = item.split(" - ");
            return Integer.parseInt(partes[0]);
        }
        return -1;
    }

    private Libro buscarLibroPorId(int id) {
        for (Libro libro : listaLibros) {
            if (libro.getid() == id) {
                return libro;
            }
        }
        return null;
    }
     
    private Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getIdUsuario() == id) {
                return usuario;
            }
        }
        return null;
    }

    private Prestamo buscarPrestamoPorId(int id) {
        for (Prestamo prestamo : listaPrestamos) {
            if (prestamo.getIdPrestamo() == id) {
                return prestamo;
            }
        }
        return null;
    }

    private int generarNuevoId() {     
        if(listaPrestamos.isEmpty()) return 1;
        return listaPrestamos.get(listaPrestamos.size()-1).getIdPrestamo() + 1;
    }
}
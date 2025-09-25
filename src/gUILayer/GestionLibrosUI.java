package gUILayer;

import java.awt.*;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import entidades.Libro;
import DAO.LibroDAO;

public class GestionLibrosUI extends JPanel implements Buscable {
	private static final long serialVersionUID = 1L;
	
    private JTextField txtId, txtTitulo, txtAutor, txtAnio;
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private LibroDAO libroDAO;
    private List<Libro> listaLibros;

    public GestionLibrosUI() {
        libroDAO = new LibroDAO();

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Libro"));

        panelFormulario.add(new JLabel("ID (Generado):"));
        txtId = new JTextField();
        txtId.setEditable(false); 
        panelFormulario.add(txtId);
        panelFormulario.add(new JLabel("Título:"));
        txtTitulo = new JTextField();
        panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel("Autor:"));
        txtAutor = new JTextField();
        panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel("Año de Publicación:"));
        txtAnio = new JTextField();
        panelFormulario.add(txtAnio);
      

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar Campos");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        String[] columnas = { "ID", "Título", "Autor", "Año", "Disponible" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaLibros = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaLibros);

        this.actualizarDatos();
        cargarDatosTabla();

        btnGuardar.addActionListener(e-> guardarLibro());
        btnModificar.addActionListener(e-> modificarLibro());
        btnEliminar.addActionListener(e-> eliminarLibro());
        btnLimpiar.addActionListener(e-> limpiarCampos());

        tablaLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int filaSeleccionada = tablaLibros.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    int idSeleccionado = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    Libro libroSeleccionado = buscarLibroPorId(idSeleccionado);

                    if (libroSeleccionado != null) {
                        txtId.setText(String.valueOf(libroSeleccionado.getid()));
                        txtTitulo.setText(libroSeleccionado.getTitulo());
                        txtAutor.setText(libroSeleccionado.getAutor());
                        txtAnio.setText(String.valueOf(libroSeleccionado.getAnioPublicacion()));
                    }
                }
            }
        });

        // --- Contenedor Principal ---
        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);

        for (Libro libro : listaLibros) {	
            Object[] fila = {libro.getid(), libro.getTitulo(), libro.getAutor(), libro.getAnioPublicacion(), libro.isDisponible() ? "Sí" : "No"};
            modeloTabla.addRow(fila);
        }
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        for (Libro libro : listaLibros) {
            Object[] fila = { libro.getid(), libro.getTitulo(), libro.getAutor(), libro.getAnioPublicacion(), libro.isDisponible() ? "Sí" : "No" };
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
        this.listaLibros = this.libroDAO.obtenerTodos();
        this.cargarDatosTabla();
    }

    private void guardarLibro() {
        if (!txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El registro ya existe", "Registro existente",
                JOptionPane.ERROR_MESSAGE);
                return;
        }
        
        if (txtTitulo.getText().isEmpty() || txtAutor.getText().isEmpty() || txtAnio.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos (excepto ID) son obligatorios.",
                    "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año de publicación debe ser un número válido.", "Error de Formato",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevoId = generarNuevoId();

        Libro nuevoLibro = new Libro(nuevoId, txtTitulo.getText(), txtAutor.getText(), anio);
        listaLibros.add(nuevoLibro);
        libroDAO.guardarTodos(listaLibros);

        cargarDatosTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro guardado exitosamente con ID: " + nuevoId, "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void modificarLibro() {
        int filaSeleccionada = tablaLibros.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para modificar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idModificar = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Libro libroExistente = buscarLibroPorId(idModificar);

        // Validación de campos
        if (txtTitulo.getText().isEmpty() || txtAutor.getText().isEmpty() || txtAnio.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(txtAnio.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El año de publicación debe ser un número válido.", "Error de Formato",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        libroExistente.setTitulo(txtTitulo.getText());
        libroExistente.setAutor(txtAutor.getText());
        libroExistente.setAnioPublicacion(anio);

        libroDAO.guardarTodos(listaLibros);
        cargarDatosTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro modificado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarLibro() {
        int filaSeleccionada = tablaLibros.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para eliminar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este libro?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            int idAEliminar = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
            Libro libroAEliminar = buscarLibroPorId(idAEliminar);

            listaLibros.remove(libroAEliminar);
            libroDAO.guardarTodos(listaLibros);

            cargarDatosTabla();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro eliminado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtAnio.setText("");
        tablaLibros.clearSelection();
    }

    private int generarNuevoId() {
        if(listaLibros.isEmpty()) return 1;     
        return listaLibros.get(listaLibros.size()-1).getid() + 1;
    }

    private Libro buscarLibroPorId(int id) {
        for (Libro libro : listaLibros) {
            if (libro.getid() == id) {
                return libro;
            }
        }
        return null;
    }
}
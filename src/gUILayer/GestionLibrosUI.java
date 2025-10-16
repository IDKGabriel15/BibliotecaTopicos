package gUILayer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import entidades.Libro;
import entidades.Prestamo;
import DAO.LibroDAO;
import DAO.PrestamoDAO;

public class GestionLibrosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    private JTextField txtId, txtTitulo, txtAutor, txtAnio, txtExistencia;
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    private LibroDAO libroDAO;
    private List<Libro> listaLibros;

    public GestionLibrosUI() {
        libroDAO = new LibroDAO();
        listaLibros = libroDAO.obtenerTodos();
        //COLORES BOTONES
        UIManager.put("Button.background", new Color(252, 197, 184));
        UIManager.put("Button.foreground", Color.BLACK);
        UIManager.put("Button.select", new Color(235, 170, 141));

        //INICIALIZAR CAMPOS Y TABLA
        initFormulario();
        initTabla();
        
        // PANELES
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();

        
        configurarEnterParaGuardarModificar();
        actualizarDatos();

        // CONTENEDOR PRINCIPAL
        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tablaLibros), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private JPanel initPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Libro"));

        panelFormulario.add(new JLabel("ID (Generado):"));
        panelFormulario.add(txtId);

        panelFormulario.add(new JLabel("Título:"));
        panelFormulario.add(txtTitulo);

        panelFormulario.add(new JLabel("Autor:"));
        panelFormulario.add(txtAutor);

        panelFormulario.add(new JLabel("Año de Publicación:"));
        panelFormulario.add(txtAnio);

        panelFormulario.add(new JLabel("Existencia:"));
        panelFormulario.add(txtExistencia);

        return panelFormulario;
    }

    private void initFormulario() {
        txtId = new JTextField(4);
        txtId.setEditable(false);
        txtTitulo = new JTextField(20);
        txtAutor = new JTextField(20);
        txtAnio = new JTextField(5);
        txtExistencia = new JTextField(5);
    }

    
    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(_ -> guardarLibro());
        btnModificar.addActionListener(_ -> modificarLibro());
        btnEliminar.addActionListener(_ -> eliminarLibro());
        btnLimpiar.addActionListener(_ -> limpiarCampos());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        return panelBotones;
    }

    private void initTabla() {
        String[] columnas = { "ID", "Título", "Autor", "Año","Existencia", "Disponibles"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaLibros = new JTable(modeloTabla);
        tablaLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaLibros.getSelectedRow();
                if (fila >= 0) cargarFormularioDesdeTabla(fila);
            }
        });
    }

    private void cargarFormularioDesdeTabla(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) return;

        txtId.setText(String.valueOf(libro.getid()));
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtExistencia.setText(String.valueOf(libro.getExistencia()));
        btnGuardar.setVisible(false);
    }

    private void cargarDatosTabla() { cargarDatosTabla(""); }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        listaLibros.stream()
            .filter(libro -> busqueda.isEmpty() || libroCoincide(libro, busqueda))
            .forEach(libro -> modeloTabla.addRow(new Object[] {
                libro.getid(), libro.getTitulo(), libro.getAutor(),
                libro.getAnioPublicacion(), libro.getExistencia(),
                calcularDisponibles(libro.getid())
            }));
    }

    private boolean libroCoincide(Libro libro, String busqueda) {
        String datos = (libro.getid() + libro.getTitulo() + libro.getAutor() +
                        libro.getAnioPublicacion() + libro.getExistencia()).toLowerCase();
        return datos.contains(busqueda.toLowerCase());
    }

    //-- INTERFAZ BUSCABLE--
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    @Override
    public void actualizarDatos() {
        listaLibros = libroDAO.obtenerTodos();
        cargarDatosTabla();
    }
    //----------------------

    private void guardarLibro() {
        if (!validarCampos()) return;

        for (Libro libro : listaLibros) {
            if (libro.getTitulo().equalsIgnoreCase(txtTitulo.getText().trim()) &&
                libro.getAutor().equalsIgnoreCase(txtAutor.getText().trim())) {

                libro.setExistencia(libro.getExistencia() + 1);
                libroDAO.guardarTodos(listaLibros);
                actualizarDatos();
                limpiarCampos();
                JOptionPane.showMessageDialog(this, "Se ha agregado otro ejemplar de este título.");
                return;
            }
        }

        if (!txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El registro ya existe. Limpia los campos para agregar un nuevo libro.", "Registro existente", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevoId = generarNuevoId();
        Libro nuevoLibro = new Libro(
    		nuevoId, txtTitulo.getText(), txtAutor.getText(),
    		Integer.parseInt(txtAnio.getText().trim()),
            Integer.parseInt(txtExistencia.getText().trim())
        );
		listaLibros.add(nuevoLibro);
        libroDAO.guardarTodos(listaLibros);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro guardado exitosamente con ID: " + nuevoId);
    }

    private void modificarLibro() {
        int fila = tablaLibros.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para modificar."); return; }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) { JOptionPane.showMessageDialog(this, "No se encontró el libro seleccionado."); return; }
        if (!validarCampos()) return;

        int anio = Integer.parseInt(txtAnio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());

        List<Prestamo> prestamos = new PrestamoDAO().obtenerTodos();
        long prestados = prestamos.stream()
            .filter(p -> p.getIdLibro() == id && "Pendiente".equals(p.getFechaDevolucion()))
            .count();
        if (existencia < prestados) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser mayor o igual a los prestados actuales (" + prestados + ").");
            return;
        }

        libro.setTitulo(txtTitulo.getText());
        libro.setAutor(txtAutor.getText());
        libro.setAnioPublicacion(anio);
        libro.setExistencia(existencia);

        libroDAO.guardarTodos(listaLibros);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro modificado exitosamente.");
    }

    private void eliminarLibro() {
        int fila = tablaLibros.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para eliminar."); return; }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) return;

        if (calcularDisponibles(id) < libro.getExistencia()) {
            JOptionPane.showMessageDialog(this, "No es posible eliminar el libro porque no está disponible.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este libro?") == JOptionPane.YES_OPTION) {
            listaLibros.remove(libro);
            libroDAO.guardarTodos(listaLibros);
            actualizarDatos();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro eliminado exitosamente.");
        }
    }

    private boolean validarCampos() {
        if (txtTitulo.getText().trim().isEmpty() || txtAutor.getText().trim().isEmpty() || txtAnio.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos (excepto ID) son obligatorios."); return false;
        }
        if (txtTitulo.getText().trim().length() < 2 || txtTitulo.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El título debe tener entre 2 y 100 caracteres."); return false;
        }
        if (txtAutor.getText().trim().length() < 2 || txtAutor.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El autor debe tener entre 2 y 100 caracteres."); return false;
        }
        if (!txtAutor.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ .,'-]+")) {
            JOptionPane.showMessageDialog(this, "El nombre del autor contiene caracteres inválidos."); return false;
        }

        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());
            if (anio < 0 || anio > Year.now().getValue()) {
                JOptionPane.showMessageDialog(this, "Año de publicación inválido."); return false;
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Año de publicación debe ser un número."); return false; }

        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia <= 0) { JOptionPane.showMessageDialog(this, "La existencia debe ser mayor a 0."); return false; }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Existencia debe ser un número."); return false; }

        return true;
    }

    private void configurarEnterParaGuardarModificar() {
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (txtId.getText().trim().isEmpty()) guardarLibro();
                    else modificarLibro();
                }
            }
        };
        txtTitulo.addKeyListener(enterAdapter);
        txtAutor.addKeyListener(enterAdapter);
        txtAnio.addKeyListener(enterAdapter);
        txtExistencia.addKeyListener(enterAdapter);
    }

    private void limpiarCampos() {
        txtId.setText(""); txtTitulo.setText(""); txtAutor.setText(""); txtAnio.setText(""); txtExistencia.setText("");
        btnGuardar.setVisible(true);
        tablaLibros.clearSelection();
    }

    private int generarNuevoId() { return listaLibros.isEmpty() ? 1 : listaLibros.get(listaLibros.size()-1).getid() + 1; }

    private int calcularDisponibles(int idLibro) {
        List<Prestamo> prestamos = new PrestamoDAO().obtenerTodos();
        long prestados = prestamos.stream()
            .filter(p -> p.getIdLibro() == idLibro && "Pendiente".equals(p.getFechaDevolucion()))
            .count();
        Libro libro = buscarLibroPorId(idLibro);
        return libro == null ? 0 : Math.max(libro.getExistencia() - (int) prestados, 0);
    }

    private Libro buscarLibroPorId(int id) {
        return listaLibros.stream().filter(l -> l.getid() == id).findFirst().orElse(null);
    }
}

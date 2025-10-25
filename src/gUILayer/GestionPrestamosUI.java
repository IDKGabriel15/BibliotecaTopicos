package gUILayer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import DAO.LibroDAO;
import DAO.PrestamoDAO;
import DAO.UsuarioDAO;
import entidades.CopiaVista;
import entidades.UsuarioVista;
import entidades.PrestamoVista;
//import entidades.Libro;
//import entidades.Prestamo;
//import entidades.Usuario;

import java.sql.SQLException;
import java.text.DateFormat;

public class GestionPrestamosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    private JTextField txtFiltroLibros;
    private JTextField txtFiltroUsuarios;
    private JComboBox<CopiaVista> cmbLibros;
    private JComboBox<UsuarioVista> cmbUsuarios;
    private JButton btnPrestar, btnDevolver, btnLimpiar;
    private JSpinner spinnerFecha;
    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;

    private PrestamoDAO prestamoDAO;
    private LibroDAO libroDAO;
    private UsuarioDAO usuarioDAO;

    private List<CopiaVista> listaCopiasCompleta;
    private List<UsuarioVista> listaUsuariosCompleta;
    private List<PrestamoVista> listaPrestamos;
    /*private List<Libro> listaLibros;
    private List<Usuario> listaUsuarios;*/

    public GestionPrestamosUI() {
        prestamoDAO = new PrestamoDAO();
        libroDAO = new LibroDAO();
        usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(10, 10));

        initFormulario();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        JScrollPane panelTabla = initTabla();

        add(panelFormulario, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
        
        configurarEventosFiltro();

        cargarDatosIniciales();
        cargarDatosTabla();
    }

    /* ---------- INICIALIZACIÓN DE COMPONENTES ---------- */

 // Archivo: gUILayer/GestionPrestamosUI.java

    private void initFormulario() {
        cmbLibros = new JComboBox<>();
        cmbUsuarios = new JComboBox<>();
        
        // --- INICIALIZAR NUEVOS FILTROS ---
        txtFiltroLibros = new JTextField(20);
        txtFiltroUsuarios = new JTextField(20);

        // Configura el Spinner para la fecha de ENTREGA (Corregido)
        spinnerFecha = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy");
        spinnerFecha.setEditor(editor);

        // ... (el resto de tu método initFormulario, como el KeyListener del spinner)
        JFormattedTextField tf = editor.getTextField();
        tf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    prestarLibro();
                }
            }
        });
    }
    private JPanel initPanelFormulario() {
        // 1. Panel principal
        JPanel panelFormulario = new JPanel(new BorderLayout(10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Realizar Préstamo (Usando SQL Server)"));

        // 2. Panel para las selecciones (Libro y Usuario)
        JPanel panelSeleccion = new JPanel(new GridLayout(1, 2, 10, 10));

        // 2a. Columna de Libros
        JPanel panelLibros = new JPanel(new BorderLayout(5, 5));
        panelLibros.add(new JLabel("1. Buscar Libro:"), BorderLayout.NORTH);
        panelLibros.add(txtFiltroLibros, BorderLayout.CENTER);
        panelLibros.add(cmbLibros, BorderLayout.SOUTH);
        
        // 2b. Columna de Usuarios
        JPanel panelUsuarios = new JPanel(new BorderLayout(5, 5));
        panelUsuarios.add(new JLabel("2. Buscar Usuario:"), BorderLayout.NORTH);
        panelUsuarios.add(txtFiltroUsuarios, BorderLayout.CENTER);
        panelUsuarios.add(cmbUsuarios, BorderLayout.SOUTH);

        panelSeleccion.add(panelLibros);
        panelSeleccion.add(panelUsuarios);
        
        // 3. Panel para la Fecha (en la parte inferior)
        JPanel panelFecha = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        // (Asegúrate de que la lógica de fechas ya fue corregida como en la respuesta anterior)
        panelFecha.add(new JLabel("3. Fecha de Entrega (Regreso):"));
        panelFecha.add(spinnerFecha);

        // 4. Ensamblar el panel
        panelFormulario.add(panelSeleccion, BorderLayout.CENTER);
        panelFormulario.add(panelFecha, BorderLayout.SOUTH);

        return panelFormulario;
    }
    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnPrestar = new JButton("Prestar Libro");
        btnDevolver = new JButton("Devolver Libro");
        btnLimpiar = new JButton("Limpiar");

        btnPrestar.addActionListener(e -> prestarLibro());
        btnDevolver.addActionListener(e -> devolverLibro());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        panelBotones.add(btnPrestar);
        panelBotones.add(btnDevolver);
        panelBotones.add(btnLimpiar);

        return panelBotones;
    }

    private JScrollPane initTabla() {
    	String[] columnas = { "ID Préstamo", "ISBN", "Copia", "Libro", "Usuario", "Fecha Préstamo", "Estado" };        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaPrestamos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        return scrollPane;
    }

    /* ---------- CARGA DE DATOS ---------- */

    private void cargarDatosIniciales() {
    	/* listaLibros = libroDAO.obtenerTodos();
        listaUsuarios = usuarioDAO.obtenerTodos();
        listaPrestamos = prestamoDAO.obtenerTodos();
    	*/
        cargarComboBoxes();
        cargarDatosTabla(); 
    }

 // Archivo: gUILayer/GestionPrestamosUI.java

    // REEMPLAZA este método
    private void cargarComboBoxes() {
        // 1. Obtener datos frescos de SQL y guardarlos en las listas maestras
        listaCopiasCompleta = libroDAO.obtenerCopiasDisponibles();
        listaUsuariosCompleta = usuarioDAO.obtenerUsuariosParaCombo();
        
        // 2. Llamar a los métodos de filtrado (con filtros vacíos)
        //    para poblar los ComboBox por primera vez.
        filtrarComboBoxLibros();
        filtrarComboBoxUsuarios();
    }
    
    /**
     * Filtra el JComboBox de Libros basándose en el texto de txtFiltroLibros.
     */
    private void filtrarComboBoxLibros() {
        if (listaCopiasCompleta == null) return; // Seguridad

        // 1. Obtener el texto del filtro
        String filtro = txtFiltroLibros.getText().toLowerCase();
        
        // 2. Limpiar el ComboBox
        cmbLibros.removeAllItems();
        
        // 3. Añadir el item nulo para "seleccione"
        cmbLibros.addItem(null); 
        
        // 4. Filtrar la lista maestra y añadir los resultados al ComboBox
        listaCopiasCompleta.stream()
            .filter(copia -> copia.toString().toLowerCase().contains(filtro)) // El .toString() busca en el texto del combo
            .forEach(cmbLibros::addItem);
    }

    /**
     * Filtra el JComboBox de Usuarios basándose en el texto de txtFiltroUsuarios.
     */
    private void filtrarComboBoxUsuarios() {
        if (listaUsuariosCompleta == null) return; // Seguridad

        // 1. Obtener el texto del filtro
        String filtro = txtFiltroUsuarios.getText().toLowerCase();
        
        // 2. Limpiar el ComboBox
        cmbUsuarios.removeAllItems();
        
        // 3. Añadir el item nulo para "seleccione"
        cmbUsuarios.addItem(null);
        
        // 4. Filtrar la lista maestra y añadir los resultados al ComboBox
        listaUsuariosCompleta.stream()
            .filter(usuario -> usuario.toString().toLowerCase().contains(filtro)) // El .toString() busca en el texto del combo
            .forEach(cmbUsuarios::addItem);
    }

    private void cargarDatosTabla() { cargarDatosTabla(""); }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        listaPrestamos = prestamoDAO.obtenerPrestamosActivos(); // Carga desde BD

        // Formateador de fecha simple
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy");

        for (PrestamoVista prestamo : listaPrestamos) {
            Object[] fila = {
                prestamo.getPrestamoNo(),
                prestamo.getIsbn(),
                prestamo.getCopyNo(),
                prestamo.getTituloLibro(),
                prestamo.getNombreUsuario(),
                df.format(prestamo.getFechaPrestamo()),
                "Pendiente"
            };

            if (busqueda == null || busqueda.trim().isEmpty()) {
                modeloTabla.addRow(fila);
            } else {
                String b = busqueda.toLowerCase();
                if (prestamo.getTituloLibro().toLowerCase().contains(b) ||
                    prestamo.getNombreUsuario().toLowerCase().contains(b) ||
                    prestamo.getIsbn().contains(b) ||
                    prestamo.getPrestamoNo().contains(b)) {
                    modeloTabla.addRow(fila);
                }
            }
        }
    }

    /* ---------- ACCIONES (PRESTAR / DEVOLVER / LIMPIAR) ---------- */

    private void prestarLibro() {
        CopiaVista copiaSeleccionada = (CopiaVista) cmbLibros.getSelectedItem();
        UsuarioVista usuarioSeleccionado = (UsuarioVista) cmbUsuarios.getSelectedItem();

        if (copiaSeleccionada == null || usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro y un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Ya no es necesario calcular disponibles, el combo SÓLO muestra disponibles.

        Date fechaEntrega = (Date) spinnerFecha.getValue(); // Fecha de entrega (Spinner)
        Date hoy = new Date();

        // Nota: Tu SP sp_RealizarPrestamo usa @FechaPres (la fecha del préstamo),
        // pero tu GUI pide "Fecha de Entrega". 
        // Asumiré que el spinner es para la *Fecha del Préstamo* (si no, el SP debe modificarse)
        // Si el spinner es Fecha de *Devolución* esperada, el SP no la almacena.
        // VOY A ASUMIR QUE EL SPINNER ES LA FECHA DEL PRÉSTAMO (@FechaPres)
        // Si quieres que sea GETDATE(), ignora el spinner.

        Date fechaPrestamo = (Date) spinnerFecha.getValue();
        if (fechaPrestamo.before(hoy)) {
            JOptionPane.showMessageDialog(this, "La fecha de entrega no puede ser anterior a hoy.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            String nuevoPrestamoID = prestamoDAO.realizarPrestamo(
                copiaSeleccionada.getIsbn(),
                copiaSeleccionada.getCopyNo(),
                usuarioSeleccionado.getUsuarioNo(),
                fechaPrestamo
            );

            JOptionPane.showMessageDialog(this, "Préstamo realizado. ID: " + nuevoPrestamoID, "Éxito", JOptionPane.INFORMATION_MESSAGE);

            actualizarDatos(); // Recarga ComboBoxes y Tabla
            limpiarCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al realizar préstamo: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void devolverLibro() {
        int filaSeleccionada = tablaPrestamos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para devolver.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtenemos los identificadores (PK) de la fila
        String isbn = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        int copyNo = (int) modeloTabla.getValueAt(filaSeleccionada, 2);

        try {
            String mensaje = prestamoDAO.realizarDevolucion(isbn, copyNo);

            JOptionPane.showMessageDialog(this, mensaje, "Devolución Exitosa", JOptionPane.INFORMATION_MESSAGE);

            actualizarDatos(); // Recarga ComboBoxes y Tabla
            limpiarCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al devolver el libro: " + e.getMessage(), "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // --- MÉTODO DE EVENTOS (NUEVO) ---

    private void configurarEventosFiltro() {
        // KeyListener para el filtro de libros
        txtFiltroLibros.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Cada vez que se suelta una tecla, se filtra la lista
                filtrarComboBoxLibros();
            }
        });
        
        // KeyListener para el filtro de usuarios
        txtFiltroUsuarios.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                // Cada vez que se suelta una tecla, se filtra la lista
                filtrarComboBoxUsuarios();
            }
        });
    }


    // REEMPLAZA este método
    private void limpiarCampos() {
        // 1. Limpiar los filtros de texto
        txtFiltroLibros.setText("");
        txtFiltroUsuarios.setText("");
        
        // 2. Limpiar las selecciones
        cmbLibros.setSelectedItem(null);
        cmbUsuarios.setSelectedItem(null);
        
        // 3. Resetear el spinner y la tabla
        spinnerFecha.setValue(new Date());
        tablaPrestamos.clearSelection();
        
        // 4. Volver a filtrar (con los filtros vacíos) para restaurar los combos
        filtrarComboBoxLibros();
        filtrarComboBoxUsuarios();
    }

    /* INTERFAZ BUSCABLE */
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }
    
    @Override
    public void actualizarDatos() {
        cargarComboBoxes();
        cargarDatosTabla();
    }
    
}

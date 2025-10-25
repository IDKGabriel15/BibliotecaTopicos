// Reemplaza el contenido de gUILayer.GestionLibrosUI.java
package gUILayer;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import entidades.LibroVista;
import DAO.LibroDAO;

public class GestionLibrosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    // --- Campos de Formulario ---
    private JTextField txtISBN, txtTitulo, txtAutor, txtAnioRegistro, txtExistencia, txtDisponibles;
    private JTextField txtIdioma;
    private JComboBox<String> cmbPasta;
    private JCheckBox chkPrestable;
    private JPanel panelDatosNuevos;
    
    // <<< NUEVO >>> Checkbox para filtrar
    private JCheckBox chkMostrarInactivos;

    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    
    private LibroDAO libroDAO;
    private List<LibroVista> listaLibrosCompleta; // Lista con TODOS (activos e inactivos)
    
    private LibroVista libroSeleccionado = null;

    public GestionLibrosUI() {
        libroDAO = new LibroDAO();
        
        initFormulario();
        initTabla();
        
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        
        configurarEnterParaGuardarModificar();
        
        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tablaLibros), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        actualizarDatos();
    }

    private void initFormulario() {
        // ... (Campos principales - sin cambios)
        txtISBN = new JTextField(13);
        txtTitulo = new JTextField(20);
        txtAutor = new JTextField(20);
        txtAnioRegistro = new JTextField(5);
        txtAnioRegistro.setEditable(false);
        txtExistencia = new JTextField(5);
        txtDisponibles = new JTextField(5);
        txtDisponibles.setEditable(false);

        // ... (Campos para libros nuevos - sin cambios)
        txtIdioma = new JTextField(3);
        cmbPasta = new JComboBox<>(new String[]{"Blanda", "Dura"});
        chkPrestable = new JCheckBox("Prestable", true);
        
        // <<< NUEVO >>> Checkbox de filtro
        chkMostrarInactivos = new JCheckBox("Mostrar Inactivos");
        chkMostrarInactivos.addActionListener(e -> cargarDatosTabla()); // Recarga la tabla al hacer clic
    }

    private JPanel initPanelFormulario() {
        JPanel panelContenedor = new JPanel(new GridLayout(1, 2, 15, 0));
        panelContenedor.setBorder(BorderFactory.createTitledBorder("Datos del Libro (Usando SQL Server)"));

        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 10, 10));
        
        panelFormulario.add(new JLabel("ISBN (ID):"));
        panelFormulario.add(txtISBN);
        panelFormulario.add(new JLabel("Título:"));
        panelFormulario.add(txtTitulo);
        panelFormulario.add(new JLabel("Autor:"));
        panelFormulario.add(txtAutor);
        panelFormulario.add(new JLabel("Año Registro (DB):"));
        panelFormulario.add(txtAnioRegistro);
        panelFormulario.add(new JLabel("Existencia Total:"));
        panelFormulario.add(txtExistencia);
        panelFormulario.add(new JLabel("Disponibles (DB):"));
        panelFormulario.add(txtDisponibles);
        
        panelContenedor.add(panelFormulario);

        panelDatosNuevos = new JPanel(new GridLayout(6, 2, 10, 10));
        panelDatosNuevos.setBorder(BorderFactory.createTitledBorder("Detalles (Solo Nuevo Ingreso)"));
        
        panelDatosNuevos.add(new JLabel("Idioma (e.g. 'ESP'):"));
        panelDatosNuevos.add(txtIdioma);
        panelDatosNuevos.add(new JLabel("Pasta:"));
        panelDatosNuevos.add(cmbPasta);
        panelDatosNuevos.add(new JLabel("Préstamo:"));
        panelDatosNuevos.add(chkPrestable);
        panelDatosNuevos.add(new JLabel("")); // Relleno
        
        // <<< NUEVO >>> Añadimos el checkbox al panel derecho
        panelDatosNuevos.add(chkMostrarInactivos);
        
        // Rellenos
        panelDatosNuevos.add(new JLabel(""));
        panelDatosNuevos.add(new JLabel(""));
        panelDatosNuevos.add(new JLabel(""));
        panelDatosNuevos.add(new JLabel(""));

        panelContenedor.add(panelDatosNuevos);

        return panelContenedor;
    }

    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Nuevo");
        btnModificar = new JButton("Modificar Existente");
        // <<< CAMBIO >>> Texto del botón
        btnEliminar = new JButton("Desactivar/Reactivar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(_ -> guardarLibro());
        btnModificar.addActionListener(_ -> modificarLibro());
        // <<< CAMBIO >>> Nombre del método
        btnEliminar.addActionListener(_ -> toggleActivoLibro());
        btnLimpiar.addActionListener(_ -> limpiarCampos());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        return panelBotones;
    }

    private void initTabla() {
        // <<< CAMBIO >>> Añadida columna Estatus
        String[] columnas = { "ISBN", "Título", "Autor", "Año Reg.", "Existencia", "Disponibles", "Estatus" };
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
        String isbn = (String) modeloTabla.getValueAt(fila, 0);
        libroSeleccionado = buscarLibroPorISBN(isbn); // Busca en la lista completa
        if (libroSeleccionado == null) return;

        txtISBN.setText(libroSeleccionado.getIsbn());
        txtTitulo.setText(libroSeleccionado.getTitulo());
        txtAutor.setText(libroSeleccionado.getAutor());
        txtAnioRegistro.setText(String.valueOf(libroSeleccionado.getAnioRegistro()));
        txtExistencia.setText(String.valueOf(libroSeleccionado.getTotalExistencia()));
        txtDisponibles.setText(String.valueOf(libroSeleccionado.getDisponibles()));

        // --- <<< NUEVA LÓGICA DE BOTONES >>> ---
        txtISBN.setEditable(false);
        btnGuardar.setVisible(false);
        panelDatosNuevos.setVisible(false);
        btnModificar.setVisible(true);
        btnEliminar.setVisible(true);

        // Si está Activo ('S'), el botón dice "Desactivar"
        if ("S".equals(libroSeleccionado.getEstatus())) {
            btnEliminar.setText("Desactivar");
            btnEliminar.setToolTipText("Marcar este ISBN como Inactivo");
            // Se puede modificar un libro activo
            btnModificar.setEnabled(true);
            txtTitulo.setEditable(true);
            txtAutor.setEditable(true);
            txtExistencia.setEditable(true);
        } else {
        // Si está Inactivo ('N'), el botón dice "Reactivar"
            btnEliminar.setText("Reactivar");
            btnEliminar.setToolTipText("Volver a activar este ISBN");
            // No dejamos modificar un libro inactivo (excepto reactivarlo)
            btnModificar.setEnabled(false);
            txtTitulo.setEditable(false);
            txtAutor.setEditable(false);
            txtExistencia.setEditable(false);
        }
    }

    private void cargarDatosTabla() { cargarDatosTabla(""); }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        // Si la lista está vacía, la carga de la BD
        if (listaLibrosCompleta == null) {
            listaLibrosCompleta = libroDAO.obtenerLibrosVista();
        }

        // --- <<< NUEVA LÓGICA DE FILTRADO >>> ---
        boolean mostrarInactivos = chkMostrarInactivos.isSelected();
        
        List<LibroVista> librosFiltrados = listaLibrosCompleta.stream()
            .filter(libro -> {
                // 1. Filtro por Estatus
                boolean estatusCoincide = mostrarInactivos || "S".equals(libro.getEstatus());
                if (!estatusCoincide) return false;
                
                // 2. Filtro por Búsqueda
                return busqueda.isEmpty() || libroCoincide(libro, busqueda);
            })
            .collect(Collectors.toList());
        
        // --- Carga en la tabla ---
        librosFiltrados.forEach(libro -> modeloTabla.addRow(new Object[] {
            libro.getIsbn(),
            libro.getTitulo(),
            libro.getAutor(),
            libro.getAnioRegistro(),
            libro.getTotalExistencia(),
            libro.getDisponibles(),
            libro.getEstatusDescripcion() // <<< Muestra "Activo" o "Inactivo"
        }));
    }

    private boolean libroCoincide(LibroVista libro, String busqueda) {
        String b = busqueda.toLowerCase();
        String datos = (libro.getIsbn() + libro.getTitulo() + libro.getAutor()).toLowerCase();
        return datos.contains(b);
    }

    //-- INTERFAZ BUSCABLE--
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    @Override
    public void actualizarDatos() {
        // Forzamos la recarga desde la BD
        listaLibrosCompleta = libroDAO.obtenerLibrosVista();
        cargarDatosTabla();
    }
    //----------------------

    private void guardarLibro() {
        if (!validarCamposGuardar()) return;
        
        try {
            // ... (Lógica de guardar no cambia)
            String isbn = txtISBN.getText().trim();
            String titulo = txtTitulo.getText().trim();
            String autor = txtAutor.getText().trim();
            int numCopias = Integer.parseInt(txtExistencia.getText().trim());
            String idioma = txtIdioma.getText().trim().toUpperCase();
            String pasta = (cmbPasta.getSelectedIndex() == 0) ? "B" : "D";
            char prestable = chkPrestable.isSelected() ? 'S' : 'N';

            String mensaje = libroDAO.registrarNuevoLibro(isbn, titulo, autor, idioma, pasta, prestable, numCopias);
            
            JOptionPane.showMessageDialog(this, mensaje, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
            limpiarCampos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarLibro() {
        if (libroSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro de la tabla para modificar.");
            return;
        }
        if (!"S".equals(libroSeleccionado.getEstatus())) {
            JOptionPane.showMessageDialog(this, "No se puede modificar un libro Inactivo.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!validarCamposModificar()) return;

        try {
            String isbn = libroSeleccionado.getIsbn();
            String nuevoTitulo = txtTitulo.getText().trim();
            String nuevoAutor = txtAutor.getText().trim();
            int nuevaExistencia = Integer.parseInt(txtExistencia.getText().trim());

            boolean algoCambio = false;
            
            // 1. Modificar Título/Autor (si cambiaron)
            if (!nuevoTitulo.equals(libroSeleccionado.getTitulo()) || !nuevoAutor.equals(libroSeleccionado.getAutor())) {
                libroDAO.modificarTitulo(isbn, nuevoTitulo, nuevoAutor);
                algoCambio = true;
            }
            
            // 2. Ajustar Existencia (si cambió)
            if (nuevaExistencia != libroSeleccionado.getTotalExistencia()) {
                String msgExistencia = libroDAO.ajustarExistencia(isbn, nuevaExistencia);
                JOptionPane.showMessageDialog(this, msgExistencia, "Ajuste de Existencia", JOptionPane.INFORMATION_MESSAGE);
                algoCambio = true;
            }

            if (!algoCambio) {
                 JOptionPane.showMessageDialog(this, "No se detectaron cambios.", "Aviso", JOptionPane.WARNING_MESSAGE);
                 return;
            }

            JOptionPane.showMessageDialog(this, "Libro (ISBN: " + isbn + ") modificado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
            limpiarCampos();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser un número válido.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al modificar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // <<< MÉTODO REFACTORIZADO >>>
    // Este método reemplaza a 'eliminarLibro()'
    private void toggleActivoLibro() {
        if (libroSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla.");
            return;
        }

        String isbn = libroSeleccionado.getIsbn();
        String mensajeAccion;
        String sqlAccion;

        try {
            // Decide si Desactivar o Reactivar
            if ("S".equals(libroSeleccionado.getEstatus())) {
                // --- Lógica para DESACTIVAR ---
                mensajeAccion = "¿Desactivar este libro (ISBN: " + isbn + ")?\n" +
                                "No podrá ser prestado, pero su historial se conservará.";
                if (JOptionPane.showConfirmDialog(this, mensajeAccion, "Confirmar Desactivación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
                    
                    String mensajeExito = libroDAO.desactivarLibroPorISBN(isbn);
                    JOptionPane.showMessageDialog(this, mensajeExito, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                // --- Lógica para REACTIVAR ---
                mensajeAccion = "¿Reactivar este libro (ISBN: " + isbn + ")?";
                if (JOptionPane.showConfirmDialog(this, mensajeAccion, "Confirmar Reactivación", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    
                    String mensajeExito = libroDAO.reactivarLibroPorISBN(isbn);
                    JOptionPane.showMessageDialog(this, mensajeExito, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                }
            }
            
            // Recargamos todo
            actualizarDatos();
            limpiarCampos();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al actualizar estatus en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ... (validarCamposGuardar y validarCamposModificar no cambian) ...
    private boolean validarCamposGuardar() {
        if (txtISBN.getText().trim().isEmpty() || 
            txtTitulo.getText().trim().isEmpty() || 
            txtAutor.getText().trim().isEmpty() || 
            txtExistencia.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Los campos ISBN, Título, Autor y Existencia son obligatorios.");
            return false;
        }
        if (!txtISBN.getText().trim().matches("\\d{13}")) {
            JOptionPane.showMessageDialog(this, "El ISBN debe contener exactamente 13 dígitos numéricos.");
            return false;
        }
        if (txtIdioma.getText().trim().length() > 3) {
            JOptionPane.showMessageDialog(this, "El Idioma debe tener máximo 3 caracteres (ej. 'ESP').");
            return false;
        }
        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia <= 0) { JOptionPane.showMessageDialog(this, "La existencia debe ser mayor a 0."); return false; }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Existencia debe ser un número."); return false; }

        return true;
    }
    
    private boolean validarCamposModificar() {
        if (txtTitulo.getText().trim().isEmpty() || 
            txtAutor.getText().trim().isEmpty() || 
            txtExistencia.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Los campos Título, Autor y Existencia no pueden estar vacíos.");
            return false;
        }
        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia < 0) { JOptionPane.showMessageDialog(this, "La existencia no puede ser negativa."); return false; }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Existencia debe ser un número."); return false; }
        
        return true;
    }

    // ... (configurarEnterParaGuardarModificar no cambia) ...
    private void configurarEnterParaGuardarModificar() {
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (btnGuardar.isVisible()) guardarLibro();
                    else modificarLibro();
                }
            }
        };
        txtISBN.addKeyListener(enterAdapter);
        txtTitulo.addKeyListener(enterAdapter);
        txtAutor.addKeyListener(enterAdapter);
        txtExistencia.addKeyListener(enterAdapter);
        txtIdioma.addKeyListener(enterAdapter);
    }
    
    private void limpiarCampos() {
        txtISBN.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtAnioRegistro.setText("");
        txtExistencia.setText("");
        txtDisponibles.setText("");
        txtIdioma.setText("");
        cmbPasta.setSelectedIndex(0);
        chkPrestable.setSelected(true);
        
        // Estado inicial
        txtISBN.setEditable(true);
        txtTitulo.setEditable(true);
        txtAutor.setEditable(true);
        txtExistencia.setEditable(true);
        btnGuardar.setVisible(true);
        panelDatosNuevos.setVisible(true);
        btnModificar.setVisible(true);
        btnModificar.setEnabled(true);
        btnEliminar.setVisible(true);
        btnEliminar.setText("Desactivar/Reactivar");
        
        tablaLibros.clearSelection();
        libroSeleccionado = null;
    }

    private LibroVista buscarLibroPorISBN(String isbn) {
        if (listaLibrosCompleta == null) return null;
        return listaLibrosCompleta.stream().filter(l -> l.getIsbn().equals(isbn)).findFirst().orElse(null);
    }
}
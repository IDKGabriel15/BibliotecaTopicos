package gUILayer;

//IMPORTS DE AWT (ABSTRAC WINDOW TOOLKIT) PARA COMPONENTES GRÁFICOS Y LAYOUTS
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Date;
import java.util.List;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;

//IMPORTS DE SWING PARA COMPONENTES DE INTERFAZ GRÁFICA MÁS AVANZADOS
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.SpinnerDateModel;
import javax.swing.table.DefaultTableModel;

//IMPORTS DE PAQUETES PERSONALIZADOS (CAPA DE DATOS Y ENTIDADES)
import DAO.LibroDAO;
import DAO.PrestamoDAO;
import DAO.UsuarioDAO;
import entidades.Libro;
import entidades.Prestamo;
import entidades.Usuario;

//SE CREA LA CLASE GESTIONPRESTAMOSUI, PANEL ENCARGADO DE LA INTERFAZ
//PARA REALIZAR Y GESTIONAR PRÉSTAMOS Y DEVOLUCIONES DE LIBROS.
public class GestionPrestamosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    //SE DECLARAN LOS COMPONENTES DE LA INTERFAZ GRÁFICA (BOTONES, TABLAS, ETC.).
    private JComboBox<String> cmbLibros;
    private JComboBox<String> cmbUsuarios;
    private JButton btnPrestar, btnDevolver, btnLimpiar;
    private JSpinner spinnerFecha;
    private JTable tablaPrestamos;
    private DefaultTableModel modeloTabla;

    //OBJETOS DAO PARA ACCEDER A LOS DATOS DE LIBROS, USUARIOS Y PRÉSTAMOS.
    private PrestamoDAO prestamoDAO;
    private LibroDAO libroDAO;
    private UsuarioDAO usuarioDAO;

    //LISTAS PARA ALMACENAR LOS DATOS CARGADOS DESDE LOS ARCHIVOS EN MEMORIA.
    private List<Prestamo> listaPrestamos;
    private List<Libro> listaLibros;
    private List<Usuario> listaUsuarios;

    //CONSTRUCTOR DE LA CLASE. SE EJECUTA AL CREAR EL PANEL.
    public GestionPrestamosUI() {
    	//SE INICIALIZAN LOS OBJETOS DAO PARA LA MANIPULACIÓN DE DATOS.
        prestamoDAO = new PrestamoDAO();
        libroDAO = new LibroDAO();
        usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(10, 10));

        //SE LLAMAN LOS MÉTODOS PARA CONSTRUIR LAS DIFERENTES PARTES DE LA INTERFAZ.
        initFormulario();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        JScrollPane panelTabla = initTabla();

        //SE AÑADEN LOS PANELES CREADOS AL PANEL PRINCIPAL.
        add(panelFormulario, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        //SE CARGAN LOS DATOS INICIALES DESDE LOS ARCHIVOS.
        cargarDatosIniciales();
        cargarDatosTabla();
    }

    /* ---------- INICIALIZACIÓN DE COMPONENTES ---------- */
    
    //INICIALIZA LOS COMPONENTES SWING DEL FORMULARIO (COMBOBOXES Y SPINNER DE FECHA).
    private void initFormulario() {
        cmbLibros = new JComboBox<>();
        cmbUsuarios = new JComboBox<>();

        spinnerFecha = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFecha, "dd/MM/yyyy");
        spinnerFecha.setEditor(editor);

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

    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LAS ETIQUETAS Y CAMPOS DE ENTRADA.
    private JPanel initPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridLayout(3, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Realizar Préstamo"));

        panelFormulario.add(new JLabel("Seleccionar Libro:"));
        panelFormulario.add(cmbLibros);

        panelFormulario.add(new JLabel("Seleccionar Usuario:"));
        panelFormulario.add(cmbUsuarios);

        panelFormulario.add(new JLabel("Fecha de Entrega:"));
        panelFormulario.add(spinnerFecha);

        return panelFormulario;
    }

    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LOS BOTONES DE ACCIÓN.
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

    //CREA Y CONFIGURA LA TABLA Y SU MODELO PARA MOSTRAR LOS PRÉSTAMOS.
    private JScrollPane initTabla() {
        String[] columnas = { "ID Préstamo", "Libro", "Usuario", "Fecha Préstamo", "Fecha Entrega", "Fecha Devolución" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tablaPrestamos = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaPrestamos);
        return scrollPane;
    }

    //CARGA TODAS LAS LISTAS DE DATOS (LIBROS, USUARIOS, PRÉSTAMOS) DESDE LOS ARCHIVOS.
   private void cargarDatosIniciales() {
        listaLibros = libroDAO.obtenerTodos();
        listaUsuarios = usuarioDAO.obtenerTodos();
        listaPrestamos = prestamoDAO.obtenerTodos();

        cargarComboBoxes();
    }

   	//LLENA LOS COMBOBOXES DE LIBROS Y USUARIOS CON LOS DATOS CARGADOS.
    private void cargarComboBoxes() {
        cmbLibros.removeAllItems();
        cmbUsuarios.removeAllItems();

        for (Libro libro : listaLibros) {
            if (calcularDisponibles(libro.getid()) > 0) {
                cmbLibros.addItem(libro.getid() + " - " + libro.getTitulo());
            } else {
                cmbLibros.addItem(libro.getid() + " - " + libro.getTitulo() + " (sin disponibles)");
            }
        }
        for (Usuario usuario : listaUsuarios) {
            cmbUsuarios.addItem(usuario.getIdUsuario() + " - " + usuario.getNombre() + " " + usuario.getApellido());
        }
    }
    
    //SOBRECARGA DEL MÉTODO PARA LLAMAR A CARGARDATOSTABLA SIN PARÁMETROS.
    private void cargarDatosTabla() { cargarDatosTabla(""); }

    //CARGA LA TABLA CON LOS DATOS DE LOS PRÉSTAMOS, APLICANDO UN FILTRO DE BÚSQUEDA.
    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        if (listaPrestamos == null) listaPrestamos = prestamoDAO.obtenerTodos();

        for (Prestamo prestamo : listaPrestamos) {
            Libro libro = buscarLibroPorId(prestamo.getIdLibro());
            Usuario usuario = buscarUsuarioPorId(prestamo.getIdUsuario());

            String sLibro = (libro != null) ? libro.getTitulo() : ("ID " + prestamo.getIdLibro());
            String sUsuario = (usuario != null) ? (usuario.getNombre() + " " + usuario.getApellido()) : ("ID " + prestamo.getIdUsuario());

            Object[] fila = {
                prestamo.getIdPrestamo(),
                sLibro,
                sUsuario,
                prestamo.getFechaPrestamo(),            
                prestamo.getFechaEntregaFormateada(),   
                prestamo.getFechaDevolucion()    
            };

            if (busqueda == null || busqueda.trim().isEmpty()) {
                modeloTabla.addRow(fila);
            } else {
                String b = busqueda.toLowerCase();
                for (Object elem : fila) {
                    if (elem != null && elem.toString().toLowerCase().contains(b)) {
                        modeloTabla.addRow(fila);
                        break;
                    }
                }
            }
        }
    }

    /* ---------- ACCIONES (PRESTAR / DEVOLVER / LIMPIAR) ---------- */

    //CONTIENE LA LÓGICA PARA REGISTRAR UN NUEVO PRÉSTAMO.
    private void prestarLibro() {
    	//VALIDA QUE SE HAYA SELECCIONADO UN LIBRO Y UN USUARIO.
        if (cmbLibros.getSelectedIndex() == -1 || cmbUsuarios.getSelectedIndex() == -1) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un libro y un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idLibroSeleccionado = obtenerIdDesdeComboBox(cmbLibros);
        int idUsuarioSeleccionado = obtenerIdDesdeComboBox(cmbUsuarios);

        //VALIDA LA DISPONIBILIDAD DEL LIBRO.
        int disponibles = calcularDisponibles(idLibroSeleccionado);
        if (disponibles <= 0) {
            JOptionPane.showMessageDialog(this, "No hay ejemplares disponibles de este libro.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String fechaPrestamo = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Date fechaEntrega = (Date) spinnerFecha.getValue();
        Date hoy = new Date();

        //VALIDA QUE LA FECHA DE ENTREGA SEA POSTERIOR A LA FECHA ACTUAL.
        if (!fechaEntrega.after(hoy)) {
            JOptionPane.showMessageDialog(this, "La fecha de entrega debe ser mayor a la fecha actual.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //CREA EL NUEVO OBJETO PRÉSTAMO Y LO GUARDA.
        Prestamo nuevoPrestamo = new Prestamo(
            generarNuevoId(),
            idLibroSeleccionado,
            idUsuarioSeleccionado,
            fechaPrestamo,
            fechaEntrega,
            "Pendiente"
        );

        listaPrestamos.add(nuevoPrestamo);
        prestamoDAO.guardarTodos(listaPrestamos);

        //ACTUALIZA LA INTERFAZ GRÁFICA.
        cargarDatosTabla();
        cargarComboBoxes();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Préstamo realizado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    //CONTIENE LA LÓGICA PARA MARCAR UN PRÉSTAMO COMO DEVUELTO.
    private void devolverLibro() {
        int filaSeleccionada = tablaPrestamos.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un préstamo de la tabla para devolver.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idPrestamoADevolver = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Prestamo prestamo = buscarPrestamoPorId(idPrestamoADevolver);
        if (prestamo == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el préstamo seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        //VERIFICA SI EL PRÉSTAMO YA FUE DEVUELTO ANTERIORMENTE.
        if ("Pendiente".equals(prestamo.getFechaDevolucion())) {
            String fechaDevolucion = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
            prestamo.setFechaDevolucion(fechaDevolucion);

            prestamoDAO.guardarTodos(listaPrestamos);
            
            //ACTUALIZA LA INTERFAZ GRÁFICA.
            cargarDatosTabla();
            cargarComboBoxes();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro devuelto exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Este libro ya ha sido devuelto.", "Advertencia", JOptionPane.WARNING_MESSAGE);
        }
    }

    //RESTABLECE LOS VALORES DE LOS COMPONENTES DEL FORMULARIO A SU ESTADO INICIAL.
    private void limpiarCampos() {
        cmbLibros.setSelectedIndex(-1);
        cmbUsuarios.setSelectedIndex(-1);
        spinnerFecha.setValue(new Date());
        tablaPrestamos.clearSelection();
    }

    /* ---------- UTILIDADES / BÚSQUEDAS ---------- */

    //FUNCIÓN UTILITARIA PARA EXTRAER EL ID NUMÉRICO DE UN ITEM DEL COMBOBOX.
    private int obtenerIdDesdeComboBox(JComboBox<String> cmb) {
        String item = (String) cmb.getSelectedItem();
        if (item != null) {
            String[] partes = item.split(" - ");
            try {
                return Integer.parseInt(partes[0].trim());
            } catch (NumberFormatException ex) { /* IGNORE */ }
        }
        return -1;
    }

    //BUSCA UN OBJETO LIBRO EN LA LISTA A PARTIR DE SU ID.
    private Libro buscarLibroPorId(int id) {
        if (listaLibros == null) listaLibros = libroDAO.obtenerTodos();
        for (Libro libro : listaLibros) {
            if (libro.getid() == id) return libro;
        }
        return null;
    }

    //BUSCA UN OBJETO USUARIO EN LA LISTA A PARTIR DE SU ID.
    private Usuario buscarUsuarioPorId(int id) {
        if (listaUsuarios == null) listaUsuarios = usuarioDAO.obtenerTodos();
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getIdUsuario() == id) return usuario;
        }
        return null;
    }

    //BUSCA UN OBJETO PRÉSTAMO EN LA LISTA A PARTIR DE SU ID.
    private Prestamo buscarPrestamoPorId(int id) {
        if (listaPrestamos == null) listaPrestamos = prestamoDAO.obtenerTodos();
        for (Prestamo p : listaPrestamos) {
            if (p.getIdPrestamo() == id) return p;
        }
        return null;
    }

    //CALCULA EL NÚMERO DE EJEMPLARES DISPONIBLES PARA UN LIBRO ESPECÍFICO.
    private int calcularDisponibles(int idLibro) {
        Libro libro = buscarLibroPorId(idLibro);
        if (libro == null) return 0;

        long prestados = listaPrestamos.stream()
                .filter(p -> p.getIdLibro() == idLibro && "Pendiente".equals(p.getFechaDevolucion()))
                .count();

        return libro.getExistencia() - (int) prestados;
    }

    //GENERA UN NUEVO ID AUTOINCREMENTAL PARA UN PRÉSTAMO.
    private int generarNuevoId() {
        if (listaPrestamos == null || listaPrestamos.isEmpty()) return 1;
        return listaPrestamos.get(listaPrestamos.size()-1).getIdPrestamo() + 1;
    }

    /* ---------- IMPLEMENTACIÓN DE LA INTERFAZ BUSCABLE ---------- */
    
    //FILTRA LOS DATOS DE LA TABLA SEGÚN EL CRITERIO DE BÚSQUEDA.
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }
    
    //ACTUALIZA TODOS LOS DATOS DEL PANEL (LISTAS, COMBOS, TABLA) DESDE LOS ARCHIVOS.
    @Override
    public void actualizarDatos() {
        listaPrestamos = prestamoDAO.obtenerTodos();
        listaLibros = libroDAO.obtenerTodos();
        listaUsuarios = usuarioDAO.obtenerTodos();
        cargarComboBoxes();
        cargarDatosTabla();
    }
    
}
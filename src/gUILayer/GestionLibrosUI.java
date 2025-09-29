package gUILayer;

//IMPORTS DE AWT (ABSTRAC WINDOW TOOLKIT) PARA COMPONENTES GRÁFICOS Y LAYOUTS
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.Year;
import java.util.List;

//IMPORTS DE SWING PARA COMPONENTES DE INTERFAZ GRÁFICA MÁS AVANZADOS
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

//IMPORTS DE PAQUETES PERSONALIZADOS (CAPA DE DATOS Y ENTIDADES)
import DAO.LibroDAO;
import DAO.PrestamoDAO;
import entidades.Libro;
import entidades.Prestamo;

//SE CREA LA CLASE GESTIONLIBROSUI, PANEL ENCARGADO DE LA INTERFAZ
//PARA REGISTRAR, MODIFICAR Y ELIMINAR LIBROS DEL SISTEMA.
public class GestionLibrosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    //SE DECLARAN LOS COMPONENTES DE LA INTERFAZ GRÁFICA (CAMPOS DE TEXTO, BOTONES, ETC.).
    private JTextField txtId, txtTitulo, txtAutor, txtAnio, txtExistencia;
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaLibros;
    private DefaultTableModel modeloTabla;
    
    //OBJETO DAO PARA LA PERSISTENCIA DE DATOS Y LISTA EN MEMORIA.
    private LibroDAO libroDAO;
    private List<Libro> listaLibros;

    //CONSTRUCTOR DE LA CLASE. SE EJECUTA AL CREAR EL PANEL.
    public GestionLibrosUI() {
    	//SE INICIALIZA EL OBJETO DAO Y SE CARGA LA LISTA DE LIBROS.
        libroDAO = new LibroDAO();
        listaLibros = libroDAO.obtenerTodos();

        //SE LLAMAN LOS MÉTODOS PARA CONSTRUIR LA INTERFAZ GRÁFICA.
        initFormulario();
        initTabla();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();

        //SE CONFIGURAN LOS LISTENERS PARA LOS EVENTOS DE TECLADO.
        configurarEnterParaGuardarModificar();
        
        //SE CARGAN LOS DATOS INICIALES EN LA TABLA.
        actualizarDatos();

        //SE AÑADEN LOS PANELES CREADOS AL PANEL PRINCIPAL.
        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(new JScrollPane(tablaLibros), BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }
    
    //INICIALIZA LOS CAMPOS DE TEXTO DEL FORMULARIO.
    private void initFormulario() {
    	txtId = new JTextField(4);
    	txtId.setEditable(false); //EL CAMPO ID NO ES EDITABLE POR EL USUARIO.
    	txtTitulo = new JTextField(20);
    	txtAutor = new JTextField(20);
    	txtAnio = new JTextField(5);
    	txtExistencia = new JTextField(5);
    }

    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LAS ETIQUETAS Y CAMPOS DEL FORMULARIO.
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
    
    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LOS BOTONES DE ACCIÓN.
    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        btnGuardar.addActionListener(e -> guardarLibro());
        btnModificar.addActionListener(e -> modificarLibro());
        btnEliminar.addActionListener(e -> eliminarLibro());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        return panelBotones;
    }
    
    //CREA Y CONFIGURA LA TABLA Y SU MODELO PARA MOSTRAR LOS LIBROS.
    private void initTabla() {
        String[] columnas = { "ID", "Título", "Autor", "Año","Existencia", "Disponibles"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int column) { return false; } //LA TABLA NO ES EDITABLE.
        };
        tablaLibros = new JTable(modeloTabla);
        //EVENTO PARA CARGAR DATOS EN EL FORMULARIO AL HACER CLIC EN UNA FILA.
        tablaLibros.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int fila = tablaLibros.getSelectedRow();
                if (fila >= 0) cargarFormularioDesdeTabla(fila);
            }
        });
    }

    //CARGA LOS DATOS DE LA FILA SELECCIONADA EN LA TABLA DENTRO DEL FORMULARIO.
    private void cargarFormularioDesdeTabla(int fila) {
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) return;

        txtId.setText(String.valueOf(libro.getid()));
        txtTitulo.setText(libro.getTitulo());
        txtAutor.setText(libro.getAutor());
        txtAnio.setText(String.valueOf(libro.getAnioPublicacion()));
        txtExistencia.setText(String.valueOf(libro.getExistencia()));
        btnGuardar.setVisible(false); //SE OCULTA EL BOTÓN GUARDAR AL EDITAR.
    }

    //SOBRECARGA DEL MÉTODO PARA LLAMAR A CARGARDATOSTABLA SIN PARÁMETROS.
    private void cargarDatosTabla() { cargarDatosTabla(""); }

    //CARGA LA TABLA CON LOS DATOS DE LOS LIBROS, APLICANDO UN FILTRO DE BÚSQUEDA.
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

    //VERIFICA SI UN LIBRO COINCIDE CON EL CRITERIO DE BÚSQUEDA.
    private boolean libroCoincide(Libro libro, String busqueda) {
        String datos = (libro.getid() + libro.getTitulo() + libro.getAutor() +
                        libro.getAnioPublicacion() + libro.getExistencia()).toLowerCase();
        return datos.contains(busqueda.toLowerCase());
    }

    //-- IMPLEMENTACIÓN DE LA INTERFAZ BUSCABLE --
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
    //-------------------------------------------

    //CONTIENE LA LÓGICA PARA GUARDAR UN NUEVO LIBRO.
    private void guardarLibro() {
    	//VALIDA LOS CAMPOS ANTES DE CONTINUAR; SI NO SON VÁLIDOS, DETIENE LA EJECUCIÓN.
        if (!validarCampos()) return;
        
        //SE CREA UNA INSTANCIA DEL NUEVO LIBRO CON LOS DATOS DEL FORMULARIO.
        int nuevoId = generarNuevoId();
        Libro nuevoLibro = new Libro(
    		nuevoId, txtTitulo.getText(), txtAutor.getText(),
    		Integer.parseInt(txtAnio.getText().trim()),
            Integer.parseInt(txtExistencia.getText().trim())
        );
        
        //SE AÑADE EL LIBRO A LA LISTA, SE GUARDA EN EL ARCHIVO Y SE ACTUALIZA LA UI.
		listaLibros.add(nuevoLibro);
        libroDAO.guardarTodos(listaLibros);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro guardado exitosamente con ID: " + nuevoId);
    }

    //CONTIENE LA LÓGICA PARA MODIFICAR UN LIBRO EXISTENTE.
    private void modificarLibro() {
    	//VALIDA QUE SE HAYA SELECCIONADO UNA FILA EN LA TABLA.
        int fila = tablaLibros.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para modificar."); return; }

        //SE OBTIENE EL LIBRO Y SE VALIDAN LOS CAMPOS DEL FORMULARIO.
        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) { JOptionPane.showMessageDialog(this, "No se encontró el libro seleccionado."); return; }
        if (!validarCampos()) return;
        
        //SE CONVIERTEN LOS DATOS NUMÉRICOS DE STRING A INT.
        int anio = Integer.parseInt(txtAnio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());

        //VALIDA QUE LA NUEVA EXISTENCIA NO SEA MENOR A LA CANTIDAD DE LIBROS YA PRESTADOS.
        List<Prestamo> prestamos = new PrestamoDAO().obtenerTodos();
        long prestados = prestamos.stream()
            .filter(p -> p.getIdLibro() == id && "Pendiente".equals(p.getFechaDevolucion()))
            .count();
        if (existencia < prestados) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser mayor o igual a los prestados actuales (" + prestados + ").");
            return;
        }

        //SE ACTUALIZAN LOS DATOS DEL OBJETO LIBRO.
        libro.setTitulo(txtTitulo.getText());
        libro.setAutor(txtAutor.getText());
        libro.setAnioPublicacion(anio);
        libro.setExistencia(existencia);

        //SE GUARDAN LOS CAMBIOS Y SE ACTUALIZA LA UI.
        libroDAO.guardarTodos(listaLibros);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Libro modificado exitosamente.");
    }

    //CONTIENE LA LÓGICA PARA ELIMINAR UN LIBRO SELECCIONADO.
    private void eliminarLibro() {
        int fila = tablaLibros.getSelectedRow();
        if (fila < 0) { JOptionPane.showMessageDialog(this, "Selecciona un libro de la tabla para eliminar."); return; }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Libro libro = buscarLibroPorId(id);
        if (libro == null) return;

        //VALIDA QUE EL LIBRO NO TENGA PRÉSTAMOS PENDIENTES ANTES DE ELIMINAR.
        if (calcularDisponibles(id) < libro.getExistencia()) {
            JOptionPane.showMessageDialog(this, "No es posible eliminar el libro porque tiene préstamos pendientes.");
            return;
        }
        
        //PIDE CONFIRMACIÓN AL USUARIO ANTES DE ELIMINAR.
        if (JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este libro?") == JOptionPane.YES_OPTION) {
            listaLibros.remove(libro);
            libroDAO.guardarTodos(listaLibros);
            actualizarDatos();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro eliminado exitosamente.");
        }
    }

    //MÉTODO CENTRAL PARA VALIDAR TODOS LOS CAMPOS DEL FORMULARIO.
    private boolean validarCampos() {
    	//VALIDA QUE LOS CAMPOS OBLIGATORIOS NO ESTÉN VACÍOS.
        if (txtTitulo.getText().trim().isEmpty() || txtAutor.getText().trim().isEmpty() || txtAnio.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos (excepto ID) son obligatorios."); return false;
        }
        
        //VALIDA LA LONGITUD DEL TÍTULO.
        if (txtTitulo.getText().trim().length() < 2 || txtTitulo.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El título debe tener entre 2 y 100 caracteres."); return false;
        }
        
        //VALIDA LA LONGITUD Y CARACTERES DEL AUTOR.
        if (txtAutor.getText().trim().length() < 2 || txtAutor.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El autor debe tener entre 2 y 100 caracteres."); return false;
        }
        if (!txtAutor.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ .,'-]+")) {
            JOptionPane.showMessageDialog(this, "El nombre del autor contiene caracteres inválidos."); return false;
        }

        //VALIDA QUE EL AÑO SEA UN NÚMERO VÁLIDO Y ESTÉ EN UN RANGO LÓGICO.
        try {
            int anio = Integer.parseInt(txtAnio.getText().trim());
            if (anio < 0 || anio > Year.now().getValue()) {
                JOptionPane.showMessageDialog(this, "Año de publicación inválido."); return false;
            }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Año de publicación debe ser un número."); return false; }

        //VALIDA QUE LA EXISTENCIA SEA UN NÚMERO POSITIVO.
        try {
            int existencia = Integer.parseInt(txtExistencia.getText().trim());
            if (existencia <= 0) { JOptionPane.showMessageDialog(this, "La existencia debe ser mayor a 0."); return false; }
        } catch (NumberFormatException e) { JOptionPane.showMessageDialog(this, "Existencia debe ser un número."); return false; }

        //SI TODAS LAS VALIDACIONES PASAN, RETORNA TRUE.
        return true;
    }

    //PERMITE GUARDAR O MODIFICAR AL PRESIONAR LA TECLA ENTER PARA MEJORAR LA EXPERIENCIA.
    private void configurarEnterParaGuardarModificar() {
    	//SE USA UN KEYADAPTER PARA CAPTURAR EL EVENTO DE LA TECLA ENTER.
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (txtId.getText().trim().isEmpty()) guardarLibro();
                    else modificarLibro();
                }
            }
        };
        
        //SE AÑADE EL LISTENER A LOS CAMPOS DE TEXTO.
        txtTitulo.addKeyListener(enterAdapter);
        txtAutor.addKeyListener(enterAdapter);
        txtAnio.addKeyListener(enterAdapter);
        txtExistencia.addKeyListener(enterAdapter);
    }

    //RESTABLECE LOS CAMPOS DEL FORMULARIO Y LA SELECCIÓN DE LA TABLA.
    private void limpiarCampos() {
        txtId.setText(""); txtTitulo.setText(""); txtAutor.setText(""); txtAnio.setText(""); txtExistencia.setText("");
        btnGuardar.setVisible(true);
        tablaLibros.clearSelection();
    }
    
    //GENERA UN NUEVO ID AUTOINCREMENTAL PARA UN LIBRO.
    private int generarNuevoId() { 
    	return listaLibros.isEmpty() ? 1 : listaLibros.get(listaLibros.size()-1).getid() + 1;
    }

    //CALCULA EL NÚMERO DE EJEMPLARES DISPONIBLES (EXISTENCIA - PRÉSTAMOS PENDIENTES).
    private int calcularDisponibles(int idLibro) {
        List<Prestamo> prestamos = new PrestamoDAO().obtenerTodos();
        long prestados = prestamos.stream()
            .filter(p -> p.getIdLibro() == idLibro && "Pendiente".equals(p.getFechaDevolucion()))
            .count();
        Libro libro = buscarLibroPorId(idLibro);
        return libro == null ? 0 : Math.max(libro.getExistencia() - (int) prestados, 0);
    }

    //BUSCA UN LIBRO EN LA LISTA EN MEMORIA POR SU ID.
    private Libro buscarLibroPorId(int id) {
        return listaLibros.stream().filter(l -> l.getid() == id).findFirst().orElse(null);
    }
}
package gUILayer;

import java.awt.*;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
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
        panelFormulario.add(new JLabel("Existencia:"));
        txtExistencia = new JTextField();
        panelFormulario.add(txtExistencia);
        
        
      

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar Campos");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        String[] columnas = { "ID", "Título", "Autor", "Año","Existencia", "Disponibles"};
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
                        txtExistencia.setText(String.valueOf(libroSeleccionado.getExistencia()));
                        calcularDisponibles(idSeleccionado);
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
        cargarDatosTabla("");
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        listaLibros.stream()
            .filter(libro -> busqueda.isEmpty() || 
                (libro.getTitulo() + libro.getAutor() + libro.getAnioPublicacion())
                    .toLowerCase()
                    .contains(busqueda.toLowerCase()))
            .forEach(libro -> modeloTabla.addRow(new Object[] {
                libro.getid(), libro.getTitulo(), libro.getAutor(), libro.getAnioPublicacion(), 
                libro.getExistencia(),calcularDisponibles(libro.getid())
            }));
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
    	for (Libro libro : listaLibros) {
    	    if (libro.getTitulo().equalsIgnoreCase(txtTitulo.getText().trim()) &&
    	        libro.getAutor().equalsIgnoreCase(txtAutor.getText().trim())) {
    	        libro.setExistencia(libro.getExistencia() + 1);
    	        libroDAO.guardarTodos(listaLibros);
    	        cargarDatosTabla();
    	        limpiarCampos();
    	        JOptionPane.showMessageDialog(this, 
    	            "Se ha agregado otro ejemplar de este título.", 
    	            "Éxito", 
    	            JOptionPane.INFORMATION_MESSAGE);
    	        return;
    	    }
    	}
    	
    	if (!txtId.getText().isEmpty()) {
	        JOptionPane.showMessageDialog(this, 
	            "El registro ya existe. Limpia los campos para agregar un nuevo libro.", 
	            "Registro existente",
	            JOptionPane.ERROR_MESSAGE);
	        return;
	    }

    	if(!validarCampos()) return;
    	
        int anio = Integer.parseInt(txtAnio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim()); 
        int nuevoId = generarNuevoId();

        //MOVER ESTOO A DONDE CORRESPONDE
        
        Libro nuevoLibro = new Libro(nuevoId, txtTitulo.getText(), txtAutor.getText(), anio, existencia);
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

        this.listaLibros = libroDAO.obtenerTodos();
        List<Prestamo> prestamos = new PrestamoDAO().obtenerTodos();

        Libro libroExistente = buscarLibroPorId(idModificar);
        if (libroExistente == null) {
            JOptionPane.showMessageDialog(this, "No se encontró el libro seleccionado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!validarCampos()) return;

        int anio = Integer.parseInt(txtAnio.getText().trim());
        int existencia = Integer.parseInt(txtExistencia.getText().trim());

        // Calcular prestados reales consultando préstamos pendientes
        long prestados = prestamos.stream()
            .filter(p -> p.getIdLibro() == idModificar && "Pendiente".equals(p.getFechaDevolucion()))
            .count();

        if (existencia < prestados) {
            JOptionPane.showMessageDialog(this, "La existencia debe ser mayor o igual a los prestados actuales (" + prestados + ").",
                "Error de Validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        	
        libroExistente.setTitulo(txtTitulo.getText());
        libroExistente.setAutor(txtAutor.getText());
        libroExistente.setAnioPublicacion(anio);
        libroExistente.setExistencia(existencia);
        
        
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

            if(calcularDisponibles(idAEliminar) < libroAEliminar.getExistencia()) {
                JOptionPane.showMessageDialog(this,
                    "No es posible eliminar el libro porque no está disponible.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            
            listaLibros.remove(libroAEliminar);
            libroDAO.guardarTodos(listaLibros);

            cargarDatosTabla();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Libro eliminado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean validarCampos() {
	    if (txtTitulo.getText().trim().isEmpty() || 
	        txtAutor.getText().trim().isEmpty() || 
	        txtAnio.getText().trim().isEmpty()) {
	        
	        JOptionPane.showMessageDialog(this, 
	            "Todos los campos (excepto ID) son obligatorios.", 
	            "Error de Validación", 
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    
	    if (txtTitulo.getText().trim().length() < 2 || txtTitulo.getText().trim().length() > 100) {
	        JOptionPane.showMessageDialog(this,
	            "El título debe tener entre 2 y 100 caracteres.",
	            "Error de Validación",
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }

	    if (txtAutor.getText().trim().length() < 2 || txtAutor.getText().trim().length() > 100) {
	        JOptionPane.showMessageDialog(this,
	            "El autor debe tener entre 2 y 100 caracteres.",
	            "Error de Validación",
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }

	    if (!txtAutor.getText().trim().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ .,'-]+")) {
	        JOptionPane.showMessageDialog(this,
	            "El nombre del autor contiene caracteres inválidos.",
	            "Error de Validación",
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }

	
	    int anio;
	    try {
	        anio = Integer.parseInt(txtAnio.getText().trim());
	    } catch (NumberFormatException ex) {
	        JOptionPane.showMessageDialog(this, 
	            "El año de publicación debe ser un número válido.", 
	            "Error de Formato", 
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	
	    int anioActual = Year.now().getValue();
	    if (anio < 0 || anio > anioActual) {
	        JOptionPane.showMessageDialog(this, 
	            "El año de publicación debe ser menor o igual a: " + anioActual + ".", 
	            "Error de Validación", 
	            JOptionPane.ERROR_MESSAGE);
	        return false;
	    }
	    
	    int existencia; 
	    try {
	    	existencia  = Integer.parseInt(txtExistencia.getText().trim());
	    	if(existencia <= 0) {
	    		JOptionPane.showMessageDialog(this, 
	    	            "La existencia debe ser mayor a 0", 
	    	            "Error de Validación", 
	    	            JOptionPane.ERROR_MESSAGE);
	    		return false; 
	    	}
	    }catch (NumberFormatException ex) {
	    	JOptionPane.showMessageDialog(this, "La existencía debe ser un número válido", "Error en el formato", JOptionPane.ERROR_MESSAGE);
	    	return false;
	    }
	    
	
	    return true;
    }
   
    private void limpiarCampos() {
        txtId.setText("");
        txtTitulo.setText("");
        txtAutor.setText("");
        txtAnio.setText("");
        txtExistencia.setText("");
        tablaLibros.clearSelection();
    }

    private int generarNuevoId() {
        if(listaLibros.isEmpty()) return 1;     
        return listaLibros.get(listaLibros.size()-1).getid() + 1;
    }


    private int calcularDisponibles(int idLibro) {
    	PrestamoDAO prestamoDAO = new PrestamoDAO();
        List<Prestamo> prestamos = prestamoDAO.obtenerTodos();

        //Libros prestados siguen pendientes de devolución
        long prestados = prestamos.stream()
                .filter(p -> p.getIdLibro() == idLibro && "Pendiente".equals(p.getFechaDevolucion()))
                .count();

        Libro libro = buscarLibroPorId(idLibro);
        if (libro == null) return 0;
        int disponibles = libro.getExistencia() - (int) prestados;
        return Math.max(disponibles, 0);
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
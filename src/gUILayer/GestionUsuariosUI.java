package gUILayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//import java.awt.event.KeyAdapter;
//import java.awt.event.KeyEvent;
import java.util.List;
import entidades.Usuario;
import DAO.UsuarioDAO;


public class GestionUsuariosUI extends JPanel implements Buscable {
	private static final long serialVersionUID = 1L;
	
    private JTextField txtId, txtNombre, txtApellido, txtTelefono;
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private UsuarioDAO usuarioDAO;
    private List<Usuario> listaUsuarios;

    public GestionUsuariosUI() {
        usuarioDAO = new UsuarioDAO();

        JPanel panelFormulario = new JPanel(new GridLayout(5, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));

        panelFormulario.add(new JLabel("ID Usuario (Generado):"));
        txtId = new JTextField();
        txtId.setEditable(false); 
        panelFormulario.add(txtId);
        panelFormulario.add(new JLabel("Nombre:"));
        txtNombre = new JTextField();
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Apellido:"));
        txtApellido = new JTextField();
        panelFormulario.add(txtApellido);
        panelFormulario.add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField();
        panelFormulario.add(txtTelefono);

        // --- Panel de Botones ---
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar Campos");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        String[] columnas = { "ID", "Nombre", "Apellido", "Teléfono" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaUsuarios = new JTable(modeloTabla);
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);

        this.actualizarDatos();
        cargarDatosTabla();

        btnGuardar.addActionListener(e-> guardarUsuario());
        btnModificar.addActionListener(e -> modificarUsuario());
        btnEliminar.addActionListener(e-> eliminarUsuario());
        btnLimpiar.addActionListener(e-> limpiarCampos());

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int filaSeleccionada = tablaUsuarios.getSelectedRow();
                if (filaSeleccionada >= 0) {
                    // Ahora se lee el ID como un entero
                    int idSeleccionado = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
                    Usuario usuarioSeleccionado = buscarUsuarioPorId(idSeleccionado);

                    if (usuarioSeleccionado != null) {
                        txtId.setText(String.valueOf(usuarioSeleccionado.getIdUsuario()));
                        txtNombre.setText(usuarioSeleccionado.getNombre());
                        txtApellido.setText(usuarioSeleccionado.getApellido());
                        txtTelefono.setText(usuarioSeleccionado.getTelefono());
                    }
                }
            }
        });

        setLayout(new BorderLayout(10, 10));
        add(panelFormulario, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);
    }

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario usuario : listaUsuarios) {
            Object[] fila = { usuario.getIdUsuario(), usuario.getNombre(), usuario.getApellido(), usuario.getTelefono() };
            modeloTabla.addRow(fila);
        }
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        for (Usuario usuario : listaUsuarios) {
            Object[] fila = { usuario.getIdUsuario(), usuario.getNombre(), usuario.getApellido(), usuario.getTelefono() };
            for(Object elemento : fila){
                if(elemento != null && elemento.toString().toLowerCase().contains(busqueda.toLowerCase())){
                    modeloTabla.addRow(fila);
                    break;
                }
            }
        }
    }

    // --- Lógica de Negocio Adaptada a ID entero ---
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    public void actualizarDatos() {
        this.listaUsuarios = this.usuarioDAO.obtenerTodos();
        this.cargarDatosTabla();
    }

    private void guardarUsuario() {
    	
    	if(!validarCampos()) {
    		return;
    	}
    	
        // Genera un nuevo ID automáticamente
        int nuevoId = generarNuevoId();

        Usuario nuevoUsuario = new Usuario(nuevoId, txtNombre.getText(), txtApellido.getText(), txtTelefono.getText());
        listaUsuarios.add(nuevoUsuario);
        usuarioDAO.guardarTodos(listaUsuarios);
        cargarDatosTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Usuario guardado exitosamente con ID: " + nuevoId, "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void modificarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla para modificar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtiene el ID directamente de la tabla
        int idModificar = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
        Usuario usuarioExistente = buscarUsuarioPorId(idModificar);

        if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El Nombre y Apellido son obligatorios.", "Error de Validación",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        usuarioExistente.setNombre(txtNombre.getText());
        usuarioExistente.setApellido(txtApellido.getText());
        usuarioExistente.setTelefono(txtTelefono.getText());

        usuarioDAO.guardarTodos(listaUsuarios);
        cargarDatosTabla();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Usuario modificado exitosamente.", "Éxito",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarUsuario() {
        int filaSeleccionada = tablaUsuarios.getSelectedRow();
        if (filaSeleccionada < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario de la tabla para eliminar.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Estás seguro de que deseas eliminar este usuario?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION);

        if (opcion == JOptionPane.YES_OPTION) {
            int idAEliminar = (int) modeloTabla.getValueAt(filaSeleccionada, 0);
            Usuario usuarioAEliminar = buscarUsuarioPorId(idAEliminar);

            listaUsuarios.remove(usuarioAEliminar);
            usuarioDAO.guardarTodos(listaUsuarios);
            cargarDatosTabla();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Usuario eliminado exitosamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private boolean validarCampos() {
    	 if (!txtId.getText().isEmpty()) {
             JOptionPane.showMessageDialog(this, "El registro ya existe", "Registro existente",
                 JOptionPane.ERROR_MESSAGE);
                 return false;
         }

         if (txtNombre.getText().isEmpty() || txtApellido.getText().isEmpty() || txtTelefono.getText().isEmpty()) {
             JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación",
                     JOptionPane.ERROR_MESSAGE);
             return false;
         }
         
         if(txtNombre.getText().trim().length() < 2 || txtNombre.getText().trim().length() > 100) {
        	 JOptionPane.showMessageDialog(this,
     	            "El Nombre debe tener entre 2 y 100 caracteres.",
     	            "Error de Validación",
     	            JOptionPane.ERROR_MESSAGE);
     	        return false;
         }
         
         if(txtApellido.getText().trim().length() < 2 || txtApellido.getText().trim().length() > 100) {
        	 JOptionPane.showMessageDialog(this,
     	            "El Apellido debe tener entre 2 y 100 caracteres.",
     	            "Error de Validación",
     	            JOptionPane.ERROR_MESSAGE);
     	        return false;
         }
         
         String telefono = txtTelefono.getText().trim();

	      // Validar que tenga exactamente 10 dígitos numéricos
	      if (!telefono.matches("\\d{10}")) {
	          JOptionPane.showMessageDialog(this, 
	                  "El teléfono debe contener exactamente 10 dígitos numéricos", 
	                  "Error de Validación", 
	                  JOptionPane.ERROR_MESSAGE);
	          return false;
	      }

         
        return true;
    }
    
    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        tablaUsuarios.clearSelection();
    }

    private Usuario buscarUsuarioPorId(int id) {
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getIdUsuario() == id) {
                return usuario;
            }
        }
        return null;
    }

    private int generarNuevoId() {
        int maxId = 0;
        for (Usuario usuario : listaUsuarios) {
            if (usuario.getIdUsuario() > maxId) {
                maxId = usuario.getIdUsuario();
            }
        }
        return maxId + 1;
    }
}
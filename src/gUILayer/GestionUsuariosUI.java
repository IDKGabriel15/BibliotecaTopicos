package gUILayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import entidades.Usuario;
import DAO.UsuarioDAO;

public class GestionUsuariosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    // Campos de formulario
    private JTextField txtId, txtNombre, txtApellido, txtTelefono;

    // Botones
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;

    // Tabla
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    // DAO y lista
    private UsuarioDAO usuarioDAO;
    private List<Usuario> listaUsuarios;

    public GestionUsuariosUI() {
        usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(10, 10));

        initFormulario();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        JScrollPane panelTabla = initTabla();

        add(panelFormulario, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos
        configurarEventos();
        configurarEnterParaGuardarModificar();

        // Cargar datos iniciales
        actualizarDatos();
    }

    /* ---------- Inicialización de componentes ---------- */

    private void initFormulario() {
        txtId = new JTextField(5);
        txtId.setEditable(false);
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtTelefono = new JTextField(10);
    }

    private JPanel initPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridLayout(4, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Usuario"));

        panelFormulario.add(new JLabel("ID Usuario (Generado):"));
        panelFormulario.add(txtId);

        panelFormulario.add(new JLabel("Nombre:"));
        panelFormulario.add(txtNombre);

        panelFormulario.add(new JLabel("Apellido:"));
        panelFormulario.add(txtApellido);

        panelFormulario.add(new JLabel("Teléfono:"));
        panelFormulario.add(txtTelefono);

        return panelFormulario;
    }

    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar");
        btnModificar = new JButton("Modificar");
        btnEliminar = new JButton("Eliminar");
        btnLimpiar = new JButton("Limpiar");

        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);

        return panelBotones;
    }

    private JScrollPane initTabla() {
        String[] columnas = { "ID", "Nombre", "Apellido", "Teléfono" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        tablaUsuarios = new JTable(modeloTabla);
        return new JScrollPane(tablaUsuarios);
    }

    /* ---------- Eventos ---------- */

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnModificar.addActionListener(e -> modificarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tablaUsuarios.getSelectedRow();
                if (fila >= 0) {
                    btnGuardar.setVisible(false);
                    int idSeleccionado = (int) modeloTabla.getValueAt(fila, 0);
                    Usuario u = buscarUsuarioPorId(idSeleccionado);
                    if (u != null) {
                        txtId.setText(String.valueOf(u.getIdUsuario()));
                        txtNombre.setText(u.getNombre());
                        txtApellido.setText(u.getApellido());
                        txtTelefono.setText(u.getTelefono());
                    }
                }
            }
        });
    }

    private void configurarEnterParaGuardarModificar() {
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (txtId.getText().trim().isEmpty()) {
                        guardarUsuario();
                    } else {
                        modificarUsuario();
                    }
                }
            }
        };
        txtNombre.addKeyListener(enterAdapter);
        txtApellido.addKeyListener(enterAdapter);
        txtTelefono.addKeyListener(enterAdapter);
    }

    /* ---------- Carga de datos ---------- */

    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : listaUsuarios) {
            Object[] fila = { u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getTelefono() };
            modeloTabla.addRow(fila);
        }
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        for (Usuario u : listaUsuarios) {
            Object[] fila = { u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getTelefono() };
            for (Object elem : fila) {
                if (elem != null && elem.toString().toLowerCase().contains(busqueda.toLowerCase())) {
                    modeloTabla.addRow(fila);
                    break;
                }
            }
        }
    }

    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    public void actualizarDatos() {
        listaUsuarios = usuarioDAO.obtenerTodos();
        cargarDatosTabla();
    }

    /* ---------- Acciones ---------- */

    private void guardarUsuario() {
        if (!validarCampos()) return;

        int nuevoId = generarNuevoId();
        Usuario u = new Usuario(nuevoId, txtNombre.getText(), txtApellido.getText(), txtTelefono.getText());

        listaUsuarios.add(u);
        usuarioDAO.guardarTodos(listaUsuarios);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Usuario guardado con ID: " + nuevoId, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void modificarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = (int) modeloTabla.getValueAt(fila, 0);
        Usuario u = buscarUsuarioPorId(id);

        if (!validarCamposSinId()) return;

        u.setNombre(txtNombre.getText());
        u.setApellido(txtApellido.getText());
        u.setTelefono(txtTelefono.getText());

        usuarioDAO.guardarTodos(listaUsuarios);
        actualizarDatos();
        limpiarCampos();
        JOptionPane.showMessageDialog(this, "Usuario modificado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
    }

    private void eliminarUsuario() {
        int fila = tablaUsuarios.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int opcion = JOptionPane.showConfirmDialog(this, "¿Eliminar este usuario?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (opcion == JOptionPane.YES_OPTION) {
            int id = (int) modeloTabla.getValueAt(fila, 0);
            Usuario u = buscarUsuarioPorId(id);

            listaUsuarios.remove(u);
            usuarioDAO.guardarTodos(listaUsuarios);
            actualizarDatos();
            limpiarCampos();
            JOptionPane.showMessageDialog(this, "Usuario eliminado.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /* ---------- Validaciones ---------- */

    private boolean validarCampos() {
        if (!txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este registro ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return validarCamposSinId();
    }

    private boolean validarCamposSinId() {
        if (txtNombre.getText().trim().isEmpty() ||
            txtApellido.getText().trim().isEmpty() ||
            txtTelefono.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (txtNombre.getText().trim().length() < 2 || txtNombre.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El Nombre debe tener entre 2 y 100 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (txtApellido.getText().trim().length() < 2 || txtApellido.getText().trim().length() > 100) {
            JOptionPane.showMessageDialog(this, "El Apellido debe tener entre 2 y 100 caracteres.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (!txtTelefono.getText().trim().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "El teléfono debe contener exactamente 10 dígitos numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        btnGuardar.setVisible(true);
        tablaUsuarios.clearSelection();
    }

    /* ---------- Utilitarios ---------- */

    private Usuario buscarUsuarioPorId(int id) {
        for (Usuario u : listaUsuarios) {
            if (u.getIdUsuario() == id) return u;
        }
        return null;
    }

    private int generarNuevoId() {
        int maxId = 0;
        for (Usuario u : listaUsuarios) {
            if (u.getIdUsuario() > maxId) maxId = u.getIdUsuario();
        }
        return maxId + 1;
    }
}

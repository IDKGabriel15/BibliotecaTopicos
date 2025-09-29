package gUILayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import entidades.Usuario;
import DAO.UsuarioDAO;

//SE CREA LA CLASE GESTIONUSUARIOSUI, EL PANEL ENCARGADO DE LA INTERFAZ
public class GestionUsuariosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    //SE DECLARAN LOS CAMPOS DE TEXTO PARA EL FORMULARIO DE USUARIO.
    private JTextField txtId, txtNombre, txtApellido, txtTelefono;

    //SE DECLARAN LOS BOTONES PARA LAS ACCIONES CRUD (CREAR, LEER, ACTUALIZAR, ELIMINAR).
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;

    //COMPONENTES PARA LA TABLA QUE MOSTRARÁ LOS USUARIOS.
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    //OBJETO DAO PARA LA PERSISTENCIA DE DATOS Y LISTA EN MEMORIA.
    private UsuarioDAO usuarioDAO;
    private List<Usuario> listaUsuarios;

    //CONSTRUCTOR DE LA CLASE. SE EJECUTA AL CREAR EL PANEL.
    public GestionUsuariosUI() {
        //SE INICIALIZA EL OBJETO DAO.
        usuarioDAO = new UsuarioDAO();

        setLayout(new BorderLayout(10, 10));

        //SE LLAMAN LOS MÉTODOS PARA CONSTRUIR LA INTERFAZ GRÁFICA.
        initFormulario();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        JScrollPane panelTabla = initTabla();

        //SE AÑADEN LOS PANELES CREADOS AL PANEL PRINCIPAL.
        add(panelFormulario, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        //SE CONFIGURAN LOS LISTENERS PARA LOS EVENTOS DE BOTONES Y TECLADO.
        configurarEventos();
        configurarEnterParaGuardarModificar();

        //SE CARGAN LOS DATOS INICIALES DE USUARIOS EN LA TABLA.
        actualizarDatos();
    }

    /* ---------- INICIALIZACIÓN DE COMPONENTES ---------- */

    //INICIALIZA LOS CAMPOS DE TEXTO DEL FORMULARIO.
    private void initFormulario() {
        txtId = new JTextField(5);
        txtId.setEditable(false); //EL CAMPO ID NO ES EDITABLE POR EL USUARIO.
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtTelefono = new JTextField(10);
    }

    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LAS ETIQUETAS Y CAMPOS DEL FORMULARIO.
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

    //CREA Y CONFIGURA EL PANEL QUE CONTIENE LOS BOTONES DE ACCIÓN.
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

    //CREA Y CONFIGURA LA TABLA Y SU MODELO PARA MOSTRAR LOS USUARIOS.
    private JScrollPane initTabla() {
        String[] columnas = { "ID", "Nombre", "Apellido", "Teléfono" };
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; } //LA TABLA NO ES EDITABLE.
        };

        tablaUsuarios = new JTable(modeloTabla);
        return new JScrollPane(tablaUsuarios);
    }

    /* ---------- CONFIGURACIÓN DE EVENTOS ---------- */

    //ASIGNA LAS ACCIONES A LOS BOTONES Y A LA SELECCIÓN EN LA TABLA.
    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnModificar.addActionListener(e -> modificarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        //EVENTO PARA CARGAR LOS DATOS EN EL FORMULARIO AL HACER CLIC EN UNA FILA DE LA TABLA.
        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tablaUsuarios.getSelectedRow();
                if (fila >= 0) {
                    btnGuardar.setVisible(false); //SE OCULTA EL BOTÓN GUARDAR AL EDITAR.
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

    //PERMITE GUARDAR O MODIFICAR UN USUARIO AL PRESIONAR LA TECLA ENTER.
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

    /* ---------- CARGA Y MANEJO DE DATOS EN LA TABLA ---------- */

    //CARGA TODOS LOS USUARIOS DE LA LISTA EN LA TABLA SIN FILTRO.
    private void cargarDatosTabla() {
        modeloTabla.setRowCount(0);
        for (Usuario u : listaUsuarios) {
            Object[] fila = { u.getIdUsuario(), u.getNombre(), u.getApellido(), u.getTelefono() };
            modeloTabla.addRow(fila);
        }
    }

    //CARGA LOS USUARIOS EN LA TABLA APLICANDO UN FILTRO DE BÚSQUEDA.
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

    //IMPLEMENTACIÓN DEL MÉTODO DE LA INTERFAZ BUSCABLE PARA FILTRAR DATOS.
    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    //ACTUALIZA LA LISTA DE USUARIOS DESDE EL ARCHIVO Y REFRESCA LA TABLA.
    public void actualizarDatos() {
        listaUsuarios = usuarioDAO.obtenerTodos();
        cargarDatosTabla();
    }

    /* ---------- ACCIONES CRUD (GUARDAR, MODIFICAR, ELIMINAR) ---------- */

    //CONTIENE LA LÓGICA PARA GUARDAR UN NUEVO USUARIO.
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

    //CONTIENE LA LÓGICA PARA MODIFICAR UN USUARIO EXISTENTE.
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

    //CONTIENE LA LÓGICA PARA ELIMINAR UN USUARIO SELECCIONADO.
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

    /* ---------- VALIDACIONES DE DATOS ---------- */

    //VALIDA LOS CAMPOS ANTES DE GUARDAR UN NUEVO USUARIO (INCLUYE VALIDACIÓN DE ID).
    private boolean validarCampos() {
        if (!txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Este registro ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return validarCamposSinId();
    }

    //VALIDA LOS CAMPOS COMUNES TANTO PARA GUARDAR COMO PARA MODIFICAR.
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
        //VALIDA QUE EL TELÉFONO CONTENGA EXACTAMENTE 10 DÍGITOS.
        if (!txtTelefono.getText().trim().matches("\\d{10}")) {
            JOptionPane.showMessageDialog(this, "El teléfono debe contener exactamente 10 dígitos numéricos.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    //RESTABLECE LOS CAMPOS DEL FORMULARIO Y LA SELECCIÓN DE LA TABLA.
    private void limpiarCampos() {
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtTelefono.setText("");
        btnGuardar.setVisible(true); //MUESTRA EL BOTÓN GUARDAR.
        tablaUsuarios.clearSelection();
    }

    /* ---------- MÉTODOS UTILITARIOS ---------- */

    //BUSCA UN USUARIO EN LA LISTA EN MEMORIA POR SU ID.
    private Usuario buscarUsuarioPorId(int id) {
        for (Usuario u : listaUsuarios) {
            if (u.getIdUsuario() == id) return u;
        }
        return null;
    }

    //GENERA UN NUEVO ID AUTOINCREMENTAL PARA UN USUARIO.
    private int generarNuevoId() {
        int maxId = 0;
        for (Usuario u : listaUsuarios) {
            if (u.getIdUsuario() > maxId) maxId = u.getIdUsuario();
        }
        return maxId + 1;
    }
}
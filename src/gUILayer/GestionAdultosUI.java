// Archivo: gUILayer/GestionAdultosUI.java
package gUILayer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import entidades.UsuarioTablaVista;
import DAO.UsuarioDAO;

// 1. Nombre de la clase cambiado
public class GestionAdultosUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    private JTextField txtId, txtNombre, txtApellido, txtInicial;
    private JTextField txtTelefono, txtCalle, txtCiudad, txtEstado, txtCPostal;
    private JSpinner spinnerFechaExp;
    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    private UsuarioDAO usuarioDAO;
    private List<UsuarioTablaVista> listaUsuarios; 
    private UsuarioTablaVista usuarioSeleccionado = null;

    public GestionAdultosUI() {
        usuarioDAO = new UsuarioDAO();
        setLayout(new BorderLayout(10, 10));

        initFormulario();
        JPanel panelFormulario = initPanelFormulario();
        JPanel panelBotones = initPanelBotones();
        JScrollPane panelTabla = initTabla();

        add(panelFormulario, BorderLayout.NORTH);
        add(panelTabla, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        configurarEventos();
        configurarEnterParaGuardarModificar();
        actualizarDatos();
    }

    private void initFormulario() {
        txtId = new JTextField(10);
        txtId.setEditable(false);
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtInicial = new JTextField(1);
        txtCalle = new JTextField(20);
        txtCiudad = new JTextField(15);
        txtEstado = new JTextField(15);
        txtCPostal = new JTextField(5);
        txtTelefono = new JTextField(10);
        
        spinnerFechaExp = new JSpinner(new SpinnerDateModel(new Date(), null, null, java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFechaExp, "dd/MM/yyyy");
        spinnerFechaExp.setEditor(editor);
    }

    private JPanel initPanelFormulario() {
        JPanel panelContenedor = new JPanel(new GridLayout(1, 2, 15, 0));
        panelContenedor.setBorder(BorderFactory.createTitledBorder("Datos del Usuario Adulto")); // Título específico

        JPanel panelPersonal = new JPanel(new GridLayout(6, 2, 10, 10));
        panelPersonal.add(new JLabel("ID Usuario (Generado):"));
        panelPersonal.add(txtId);
        panelPersonal.add(new JLabel("Nombre (*):"));
        panelPersonal.add(txtNombre);
        panelPersonal.add(new JLabel("Apellido (*):"));
        panelPersonal.add(txtApellido);
        panelPersonal.add(new JLabel("Inicial:"));
        panelPersonal.add(txtInicial);
        panelPersonal.add(new JLabel("Teléfono:"));
        panelPersonal.add(txtTelefono);
        panelPersonal.add(new JLabel("Fecha Exp. (*):"));
        panelPersonal.add(spinnerFechaExp);
        panelContenedor.add(panelPersonal);
        
        JPanel panelDireccion = new JPanel(new GridLayout(6, 2, 10, 10));
        panelDireccion.add(new JLabel("Calle:"));
        panelDireccion.add(txtCalle);
        panelDireccion.add(new JLabel("Ciudad:"));
        panelDireccion.add(txtCiudad);
        panelDireccion.add(new JLabel("Estado:"));
        panelDireccion.add(txtEstado);
        panelDireccion.add(new JLabel("Código Postal:"));
        panelDireccion.add(txtCPostal);
        panelDireccion.add(new JLabel(""));
        panelDireccion.add(new JLabel(""));
        panelDireccion.add(new JLabel(""));
        panelDireccion.add(new JLabel(""));
        panelContenedor.add(panelDireccion);

        return panelContenedor;
    }

    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Adulto");
        btnModificar = new JButton("Modificar Adulto");
        btnEliminar = new JButton("Eliminar Usuario");
        btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        return panelBotones;
    }

    private JScrollPane initTabla() {
        String[] columnas = { "ID", "Nombre", "Apellido", "Teléfono", "Fecha Registro" }; // Tipo ya no es necesario
        modeloTabla = new DefaultTableModel(columnas, 0) {
            private static final long serialVersionUID = 1L;
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
        tablaUsuarios = new JTable(modeloTabla);
        return new JScrollPane(tablaUsuarios);
    }

    private void configurarEventos() {
        btnGuardar.addActionListener(e -> guardarUsuario());
        btnModificar.addActionListener(e -> modificarUsuario());
        btnEliminar.addActionListener(e -> eliminarUsuario());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        tablaUsuarios.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int fila = tablaUsuarios.getSelectedRow();
                if (fila >= 0) {
                    String idSeleccionado = (String) modeloTabla.getValueAt(fila, 0);
                    usuarioSeleccionado = buscarUsuarioPorNo(idSeleccionado);
                    if (usuarioSeleccionado != null) {
                        cargarFormulario(usuarioSeleccionado);
                    }
                }
            }
        });
    }
    
    private void cargarFormulario(UsuarioTablaVista u) {
        // Esta GUI solo puede cargar Adultos
        if (!"Adulto".equals(u.getTipoDescripcion())) return; 

        txtId.setText(u.getUsuarioNo());
        txtNombre.setText(u.getNombre());
        txtApellido.setText(u.getApellido());
        txtInicial.setText(u.getInicial());
        txtTelefono.setText(u.getTelefono());
        txtCalle.setText(u.getCalle());
        txtCiudad.setText(u.getCiudad());
        txtEstado.setText(u.getEstado());
        txtCPostal.setText(u.getCpostal());
        spinnerFechaExp.setValue(u.getFechaExp() != null ? u.getFechaExp() : new Date());
        
        btnGuardar.setVisible(false);
        btnModificar.setVisible(true);
        btnEliminar.setVisible(true);
    }

    private void configurarEnterParaGuardarModificar() {
        // ... (Sin cambios)
        KeyAdapter enterAdapter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (btnGuardar.isVisible()) {
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
        txtCalle.addKeyListener(enterAdapter);
        txtCiudad.addKeyListener(enterAdapter);
        txtEstado.addKeyListener(enterAdapter);
        txtCPostal.addKeyListener(enterAdapter);
    }

    private void cargarDatosTabla() {
        cargarDatosTabla(""); // Carga la tabla filtrando por "Adulto"
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        // 2. Llama al DAO pidiendo SOLO Adultos ('01')
        listaUsuarios = usuarioDAO.obtenerUsuariosVista("01"); 
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        
        String b = busqueda.toLowerCase();

        for (UsuarioTablaVista u : listaUsuarios) {
            String datos = (u.getUsuarioNo() + u.getNombre() + u.getApellido() + u.getTelefono()).toLowerCase();
            if (busqueda.isEmpty() || datos.contains(b)) {
                Object[] fila = { 
                    u.getUsuarioNo(), 
                    u.getNombre(), 
                    u.getApellido(), 
                    u.getTelefono() != null ? u.getTelefono() : "N/A",
                    df.format(u.getFechaRegistro())
                };
                modeloTabla.addRow(fila);
            }
        }
    }

    @Override
    public void buscar(String criterio) {
        limpiarCampos();
        cargarDatosTabla(criterio);
    }

    @Override
    public void actualizarDatos() {
        // 3. Llama al DAO pidiendo SOLO Adultos ('01')
        listaUsuarios = usuarioDAO.obtenerUsuariosVista("01");
        cargarDatosTabla();
    }

    private void guardarUsuario() {
        if (!validarCamposGuardar()) return;
        try {
            String msg = usuarioDAO.registrarAdulto(
                txtApellido.getText().trim(),
                txtNombre.getText().trim(),
                txtInicial.getText().trim(),
                txtCalle.getText().trim(),
                txtCiudad.getText().trim(),
                txtEstado.getText().trim(),
                txtCPostal.getText().trim(),
                txtTelefono.getText().trim(),
                (Date) spinnerFechaExp.getValue()
            );
            JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
            limpiarCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al guardar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void modificarUsuario() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un adulto.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!validarCamposModificar()) return;
        try {
            String msg = usuarioDAO.modificarAdulto(
                usuarioSeleccionado.getUsuarioNo(),
                txtApellido.getText().trim(),
                txtNombre.getText().trim(),
                txtInicial.getText().trim(),
                txtCalle.getText().trim(),
                txtCiudad.getText().trim(),
                txtEstado.getText().trim(),
                txtCPostal.getText().trim(),
                txtTelefono.getText().trim(),
                (Date) spinnerFechaExp.getValue()
            );
            JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
            limpiarCampos();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al modificar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
        // ... (Sin cambios, sp_EliminarUsuario maneja la lógica)
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this, "Selecciona un usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int opcion = JOptionPane.showConfirmDialog(this, 
                "¿Eliminar este usuario? (" + usuarioSeleccionado.getNombre() + " " + usuarioSeleccionado.getApellido() + ")\n" +
                "ID: " + usuarioSeleccionado.getUsuarioNo() + "\n" +
                "¡Esta acción no se puede deshacer!", 
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (opcion == JOptionPane.YES_OPTION) {
            try {
                String msg = usuarioDAO.eliminarUsuario(usuarioSeleccionado.getUsuarioNo());
                JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
                actualizarDatos();
                limpiarCampos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean validarCamposGuardar() {
        // ... (Sin cambios)
        if (!txtId.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Limpia los campos para guardar un nuevo usuario.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return validarCamposModificar();
    }

    private boolean validarCamposModificar() {
        // ... (Sin cambios)
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Apellido son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        Date fechaExp = (Date) spinnerFechaExp.getValue();
        if (fechaExp.before(new Date())) {
            JOptionPane.showMessageDialog(this, "La fecha de expiración no puede ser en el pasado.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String tel = txtTelefono.getText().trim();
        if (!tel.isEmpty() && !tel.matches("\\d{7,15}")) {
            JOptionPane.showMessageDialog(this, "El teléfono debe contener solo dígitos (7-15).", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        String inicial = txtInicial.getText().trim();
        if (!inicial.isEmpty() && inicial.length() > 1) {
            JOptionPane.showMessageDialog(this, "La inicial solo puede ser un caracter.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void limpiarCampos() {
        // ... (Sin cambios)
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtInicial.setText("");
        txtTelefono.setText("");
        txtCalle.setText("");
        txtCiudad.setText("");
        txtEstado.setText("");
        txtCPostal.setText("");
        spinnerFechaExp.setValue(new Date());
        
        btnGuardar.setVisible(true);
        btnModificar.setVisible(true);
        btnEliminar.setVisible(true);
        
        tablaUsuarios.clearSelection();
        usuarioSeleccionado = null;
    }

    private UsuarioTablaVista buscarUsuarioPorNo(String usuarioNo) {
        // ... (Sin cambios)
        if (listaUsuarios == null) return null;
        for (UsuarioTablaVista u : listaUsuarios) {
            if (u.getUsuarioNo().equals(usuarioNo)) return u;
        }
        return null;
    }
}
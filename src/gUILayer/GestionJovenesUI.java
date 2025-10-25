// Archivo: gUILayer/GestionJovenesUI.java (NUEVO)
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
import entidades.UsuarioVista; // Para el ComboBox de Tutores
import DAO.UsuarioDAO;

public class GestionJovenesUI extends JPanel implements Buscable {
    private static final long serialVersionUID = 1L;

    // --- Campos de formulario ---
    private JTextField txtId, txtNombre, txtApellido, txtInicial;
    private JSpinner spinnerFechaNac;
    private JComboBox<UsuarioVista> cmbTutores; // ComboBox para Tutores

    private JButton btnGuardar, btnEliminar, btnModificar, btnLimpiar;
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;
    
    private UsuarioDAO usuarioDAO;
    private List<UsuarioTablaVista> listaUsuarios; 
    private List<UsuarioVista> listaTutores; // Lista para el ComboBox
    private UsuarioTablaVista usuarioSeleccionado = null;

    public GestionJovenesUI() {
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
        actualizarDatos(); // Carga Jóvenes y Tutores
    }

    /* ---------- Inicialización de componentes ---------- */

    private void initFormulario() {
        txtId = new JTextField(10);
        txtId.setEditable(false);
        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtInicial = new JTextField(1);
        
        // JSpinner para la Fecha de Nacimiento
        spinnerFechaNac = new JSpinner(new SpinnerDateModel(new Date(), null, new Date(), java.util.Calendar.DAY_OF_MONTH));
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinnerFechaNac, "dd/MM/yyyy");
        spinnerFechaNac.setEditor(editor);
        
        // ComboBox para los Tutores
        cmbTutores = new JComboBox<>();
    }

    private JPanel initPanelFormulario() {
        JPanel panelFormulario = new JPanel(new GridLayout(6, 2, 10, 10));
        panelFormulario.setBorder(BorderFactory.createTitledBorder("Datos del Usuario Joven"));
        
        panelFormulario.add(new JLabel("ID Usuario (Generado):"));
        panelFormulario.add(txtId);
        panelFormulario.add(new JLabel("Nombre (*):"));
        panelFormulario.add(txtNombre);
        panelFormulario.add(new JLabel("Apellido (*):"));
        panelFormulario.add(txtApellido);
        panelFormulario.add(new JLabel("Inicial:"));
        panelFormulario.add(txtInicial);
        panelFormulario.add(new JLabel("Fecha Nac. (*):"));
        panelFormulario.add(spinnerFechaNac);
        panelFormulario.add(new JLabel("Tutor (Adulto) (*):"));
        panelFormulario.add(cmbTutores);

        return panelFormulario;
    }

    private JPanel initPanelBotones() {
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnGuardar = new JButton("Guardar Joven");
        btnModificar = new JButton("Modificar Joven");
        btnEliminar = new JButton("Eliminar Usuario");
        btnLimpiar = new JButton("Limpiar");
        panelBotones.add(btnGuardar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnLimpiar);
        return panelBotones;
    }

    private JScrollPane initTabla() {
        // La tabla mostrará Jóvenes y su Tutor
        String[] columnas = { "ID", "Nombre", "Apellido", "Fecha Nac.", "Tutor ID", "Fecha Registro" };
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
        if (!"Joven".equals(u.getTipoDescripcion())) return; 

        txtId.setText(u.getUsuarioNo());
        txtNombre.setText(u.getNombre());
        txtApellido.setText(u.getApellido());
        txtInicial.setText(u.getInicial());
        
        // Asumimos que VistaUsuarios fue actualizada para incluir 'fecha_nac' y 'tutor_usuario_no'
        // (Lo hicimos en el Paso 1 de la respuesta anterior)
        spinnerFechaNac.setValue(u.getFechaNac() != null ? u.getFechaNac() : new Date());
        
        // Seleccionar el tutor en el ComboBox
        String tutorId = u.getTutorUsuarioNo();
        if (tutorId != null) {
            for (int i = 0; i < cmbTutores.getItemCount(); i++) {
                if (cmbTutores.getItemAt(i).getUsuarioNo().equals(tutorId)) {
                    cmbTutores.setSelectedIndex(i);
                    break;
                }
            }
        } else {
            cmbTutores.setSelectedIndex(-1);
        }
        
        btnGuardar.setVisible(false);
        btnModificar.setVisible(true);
        btnEliminar.setVisible(true);
    }

    /* ---------- Carga de datos ---------- */

    private void cargarTutoresComboBox() {
        listaTutores = usuarioDAO.obtenerTutoresPosibles();
        cmbTutores.removeAllItems();
        cmbTutores.addItem(null); // Opción nula
        for (UsuarioVista tutor : listaTutores) {
            cmbTutores.addItem(tutor);
        }
    }

    private void cargarDatosTabla() {
        cargarDatosTabla("");
    }

    private void cargarDatosTabla(String busqueda) {
        modeloTabla.setRowCount(0);
        // Llama al DAO pidiendo SOLO Jóvenes ('02')
        listaUsuarios = usuarioDAO.obtenerUsuariosVista("02"); 
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        
        String b = busqueda.toLowerCase();

        for (UsuarioTablaVista u : listaUsuarios) {
            String datos = (u.getUsuarioNo() + u.getNombre() + u.getApellido()).toLowerCase();
            if (busqueda.isEmpty() || datos.contains(b)) {
                Object[] fila = { 
                    u.getUsuarioNo(), 
                    u.getNombre(), 
                    u.getApellido(), 
                    u.getFechaNac() != null ? df.format(u.getFechaNac()) : "N/A",
                    u.getTutorUsuarioNo() != null ? u.getTutorUsuarioNo() : "N/A",
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
        cargarTutoresComboBox(); // Carga/Recarga los tutores
        listaUsuarios = usuarioDAO.obtenerUsuariosVista("02"); // Carga Jóvenes
        cargarDatosTabla();
    }

    /* ---------- Acciones ---------- */

    private void guardarUsuario() {
        if (!validarCampos()) return;

        try {
            UsuarioVista tutor = (UsuarioVista) cmbTutores.getSelectedItem();
            
            String msg = usuarioDAO.registrarJoven(
                txtApellido.getText().trim(),
                txtNombre.getText().trim(),
                txtInicial.getText().trim(),
                (Date) spinnerFechaNac.getValue(),
                tutor.getUsuarioNo()
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
            JOptionPane.showMessageDialog(this, "Selecciona un joven.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!validarCampos()) return;

        try {
            UsuarioVista tutor = (UsuarioVista) cmbTutores.getSelectedItem();
            
            String msg = usuarioDAO.modificarJoven(
                usuarioSeleccionado.getUsuarioNo(),
                txtApellido.getText().trim(),
                txtNombre.getText().trim(),
                txtInicial.getText().trim(),
                (Date) spinnerFechaNac.getValue(),
                tutor.getUsuarioNo()
            );
            
            JOptionPane.showMessageDialog(this, msg, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            actualizarDatos();
            limpiarCampos();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error al modificar en BD:\n" + e.getMessage(), "Error SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void eliminarUsuario() {
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

    /* ---------- Validaciones ---------- */

    private boolean validarCampos() {
        if (txtNombre.getText().trim().isEmpty() || txtApellido.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y Apellido son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (cmbTutores.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar un Tutor.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Date fechaNac = (Date) spinnerFechaNac.getValue();
        if (fechaNac.after(new Date())) {
            JOptionPane.showMessageDialog(this, "La fecha de nacimiento no puede ser en el futuro.", "Error", JOptionPane.ERROR_MESSAGE);
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
        txtId.setText("");
        txtNombre.setText("");
        txtApellido.setText("");
        txtInicial.setText("");
        spinnerFechaNac.setValue(new Date());
        cmbTutores.setSelectedIndex(-1);
        
        btnGuardar.setVisible(true);
        btnModificar.setVisible(true);
        btnEliminar.setVisible(true);
        
        tablaUsuarios.clearSelection();
        usuarioSeleccionado = null;
    }

    private UsuarioTablaVista buscarUsuarioPorNo(String usuarioNo) {
        if (listaUsuarios == null) return null;
        for (UsuarioTablaVista u : listaUsuarios) {
            if (u.getUsuarioNo().equals(usuarioNo)) return u;
        }
        return null;
    }
}
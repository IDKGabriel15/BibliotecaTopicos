package aplicacion;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import gUILayer.*;

public class App extends JFrame {

    private static final long serialVersionUID = 1L;

    // CONSTANTES PARA EL CARDLAYOUT
    private static final String LIBROS = "LIBROS";
    private static final String USUARIOS = "USUARIOS";
    private static final String PRESTAMOS = "PRESTAMOS";

    private JPanel panelPrincipal;
    private CardLayout cardLayout;
    private JToolBar toolBar;
    private JMenuBar menuBar;
    private GestionLibrosUI panelLibros;
    private GestionUsuariosUI panelUsuarios;
    private GestionPrestamosUI panelPrestamos;
    private Buscable panelActual;
    private JTextField campoBusqueda;
    private boolean buscando = false;

    public App() {
        configurarVentana();
        inicializarComponentes();
        setIconImage(new ImageIcon("assets/book.png").getImage());
        cardLayout.show(panelPrincipal, LIBROS);
    }

    private void configurarVentana() {
        setTitle("Sistema de Control Bibliotecario");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void inicializarComponentes() {
        crearMenu();
        crearToolBar();
        crearPaneles();
    }

    private void crearMenu() {
        menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Perfil");
        JMenuItem itemSalir = new JMenuItem("Cerrar sesión");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);

        JMenu menuGestion = new JMenu("Gestión");
        JMenuItem itemLibros = new JMenuItem("Gestión de Libros");
        JMenuItem itemUsuarios = new JMenuItem("Gestión de Usuarios");
        JMenuItem itemPrestamos = new JMenuItem("Gestión de Préstamos");
        menuGestion.add(itemLibros);
        menuGestion.add(itemUsuarios);
        menuGestion.add(itemPrestamos);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de...");
        itemAcercaDe.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Sistema de Control Bibliotecario v1.0\nDesarrollado por PROYECTO TOPICOS",
                "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));
        menuAyuda.add(itemAcercaDe);

        menuBar.add(menuArchivo);
        menuBar.add(menuGestion);
        menuBar.add(menuAyuda);

        setJMenuBar(menuBar);

        // Acciones del menú
        itemLibros.addActionListener(e -> mostrarPanel(LIBROS));
        itemUsuarios.addActionListener(e -> mostrarPanel(USUARIOS));
        itemPrestamos.addActionListener(e -> mostrarPanel(PRESTAMOS));
    }

    private void crearToolBar() {
        toolBar = new JToolBar("Navegación");
        toolBar.setFloatable(false);

        JButton btnLibros = crearBoton("Libros", "assets/libros.png", LIBROS);
        JButton btnUsuarios = crearBoton("Usuarios", "assets/usuarios.png", USUARIOS);
        JButton btnPrestamos = crearBoton("Prestamos", "assets/prestamos.png", PRESTAMOS);
        JButton btnBuscar = crearBoton("Buscar", "assets/buscar.png", null);

        campoBusqueda = new JTextField(15);
        campoBusqueda.setMaximumSize(campoBusqueda.getPreferredSize());
        campoBusqueda.setVisible(false);
        campoBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (panelActual != null) {
                    panelActual.buscar(campoBusqueda.getText().trim());
                }
            }
        });

        btnBuscar.addActionListener(e -> toggleBusqueda());

        toolBar.add(btnLibros);
        toolBar.add(btnUsuarios);
        toolBar.add(btnPrestamos);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnBuscar);
        toolBar.add(campoBusqueda);

        add(toolBar, BorderLayout.NORTH);
    }

    private JButton crearBoton(String texto, String rutaIcono, String panel) {
        JButton boton = new JButton(texto, new ImageIcon(rutaIcono));
        boton.setToolTipText("Gestión " + texto);
        boton.setBorderPainted(false);
        if (panel != null) {
            boton.addActionListener(e -> mostrarPanel(panel));
        }
        return boton;
    }

    private void crearPaneles() {
        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        panelLibros = new GestionLibrosUI();
        panelUsuarios = new GestionUsuariosUI();
        panelPrestamos = new GestionPrestamosUI();

        panelPrincipal.add(panelLibros, LIBROS);
        panelPrincipal.add(panelUsuarios, USUARIOS);
        panelPrincipal.add(panelPrestamos, PRESTAMOS);

        panelActual = panelLibros;

        add(panelPrincipal, BorderLayout.CENTER);
    }

    private void mostrarPanel(String nombre) {
        switch (nombre) {
            case LIBROS -> panelLibros.actualizarDatos();
            case USUARIOS -> panelUsuarios.actualizarDatos();
            case PRESTAMOS -> panelPrestamos.actualizarDatos();
        }

        panelActual = switch (nombre) {
            case LIBROS -> panelLibros;
            case USUARIOS -> panelUsuarios;
            case PRESTAMOS -> panelPrestamos;
            default -> panelActual;
        };

        cardLayout.show(panelPrincipal, nombre);
    }

    private void toggleBusqueda() {
        buscando = !buscando;
        campoBusqueda.setVisible(buscando);
        if (buscando) campoBusqueda.requestFocusInWindow();
        else campoBusqueda.setText("");

        toolBar.revalidate();
        toolBar.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}

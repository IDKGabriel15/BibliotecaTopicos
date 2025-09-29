package aplicacion;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import gUILayer.Buscable;
import gUILayer.GestionLibrosUI;
import gUILayer.GestionPrestamosUI;
import gUILayer.GestionUsuariosUI;

//CLASE PRINCIPAL DE LA APLICACIÓN. EXTIENDE DE JFRAME PARA SER LA VENTANA PRINCIPAL.
public class App extends JFrame {

    private static final long serialVersionUID = 1L;

    //SE DEFINEN CONSTANTES PARA IDENTIFICAR LOS PANELES EN EL CARDLAYOUT.
    private static final String LIBROS = "LIBROS";
    private static final String USUARIOS = "USUARIOS";
    private static final String PRESTAMOS = "PRESTAMOS";

    //COMPONENTES PRINCIPALES DE LA INTERFAZ.
    private JPanel panelPrincipal; //PANEL QUE CONTENDRÁ LOS DEMÁS PANELES (LIBROS, USUARIOS, PRÉSTAMOS).
    private CardLayout cardLayout; //LAYOUT QUE PERMITE CAMBIAR ENTRE PANELES.
    private JToolBar toolBar; //BARRA DE HERRAMIENTAS SUPERIOR.
    private JMenuBar menuBar; //BARRA DE MENÚ SUPERIOR.
    
    //PANELES DE GESTIÓN ESPECÍFICOS.
    private GestionLibrosUI panelLibros;
    private GestionUsuariosUI panelUsuarios;
    private GestionPrestamosUI panelPrestamos;
    
    //VARIABLES PARA CONTROLAR EL PANEL ACTUAL Y LA BÚSQUEDA.
    private Buscable panelActual; //REFERENCIA AL PANEL VISIBLE ACTUALMENTE PARA LA BÚSQUEDA.
    private JTextField campoBusqueda; //CAMPO DE TEXTO PARA BÚSQUEDAS.
    private boolean buscando = false; //BANDERA PARA SABER SI EL CAMPO DE BÚSQUEDA ESTÁ ACTIVO.

    //CONSTRUCTOR PRINCIPAL DE LA APLICACIÓN.
    public App() {
        configurarVentana();
        inicializarComponentes();
        setIconImage(new ImageIcon("assets/book.png").getImage());
        cardLayout.show(panelPrincipal, LIBROS); //SE MUESTRA EL PANEL DE LIBROS POR DEFECTO.
    }

    //CONFIGURA LAS PROPIEDADES BÁSICAS DE LA VENTANA PRINCIPAL (TÍTULO, TAMAÑO, ETC.).
    private void configurarVentana() {
        setTitle("Sistema de Control Bibliotecario");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); //CENTRA LA VENTANA EN LA PANTALLA.
    }

    //MÉTODO CENTRAL QUE ORQUESTA LA CREACIÓN DE LOS COMPONENTES GRÁFICOS.
    private void inicializarComponentes() {
        crearMenu();
        crearToolBar();
        crearPaneles();
    }

    //CREA Y CONFIGURA LA BARRA DE MENÚ SUPERIOR Y SUS ELEMENTOS.
    private void crearMenu() {
        menuBar = new JMenuBar();

        //MENÚ "PERFIL"
        JMenu menuArchivo = new JMenu("Perfil");
        JMenuItem itemSalir = new JMenuItem("Cerrar sesión");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);

        //MENÚ "GESTIÓN"
        JMenu menuGestion = new JMenu("Gestión");
        JMenuItem itemLibros = new JMenuItem("Gestión de Libros");
        JMenuItem itemUsuarios = new JMenuItem("Gestión de Usuarios");
        JMenuItem itemPrestamos = new JMenuItem("Gestión de Préstamos");
        menuGestion.add(itemLibros);
        menuGestion.add(itemUsuarios);
        menuGestion.add(itemPrestamos);

        //MENÚ "AYUDA"
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

        //ASIGNA LAS ACCIONES A LOS ITEMS DEL MENÚ PARA CAMBIAR DE PANEL.
        itemLibros.addActionListener(e -> mostrarPanel(LIBROS));
        itemUsuarios.addActionListener(e -> mostrarPanel(USUARIOS));
        itemPrestamos.addActionListener(e -> mostrarPanel(PRESTAMOS));
    }

    //CREA Y CONFIGURA LA BARRA DE HERRAMIENTAS CON BOTONES DE ACCESO RÁPIDO.
    private void crearToolBar() {
        toolBar = new JToolBar("Navegación");
        toolBar.setFloatable(false); //LA BARRA NO SE PUEDE MOVER.

        //SE CREAN LOS BOTONES DE LA BARRA DE HERRAMIENTAS.
        JButton btnLibros = crearBoton("Libros", "assets/libros.png", LIBROS);
        JButton btnUsuarios = crearBoton("Usuarios", "assets/usuarios.png", USUARIOS);
        JButton btnPrestamos = crearBoton("Prestamos", "assets/prestamos.png", PRESTAMOS);
        JButton btnBuscar = crearBoton("Buscar", "assets/buscar.png", null);

        //CONFIGURACIÓN DEL CAMPO DE BÚSQUEDA.
        campoBusqueda = new JTextField(15);
        campoBusqueda.setMaximumSize(campoBusqueda.getPreferredSize());
        campoBusqueda.setVisible(false); //INICIALMENTE OCULTO.
        campoBusqueda.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                //CADA VEZ QUE SE SUELTA UNA TECLA, SE LLAMA AL MÉTODO BUSCAR DEL PANEL ACTUAL.
                if (panelActual != null) {
                    panelActual.buscar(campoBusqueda.getText().trim());
                }
            }
        });

        //EL BOTÓN BUSCAR MUESTRA U OCULTA EL CAMPO DE BÚSQUEDA.
        btnBuscar.addActionListener(e -> toggleBusqueda());

        //SE AÑADEN LOS COMPONENTES A LA BARRA DE HERRAMIENTAS.
        toolBar.add(btnLibros);
        toolBar.add(btnUsuarios);
        toolBar.add(btnPrestamos);
        toolBar.add(Box.createHorizontalGlue()); //ESPACIO FLEXIBLE PARA EMPUJAR LO SIGUIENTE A LA DERECHA.
        toolBar.add(btnBuscar);
        toolBar.add(campoBusqueda);

        add(toolBar, BorderLayout.NORTH);
    }

    //MÉTODO UTILITARIO PARA CREAR BOTONES DE LA TOOLBAR DE FORMA CONSISTENTE.
    private JButton crearBoton(String texto, String rutaIcono, String panel) {
        JButton boton = new JButton(texto, new ImageIcon(rutaIcono));
        boton.setToolTipText("Gestión " + texto);
        boton.setBorderPainted(false); //QUITAR BORDE PARA UN ASPECTO MÁS LIMPIO.
        if (panel != null) {
            boton.addActionListener(e -> mostrarPanel(panel));
        }
        return boton;
    }

    //INICIALIZA EL PANEL PRINCIPAL CON CARDLAYOUT Y AÑADE LOS PANELES DE GESTIÓN.
    private void crearPaneles() {
        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        //SE CREAN LAS INSTANCIAS DE LOS PANELES DE LA CAPA GUILAYER.
        panelLibros = new GestionLibrosUI();
        panelUsuarios = new GestionUsuariosUI();
        panelPrestamos = new GestionPrestamosUI();

        //SE AÑADEN LOS PANELES AL CARDLAYOUT CON SU RESPECTIVA CONSTANTE IDENTIFICADORA.
        panelPrincipal.add(panelLibros, LIBROS);
        panelPrincipal.add(panelUsuarios, USUARIOS);
        panelPrincipal.add(panelPrestamos, PRESTAMOS);

        //SE ESTABLECE EL PANEL DE LIBROS COMO EL PANEL ACTIVO INICIAL.
        panelActual = panelLibros;

        add(panelPrincipal, BorderLayout.CENTER);
    }

    //MÉTODO CLAVE PARA CAMBIAR EL PANEL VISIBLE EN EL CARDLAYOUT.
    private void mostrarPanel(String nombre) {
        //ANTES DE MOSTRAR UN PANEL, SE ACTUALIZAN SUS DATOS PARA REFLEJAR CUALQUIER CAMBIO.
        switch (nombre) {
            case LIBROS -> panelLibros.actualizarDatos();
            case USUARIOS -> panelUsuarios.actualizarDatos();
            case PRESTAMOS -> panelPrestamos.actualizarDatos();
        }

        //SE ACTUALIZA LA REFERENCIA AL PANEL ACTUAL PARA QUE LA BÚSQUEDA FUNCIONE CORRECTAMENTE.
        panelActual = switch (nombre) {
            case LIBROS -> panelLibros;
            case USUARIOS -> panelUsuarios;
            case PRESTAMOS -> panelPrestamos;
            default -> panelActual;
        };

        //SE MUESTRA EL PANEL SOLICITADO.
        cardLayout.show(panelPrincipal, nombre);
    }

    //MUESTRA U OCULTA EL CAMPO DE BÚSQUEDA EN LA BARRA DE HERRAMIENTAS.
    private void toggleBusqueda() {
        buscando = !buscando;
        campoBusqueda.setVisible(buscando);
        if (buscando) {
            campoBusqueda.requestFocusInWindow(); //PONE EL FOCO EN EL CAMPO DE BÚSQUEDA.
        } else {
            campoBusqueda.setText(""); //LIMPIA EL CAMPO AL OCULTARLO.
        }

        toolBar.revalidate();
        toolBar.repaint();
    }

    //PUNTO DE ENTRADA PRINCIPAL DE LA APLICACIÓN.
    public static void main(String[] args) {
        //SE ASEGURA QUE LA INTERFAZ GRÁFICA SE CREE Y ACTUALICE EN EL HILO DE DESPACHO DE EVENTOS (EDT).
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
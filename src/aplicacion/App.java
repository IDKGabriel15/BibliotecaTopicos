package aplicacion;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;
import gUILayer.*;

public class App extends JFrame {
	private static final long serialVersionUID = 1L;
	private JPanel panelPrincipal;
    private CardLayout cardLayout;
    private JToolBar toolBar;
    private JMenuBar menuBar;
    private GestionLibrosUI panelLibros;
    private GestionUsuariosUI panelUsuarios;
    private GestionPrestamosUI panelPrestamos;
    private Buscable panelActivo;
    
    
    public App() {
        setTitle("Sistema de Control Bibliotecario");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        menuBar = new JMenuBar();

        JMenu menuArchivo = new JMenu("Archivo");
        JMenuItem itemSalir = new JMenuItem("Salir");
        menuArchivo.add(itemSalir);

        JMenu menuGestion = new JMenu("Gestión");
        JMenuItem itemGestionLibros = new JMenuItem("Gestión de Libros");
        JMenuItem itemGestionUsuarios = new JMenuItem("Gestión de Usuarios");
        JMenuItem itemGestionPrestamos = new JMenuItem("Gestión de Préstamos");
        menuGestion.add(itemGestionLibros);
        menuGestion.add(itemGestionUsuarios);
        menuGestion.add(itemGestionPrestamos);

        JMenu menuAyuda = new JMenu("Ayuda");
        JMenuItem itemAcercaDe = new JMenuItem("Acerca de...");
        menuAyuda.add(itemAcercaDe);

        menuBar.add(menuArchivo);
        menuBar.add(menuGestion);
        menuBar.add(menuAyuda);
        setJMenuBar(menuBar);

        toolBar = new JToolBar("Navegación");
        toolBar.setFloatable(false);
        add(toolBar, BorderLayout.NORTH);

        ImageIcon iconoLibros = new ImageIcon("assets/libros.png");
        ImageIcon iconoUsuarios = new ImageIcon("assets/usuarios.png");
        ImageIcon iconoPrestamos = new ImageIcon("assets/prestamos.png");
        ImageIcon iconoBuscar = new ImageIcon("assets/buscar.png");

        JButton btnLibros = new JButton("Libros", iconoLibros);
        btnLibros.setToolTipText("Gestión Libros");
        btnLibros.setBorderPainted(false);
        JButton btnUsuarios = new JButton("Usuarios",iconoUsuarios);
        btnUsuarios.setToolTipText("Gestión Usuarios");
        btnUsuarios.setBorderPainted(false);
        JButton btnPrestamos = new JButton("Prestamos",iconoPrestamos);
        btnPrestamos.setToolTipText("Gestión Préstamos");
        btnPrestamos.setBorderPainted(false);
        
        JButton btnBuscar = new JButton("Buscar", iconoBuscar);
        btnBuscar.setToolTipText("Buscar");
        btnBuscar.setBorderPainted(false);
        
        //CAMPO DE TEXTO PARA BUSCAR
        JTextField txtBuscar = new JTextField(15);
        txtBuscar.setMaximumSize(txtBuscar.getPreferredSize()); 
        txtBuscar.setVisible(false);
        
        
        toolBar.add(btnLibros);
        toolBar.add(btnUsuarios);
        toolBar.add(btnPrestamos);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnBuscar);
        toolBar.add(txtBuscar);

        final boolean[] buscando = {false};

        
        cardLayout = new CardLayout();
        panelPrincipal = new JPanel(cardLayout);

        panelLibros = new GestionLibrosUI();
        panelUsuarios = new GestionUsuariosUI();
        panelPrestamos = new GestionPrestamosUI();

        panelActivo = panelLibros;
        		
        panelPrincipal.add(panelLibros, "LIBROS");
        panelPrincipal.add(panelUsuarios, "USUARIOS");
        panelPrincipal.add(panelPrestamos, "PRESTAMOS");

        add(panelPrincipal, BorderLayout.CENTER);

        btnLibros.addActionListener(e-> {
            panelLibros.actualizarDatos();
            cardLayout.show(panelPrincipal, "LIBROS");
            panelActivo = panelLibros;
        });

        btnUsuarios.addActionListener(e-> {
            panelUsuarios.actualizarDatos();
            cardLayout.show(panelPrincipal, "USUARIOS");
            panelActivo = panelUsuarios;
        });

        btnPrestamos.addActionListener(e-> {
            panelPrestamos.actualizarDatos();
            cardLayout.show(panelPrincipal, "PRESTAMOS");
            panelActivo = panelPrestamos;
        });

        btnBuscar.addActionListener(e -> {
        	buscando[0] = !buscando[0]; 
            txtBuscar.setVisible(buscando[0]);
            toolBar.revalidate(); 
            toolBar.repaint();
            
            if (buscando[0]) txtBuscar.requestFocusInWindow(); 
            else txtBuscar.setText(""); 
            
        });
        
        txtBuscar.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                if (panelActivo != null) {
                    String criterio = txtBuscar.getText().trim();
                    panelActivo.buscar(criterio);
                }
            }
        });

        
        
        itemGestionLibros.addActionListener(e-> cardLayout.show(panelPrincipal, "LIBROS"));
        itemGestionUsuarios.addActionListener(e -> cardLayout.show(panelPrincipal, "USUARIOS"));
        itemGestionPrestamos.addActionListener(e-> cardLayout.show(panelPrincipal, "PRESTAMOS"));

        itemSalir.addActionListener(e->System.exit(0));
        itemAcercaDe.addActionListener(e-> JOptionPane.showMessageDialog(this,
                "Sistema de Control Bibliotecario v1.0\nDesarrollado por PROYECTO TOPICOS", "Acerca de",
                JOptionPane.INFORMATION_MESSAGE));

        cardLayout.show(panelPrincipal, "LIBROS");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.setVisible(true);
        });
    }
}
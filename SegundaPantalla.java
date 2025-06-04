import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;


public class SegundaPantalla extends JFrame {
    private String url, usuario, contrasena;
    private JTable tablaResultados;
    private JTextArea areaConsulta;
    private JButton btnEjecutar;
    private JTree arbolTablas;

    public SegundaPantalla(String url, String usuario, String contrasena) {
        this.url = url;
        this.usuario = usuario;
        this.contrasena = contrasena;
        initComponents();
        cargarTablas();
    }

    private void initComponents() {
        setTitle("Gestor de Base de Datos");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        getContentPane().add(panelPrincipal);

        // Panel Izquierdo - Árbol de tablas
        arbolTablas = new JTree();
        JScrollPane scrollArbol = new JScrollPane(arbolTablas);
        scrollArbol.setPreferredSize(new Dimension(250, 500));
        panelPrincipal.add(scrollArbol, BorderLayout.WEST);

        // Panel Central - Área de consulta y botón
        JPanel panelCentro = new JPanel(new BorderLayout(5, 5));

        areaConsulta = new JTextArea(5, 40);
        JScrollPane scrollConsulta = new JScrollPane(areaConsulta);
        areaConsulta.setFont(new Font("Monospaced", Font.PLAIN, 14));
        panelCentro.add(scrollConsulta, BorderLayout.NORTH);

        btnEjecutar = new JButton("Ejecutar Consulta");
        btnEjecutar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                ejecutarConsulta();
            }
        });
        panelCentro.add(btnEjecutar, BorderLayout.CENTER);

        // Panel Inferior - Resultados
        tablaResultados = new JTable();
        JScrollPane scrollTabla = new JScrollPane(tablaResultados);
        panelCentro.add(scrollTabla, BorderLayout.SOUTH);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
    }

private void cargarTablas() {
    try (Connection con = DriverManager.getConnection(url, usuario, contrasena)) {
        DatabaseMetaData metaData = con.getMetaData();
        ResultSet tablas = metaData.getTables(null, null, "%", new String[]{"TABLE"});

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("Base de Datos");

        while (tablas.next()) {
            String nombreTabla = tablas.getString("TABLE_NAME");
            DefaultMutableTreeNode nodoTabla = new DefaultMutableTreeNode(nombreTabla);

            ResultSet columnas = metaData.getColumns(null, null, nombreTabla, null);
            while (columnas.next()) {
                String nombreColumna = columnas.getString("COLUMN_NAME");
                nodoTabla.add(new DefaultMutableTreeNode(nombreColumna));
            }

            raiz.add(nodoTabla);
        }

        DefaultTreeModel modelo = new DefaultTreeModel(raiz);
        arbolTablas.setModel(modelo);

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al cargar tablas: " + e.getMessage());
    }
}


    private void ejecutarConsulta() {
    String consulta = areaConsulta.getText().trim();

    try (Connection con = DriverManager.getConnection(url, usuario, contrasena);
         Statement stmt = con.createStatement()) {

        if (consulta.toLowerCase().startsWith("select")) {
            ResultSet rs = stmt.executeQuery(consulta);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnas = metaData.getColumnCount();

            DefaultTableModel modelo = new DefaultTableModel();
            for (int i = 1; i <= columnas; i++) {
                modelo.addColumn(metaData.getColumnName(i));
            }

            while (rs.next()) {
                Object[] fila = new Object[columnas];
                for (int i = 0; i < columnas; i++) {
                    fila[i] = rs.getObject(i + 1);
                }
                modelo.addRow(fila);
            }

            tablaResultados.setModel(modelo);

        } else {
            int filasAfectadas = stmt.executeUpdate(consulta);
            JOptionPane.showMessageDialog(this, "Consulta ejecutada correctamente. Filas afectadas: " + filasAfectadas);
        }

    } catch (SQLException e) {
        JOptionPane.showMessageDialog(this, "Error al ejecutar consulta: " + e.getMessage());
    }
}


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String url = "jdbc:mysql://localhost:3306/tu_basedatos";
            String usuario = "root";
            String contrasena = "tu_contrasena";

            new SegundaPantalla(url, usuario, contrasena).setVisible(true);
        });
    }
}




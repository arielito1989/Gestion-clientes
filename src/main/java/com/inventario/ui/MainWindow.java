package com.inventario.ui;

import com.inventario.models.Cliente;
import com.inventario.utils.ExcelExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private List<Cliente> clientes;
    private DefaultListModel<String> clienteListModel = new DefaultListModel<>();
    private JList<String> listaClientes = new JList<>(clienteListModel);

    private static final String EXCEL_FILE_PATH = "clientes.xlsx";

    public MainWindow() {
        setTitle("Inventario de Clientes - Tecnología");
        setSize(850, 500); // ANCHO AJUSTADO AQUÍ: De 700 a 850 para más espacio horizontal
        setMinimumSize(new Dimension(800, 450)); // Tamaño mínimo ajustado también
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        cargarClientesDesdeExcel();

        // --- Panel de Botones (Superior) ---
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JButton btnAgregar = new JButton("Agregar Cliente");
        JButton btnPagarCuota = new JButton("Registrar Pago de Cuota");
        JButton btnActualizar = new JButton("Actualizar Lista");
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnEliminarCliente = new JButton("Eliminar Cliente");

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);
        Color primaryColor = new Color(30, 144, 255); // Azul
        Color secondaryColor = new Color(60, 179, 113); // Verde
        Color exportColor = new Color(0, 128, 0); // Verde oscuro
        Color deleteColor = new Color(220, 20, 60); // Rojo carmesí

        styleButton(btnAgregar, primaryColor, buttonFont);
        styleButton(btnPagarCuota, secondaryColor, buttonFont);
        styleButton(btnActualizar, primaryColor, buttonFont);
        styleButton(btnExportarExcel, exportColor, buttonFont);
        styleButton(btnEliminarCliente, deleteColor, buttonFont);

        panelBotones.add(btnAgregar);
        panelBotones.add(btnPagarCuota);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnEliminarCliente);

        add(panelBotones, BorderLayout.NORTH);

        // --- Lista de Clientes (Centro) ---
        listaClientes.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listaClientes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(listaClientes);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        // --- Listeners de Botones ---
        btnAgregar.addActionListener(e -> mostrarFormularioAgregarCliente());
        btnPagarCuota.addActionListener(e -> mostrarFormularioPagarCuota());
        btnActualizar.addActionListener(e -> actualizarLista());
        btnExportarExcel.addActionListener(e -> {
            ExcelExporter.exportarClientes(clientes, EXCEL_FILE_PATH);
            JOptionPane.showMessageDialog(this, "Clientes exportados a: " + EXCEL_FILE_PATH, "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
        });
        btnEliminarCliente.addActionListener(e -> eliminarClienteSeleccionado());

        // --- Listener para guardar datos al cerrar la ventana ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                guardarClientesAExcel();
            }
        });

        actualizarLista();
    }

    /**
     * Aplica estilos comunes a un botón.
     * @param button El botón a estilizar.
     * @param bgColor El color de fondo.
     * @param font La fuente del texto.
     */
    private void styleButton(JButton button, Color bgColor, Font font) {
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Muestra el formulario para agregar un nuevo cliente, incluyendo campos para cuotas.
     */
    private void mostrarFormularioAgregarCliente() {
        JTextField nombre = new JTextField(20);
        JTextField dni = new JTextField(20);
        JTextField tipoCuota = new JTextField(20);
        JTextField total = new JTextField(20);
        JTextField totalCuotas = new JTextField(10);
        JTextField valorCuota = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombre);
        panel.add(new JLabel("DNI:"));
        panel.add(dni);
        panel.add(new JLabel("Tipo de Cuota (mensual/quincenal):"));
        panel.add(tipoCuota);
        panel.add(new JLabel("Total del producto:"));
        panel.add(total);
        panel.add(new JLabel("Total de Cuotas:"));
        panel.add(totalCuotas);
        panel.add(new JLabel("Valor por Cuota:"));
        panel.add(valorCuota);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                if (nombre.getText().trim().isEmpty() || dni.getText().trim().isEmpty() ||
                        tipoCuota.getText().trim().isEmpty() || total.getText().trim().isEmpty() ||
                        totalCuotas.getText().trim().isEmpty() || valorCuota.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double totalProducto = Double.parseDouble(total.getText());
                int numTotalCuotas = Integer.parseInt(totalCuotas.getText());
                double valCuota = Double.parseDouble(valorCuota.getText());

                if (totalProducto <= 0 || numTotalCuotas <= 0 || valCuota <= 0) {
                    JOptionPane.showMessageDialog(this, "Los valores numéricos deben ser positivos.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (clientes.stream().anyMatch(c -> c.getDni().equals(dni.getText().trim()))) {
                    JOptionPane.showMessageDialog(this, "Ya existe un cliente con ese DNI.", "DNI Duplicado", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                Cliente c = new Cliente(
                        nombre.getText().trim(),
                        dni.getText().trim(),
                        tipoCuota.getText().trim(),
                        totalProducto,
                        numTotalCuotas,
                        valCuota
                );
                clientes.add(c);
                actualizarLista();
                guardarClientesAExcel();
                JOptionPane.showMessageDialog(this, "Cliente agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Asegúrate de que 'Total del producto', 'Total de Cuotas' y 'Valor por Cuota' sean números válidos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Muestra el formulario para registrar un pago de cuota y descontarla.
     */
    private void mostrarFormularioPagarCuota() {
        String dni = JOptionPane.showInputDialog(this, "Ingrese el DNI del cliente para registrar el pago:", "Registrar Pago", JOptionPane.QUESTION_MESSAGE);
        if (dni == null || dni.trim().isEmpty()) {
            return;
        }

        Cliente cliente = clientes.stream()
                .filter(c -> c.getDni().equals(dni.trim()))
                .findFirst()
                .orElse(null);

        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado con el DNI: " + dni, "Cliente No Encontrado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String infoCliente = String.format("Cliente: %s (DNI: %s)\nDeuda restante: $%.2f\nCuotas pagadas: %d/%d (Valor cuota: $%.2f)",
                cliente.getNombre(), cliente.getDni(), cliente.calcularDeudaRestante(),
                cliente.getCuotasPagadasCount(), cliente.getTotalCuotas(), cliente.getValorCuota());

        String montoStr = JOptionPane.showInputDialog(this, infoCliente + "\n\nIngrese el monto de la cuota a pagar:", "Monto de Cuota", JOptionPane.QUESTION_MESSAGE);
        if (montoStr == null || montoStr.trim().isEmpty()) {
            return;
        }

        try {
            double monto = Double.parseDouble(montoStr);
            if (monto <= 0) {
                JOptionPane.showMessageDialog(this, "El monto de la cuota debe ser un número positivo.", "Monto Inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (monto > cliente.calcularDeudaRestante() && cliente.calcularDeudaRestante() > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        String.format("El monto ingresado ($%.2f) es mayor que la deuda restante ($%.2f). ¿Desea registrar este pago de todos modos?", monto, cliente.calcularDeudaRestante()),
                        "Advertencia de Monto", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            cliente.pagarCuota(monto);
            cliente.incrementarCuotaPagada();

            actualizarLista();
            guardarClientesAExcel();
            JOptionPane.showMessageDialog(this, "Pago y cuota registrados exitosamente para " + cliente.getNombre() + ".", "Pago Registrado", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto de cuota inválido. Por favor, ingrese un número.", "Error de Entrada", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Elimina el cliente seleccionado de la lista.
     */
    private void eliminarClienteSeleccionado() {
        int selectedIndex = listaClientes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente de la lista para eliminar.", "Ningún Cliente Seleccionado", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar al cliente seleccionado?",
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            clientes.remove(selectedIndex);
            actualizarLista();
            guardarClientesAExcel();
            JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void actualizarLista() {
        clienteListModel.clear();
        if (clientes != null) {
            for (Cliente c : clientes) {
                clienteListModel.addElement(c.toString());
            }
        }
    }

    private void cargarClientesDesdeExcel() {
        clientes = ExcelExporter.importarClientes(EXCEL_FILE_PATH);
        if (clientes == null) {
            clientes = new ArrayList<>();
        }
    }

    private void guardarClientesAExcel() {
        ExcelExporter.exportarClientes(clientes, EXCEL_FILE_PATH);
    }
}

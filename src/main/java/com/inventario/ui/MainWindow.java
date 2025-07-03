package com.inventario.ui;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import com.inventario.utils.ExcelExporter;
import com.inventario.commands.Command;
import com.inventario.commands.AgregarClienteCommand;
import com.inventario.commands.RegistrarPagoCommand;
import com.inventario.commands.EliminarClienteCommand;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainWindow extends JFrame {
    private List<Cliente> clientes = new ArrayList<>();
    private DefaultListModel<String> clienteListModel = new DefaultListModel<>();
    private JList<String> listaClientes = new JList<>(clienteListModel);

    private Stack<Command> commandHistory = new Stack<>();
    private JButton btnDeshacer;
    private JButton btnDetalles;

    public MainWindow() {
        setTitle("Inventario de Clientes - Tecnología");
        setSize(850, 500);
        setMinimumSize(new Dimension(900, 500));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        loadClientsFromExcel();

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JButton btnAgregar = new JButton("Agregar Cliente");
        JButton btnPagarCuota = new JButton("Registrar Pago");
        JButton btnActualizar = new JButton("Actualizar Lista");
        JButton btnExportarExcel = new JButton("Exportar a Excel");
        JButton btnEliminarCliente = new JButton("Eliminar Cliente");
        btnDeshacer = new JButton("Deshacer");
        btnDetalles = new JButton("Detalles Cliente");

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);
        Color primaryColor = new Color(30, 144, 255);
        Color secondaryColor = new Color(60, 179, 113);
        Color exportColor = new Color(0, 128, 0);
        Color deleteColor = new Color(220, 20, 60);
        Color undoColor = new Color(255, 165, 0);
        Color detailColor = new Color(70, 130, 180);

        styleButton(btnAgregar, primaryColor, buttonFont);
        styleButton(btnPagarCuota, secondaryColor, buttonFont);
        styleButton(btnActualizar, primaryColor, buttonFont);
        styleButton(btnExportarExcel, exportColor, buttonFont);
        styleButton(btnEliminarCliente, deleteColor, buttonFont);
        styleButton(btnDeshacer, undoColor, buttonFont);
        styleButton(btnDetalles, detailColor, buttonFont);

        panelBotones.add(btnAgregar);
        panelBotones.add(btnPagarCuota);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnExportarExcel);
        panelBotones.add(btnEliminarCliente);
        panelBotones.add(btnDeshacer);
        panelBotones.add(btnDetalles);

        add(panelBotones, BorderLayout.NORTH);

        listaClientes.setFont(new Font("Monospaced", Font.PLAIN, 12));
        listaClientes.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        JScrollPane scrollPane = new JScrollPane(listaClientes);
        scrollPane.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1)
        ));
        add(scrollPane, BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> showAddClientForm());
        btnPagarCuota.addActionListener(e -> showRegisterPaymentForm());
        btnActualizar.addActionListener(e -> updateClientList());
        btnExportarExcel.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar archivo Excel");
            fileChooser.setSelectedFile(new java.io.File("clientes.xlsx"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                java.io.File fileToSave = fileChooser.getSelectedFile();
                ExcelExporter.exportarClientes(clientes, fileToSave.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Clientes exportados a: " + fileToSave.getAbsolutePath(),
                        "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        btnEliminarCliente.addActionListener(e -> deleteSelectedClient());
        btnDeshacer.addActionListener(e -> undoLastAction());
        btnDetalles.addActionListener(e -> showClientDetails());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveClientsToExcel();
            }
        });

        updateClientList();
        updateUndoButtonState();
    }

    private void styleButton(JButton button, Color bgColor, Font font) {
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(bgColor.darker(), 1, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void updateUndoButtonState() {
        btnDeshacer.setEnabled(!commandHistory.isEmpty());
    }

    private void updateClientList() {
        clienteListModel.clear();
        for (Cliente c : clientes) {
            clienteListModel.addElement(c.toString());
        }
    }

    private void loadClientsFromExcel() {
        List<Cliente> cargados = ExcelExporter.importarClientes("clientes.xlsx");
        if (cargados != null) {
            clientes = cargados;
        }
    }

    private void saveClientsToExcel() {
        ExcelExporter.exportarClientes(clientes, "clientes.xlsx");
    }

    private void undoLastAction() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();
            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
        } else {
            JOptionPane.showMessageDialog(this, "No hay acciones para deshacer.", "Deshacer", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedClient() {
        int selectedIndex = listaClientes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente client = clientes.get(selectedIndex);
        int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar a " + client.getNombre() + "?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            EliminarClienteCommand command = new EliminarClienteCommand(clientes, client, selectedIndex);
            command.execute();
            commandHistory.push(command);
            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
        }
    }

    private void showClientDetails() {
        int index = listaClientes.getSelectedIndex();
        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente de la lista.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Cliente client = clientes.get(index);
        JOptionPane.showMessageDialog(this, client.toString(), "Detalles del Cliente", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddClientForm() {
        JTextField nameField = new JTextField();
        JTextField dniField = new JTextField();
        JComboBox<String> tipoCuotaBox = new JComboBox<>(new String[]{"mensual", "quincenal"});
        JTextField totalProductoField = new JTextField();
        JTextField totalCuotasField = new JTextField();
        JTextField valorCuotaField = new JTextField();
        valorCuotaField.setEditable(false);

        DocumentListener calcListener = new DocumentListener() {
            void calcular() {
                try {
                    double total = Double.parseDouble(totalProductoField.getText().replace(".", "").replace(",", "."));
                    int cuotas = Integer.parseInt(totalCuotasField.getText().replace(".", ""));
                    if (cuotas > 0) {
                        valorCuotaField.setText(String.format("%.2f", total / cuotas));
                    } else {
                        valorCuotaField.setText("0.00");
                    }
                } catch (NumberFormatException e) {
                    valorCuotaField.setText("0.00");
                }
            }
            public void insertUpdate(DocumentEvent e) { calcular(); }
            public void removeUpdate(DocumentEvent e) { calcular(); }
            public void changedUpdate(DocumentEvent e) { calcular(); }
        };
        totalProductoField.getDocument().addDocumentListener(calcListener);
        totalCuotasField.getDocument().addDocumentListener(calcListener);

        JPanel panel = new JPanel(new GridLayout(0, 2));
        panel.add(new JLabel("Nombre:")); panel.add(nameField);
        panel.add(new JLabel("DNI:")); panel.add(dniField);
        panel.add(new JLabel("Tipo Cuota:")); panel.add(tipoCuotaBox);
        panel.add(new JLabel("Total Producto:")); panel.add(totalProductoField);
        panel.add(new JLabel("Total Cuotas:")); panel.add(totalCuotasField);
        panel.add(new JLabel("Valor Cuota:")); panel.add(valorCuotaField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Agregar Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String nombre = nameField.getText().trim();
                String dni = dniField.getText().trim();
                String tipo = (String) tipoCuotaBox.getSelectedItem();
                double total = Double.parseDouble(totalProductoField.getText().replace(".", "").replace(",", "."));
                int cuotas = Integer.parseInt(totalCuotasField.getText().replace(".", ""));
                double valor = Double.parseDouble(valorCuotaField.getText().replace(".", "").replace(",", "."));

                Cliente nuevo = new Cliente(nombre, dni, tipo, total, cuotas, valor);
                AgregarClienteCommand command = new AgregarClienteCommand(clientes, nuevo);
                command.execute();
                commandHistory.push(command);
                updateClientList();
                saveClientsToExcel();
                updateUndoButtonState();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al ingresar datos: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showRegisterPaymentForm() {
        String dni = JOptionPane.showInputDialog(this, "Ingrese el DNI del cliente:");
        if (dni == null || dni.isEmpty()) return;
        Cliente cliente = clientes.stream().filter(c -> c.getDni().equals(dni)).findFirst().orElse(null);
        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        String montoStr = JOptionPane.showInputDialog(this, "Monto a pagar:");
        if (montoStr == null || montoStr.isEmpty()) return;
        try {
            double monto = Double.parseDouble(montoStr.replace(".", "").replace(",", "."));
            RegistrarPagoCommand command = new RegistrarPagoCommand(cliente, monto);
            command.execute();
            commandHistory.push(command);
            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Monto inválido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

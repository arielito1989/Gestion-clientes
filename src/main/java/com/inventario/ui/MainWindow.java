package com.inventario.ui;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import com.inventario.utils.ExcelExporter;
import com.inventario.commands.Command;
import com.inventario.commands.AgregarClienteCommand;
import com.inventario.commands.RegistrarPagoCommand;
import com.inventario.commands.EliminarClienteCommand;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MainWindow extends JFrame {
    private List<Cliente> clientes; // Usar 'clientes' como nombre de la lista de instancia
    private DefaultListModel<String> clienteListModel = new DefaultListModel<>();
    private JList<String> listaClientes = new JList<>(clienteListModel);


    private void updateUndoButtonState() {
    }

    private Stack<Command> commandHistory = new Stack<>();
    private JButton btnDeshacer;
    private JButton btnDetalles; // Nuevo botón de detalles

    private static final String EXCEL_FILE_PATH = "clientes.xlsx";

    public MainWindow() {
        setTitle("Inventario de Clientes - Tecnología"); // Título de la ventana
        setSize(850, 500);
        setMinimumSize(new Dimension(800, 450));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        loadClientsFromExcel();

        // --- Panel de Botones Superior ---
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));

        JButton btnAgregar = new JButton("Agregar Cliente"); // Traducido
        JButton btnPagarCuota = new JButton("Registrar Pago"); // Traducido
        JButton btnActualizar = new JButton("Actualizar Lista"); // Traducido
        JButton btnExportarExcel = new JButton("Exportar a Excel"); // Traducido
        JButton btnEliminarCliente = new JButton("Eliminar Cliente"); // Traducido
        btnDeshacer = new JButton("Deshacer"); // Traducido
        btnDetalles = new JButton("Detalles Cliente"); // Traducido

        Font buttonFont = new Font("SansSerif", Font.BOLD, 14);
        Color primaryColor = new Color(30, 144, 255); // Azul
        Color secondaryColor = new Color(60, 179, 113); // Verde
        Color exportColor = new Color(0, 128, 0); // Verde oscuro
        Color deleteColor = new Color(220, 20, 60); // Rojo carmesí
        Color undoColor = new Color(255, 165, 0); // Naranja
        Color detailColor = new Color(70, 130, 180); // Azul acero para detalles

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
        btnAgregar.addActionListener(e -> showAddClientForm());
        btnPagarCuota.addActionListener(e -> showRegisterPaymentForm());
        btnActualizar.addActionListener(e -> updateClientList());
        btnExportarExcel.addActionListener(e -> {
            ExcelExporter.exportarClientes(clientes, EXCEL_FILE_PATH);
            JOptionPane.showMessageDialog(this, "Clientes exportados a: " + EXCEL_FILE_PATH, "Exportación Exitosa", JOptionPane.INFORMATION_MESSAGE); // Traducido
        });
        btnEliminarCliente.addActionListener(e -> deleteSelectedClient());
        btnDeshacer.addActionListener(e -> undoLastAction());
        btnDetalles.addActionListener(e -> showClientDetails());

        // --- Listener para guardar datos al cerrar la ventana ---
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveClientsToExcel();
            }
        });

        updateClientList();
        updateUndoButtonState(); // Esta llamada es correcta y el método está definido.
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
    private void showAddClientForm() {
        JTextField nameField = new JTextField(20);
        JTextField dniField = new JTextField(20);
        JComboBox<String> installmentTypeComboBox = new JComboBox<>(new String[]{"mensual", "quincenal"}); // Traducido
        JTextField totalProductField = new JTextField(20);
        JTextField totalInstallmentsField = new JTextField(10);
        JTextField installmentValueField = new JTextField(10);

        totalProductField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> calculateInstallmentValue(totalProductField, totalInstallmentsField, installmentValueField)));
        totalInstallmentsField.getDocument().addDocumentListener(new SimpleDocumentListener(() -> calculateInstallmentValue(totalProductField, totalInstallmentsField, installmentValueField)));

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Nombre:")); // Traducido
        panel.add(nameField);
        panel.add(new JLabel("DNI:")); // Traducido
        panel.add(dniField);
        panel.add(new JLabel("Tipo de Cuota:")); // Traducido
        panel.add(installmentTypeComboBox);
        panel.add(new JLabel("Total del Producto:")); // Traducido
        panel.add(totalProductField);
        panel.add(new JLabel("Total de Cuotas:")); // Traducido
        panel.add(totalInstallmentsField);
        panel.add(new JLabel("Valor por Cuota (calculado):")); // Traducido
        panel.add(installmentValueField);
        installmentValueField.setEditable(false);

        int option = JOptionPane.showConfirmDialog(this, panel, "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); // Traducido
        if (option == JOptionPane.OK_OPTION) {
            try {
                if (nameField.getText().trim().isEmpty() || dniField.getText().trim().isEmpty() ||
                        totalProductField.getText().trim().isEmpty() || totalInstallmentsField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE); // Traducido
                    return;
                }

                double totalProduct = Double.parseDouble(totalProductField.getText());
                int numTotalInstallments = Integer.parseInt(totalInstallmentsField.getText());

                double installmentValue = 0.0;
                if (numTotalInstallments > 0) {
                    installmentValue = totalProduct / numTotalInstallments;
                } else {
                    JOptionPane.showMessageDialog(this, "El total de cuotas debe ser mayor que cero.", "Error de Validación", JOptionPane.ERROR_MESSAGE); // Traducido
                    return;
                }

                if (totalProduct <= 0 || installmentValue <= 0) {
                    JOptionPane.showMessageDialog(this, "Los valores numéricos deben ser positivos.", "Error de Validación", JOptionPane.ERROR_MESSAGE); // Traducido
                    return;
                }

                if (clientes.stream().anyMatch(c -> c.getDni().equals(dniField.getText().trim()))) {
                    JOptionPane.showMessageDialog(this, "Ya existe un cliente con este DNI.", "DNI Duplicado", JOptionPane.WARNING_MESSAGE); // Traducido
                    return;
                }

                Cliente client = new Cliente(
                        nameField.getText().trim(),
                        dniField.getText().trim(),
                        (String) installmentTypeComboBox.getSelectedItem(),
                        totalProduct,
                        numTotalInstallments,
                        installmentValue
                );

                AgregarClienteCommand command = new AgregarClienteCommand(this.clientes, client);
                command.execute();
                commandHistory.push(command);

                updateClientList();
                saveClientsToExcel();
                updateUndoButtonState();
                JOptionPane.showMessageDialog(this, "Cliente agregado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE); // Traducido
            }
            catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Asegúrese de que 'Total del Producto' y 'Total de Cuotas' sean números válidos.", "Error de Entrada", JOptionPane.ERROR_MESSAGE); // Traducido
            }
        }
    }

    private void calculateInstallmentValue(JTextField totalField, JTextField totalInstallmentsField, JTextField installmentValueField) {
        try {
            double total = Double.parseDouble(totalField.getText());
            int totalInstallments = Integer.parseInt(totalInstallmentsField.getText());
            if (totalInstallments > 0) {
                double value = total / totalInstallments;
                installmentValueField.setText(String.format("%.2f", value));
            } else {
                installmentValueField.setText("0.00");
            }
        } catch (NumberFormatException e) {
            installmentValueField.setText("0.00");
        }
    }

    /**
     * Muestra el formulario para registrar un pago de cuota.
     */
    private void showRegisterPaymentForm() {
        String dni = JOptionPane.showInputDialog(this, "Ingrese el DNI del cliente para registrar el pago:", "Registrar Pago", JOptionPane.QUESTION_MESSAGE); // Traducido
        if (dni == null || dni.trim().isEmpty()) {
            return;
        }

        Cliente client = clientes.stream()
                .filter(c -> c.getDni().equals(dni.trim()))
                .findFirst()
                .orElse(null);

        if (client == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado con DNI: " + dni, "Cliente No Encontrado", JOptionPane.WARNING_MESSAGE); // Traducido
            return;
        }

        StringBuilder installmentsInfo = new StringBuilder();
        installmentsInfo.append(String.format("Cliente: %s (DNI: %s)\nDeuda restante total: $%.2f\n\n", // Traducido
                client.getNombre(), client.getDni(), client.calcularDeudaRestante()));

        installmentsInfo.append("Detalle de Cuotas:\n"); // Traducido
        if (client.getCuotas().isEmpty()) {
            installmentsInfo.append("  No hay cuotas registradas para este cliente.\n"); // Traducido
        } else {
            for (Cuota c : client.getCuotas()) {
                String installmentTypeStr = c.isFaltante() ? "Faltante" : "Original"; // Traducido
                installmentsInfo.append(String.format("  Cuota %d (%s): Original $%.2f, Pagado $%.2f, Restante $%.2f\n", // Traducido
                        c.getNumeroCuota(), installmentTypeStr, c.getMontoOriginal(), c.getMontoPagado(), c.getMontoRestante()));
            }
        }


        String amountStr = JOptionPane.showInputDialog(this, installmentsInfo.toString() + "\nIngrese el monto total a pagar en este momento:", "Monto de Pago", JOptionPane.QUESTION_MESSAGE); // Traducido
        if (amountStr == null || amountStr.trim().isEmpty()) {
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "El monto del pago debe ser un número positivo.", "Monto Inválido", JOptionPane.ERROR_MESSAGE); // Traducido
                return;
            }
            if (amount > client.calcularDeudaRestante() && client.calcularDeudaRestante() > 0) {
                int confirm = JOptionPane.showConfirmDialog(this,
                        String.format("El monto ingresado ($%.2f) es mayor que la deuda restante total ($%.2f). ¿Desea registrar este pago de todos modos?", amount, client.calcularDeudaRestante()), // Traducido
                        "Advertencia de Monto", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE); // Traducido
                if (confirm == JOptionPane.NO_OPTION) {
                    return;
                }
            }

            RegistrarPagoCommand command = new RegistrarPagoCommand(client, amount);
            command.execute();
            commandHistory.push(command);

            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
            JOptionPane.showMessageDialog(this, "Pago y cuota registrados exitosamente para " + client.getNombre() + ".", "Pago Registrado", JOptionPane.INFORMATION_MESSAGE); // Traducido
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto de pago inválido. Por favor, ingrese un número.", "Error de Entrada", JOptionPane.ERROR_MESSAGE); // Traducido
        }
    }

    /**
     * Elimina el cliente seleccionado de la lista.
     */
    private void deleteSelectedClient() {
        int selectedIndex = listaClientes.getSelectedIndex();
        if (selectedIndex == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un cliente de la lista para eliminar.", "Ningún Cliente Seleccionado", JOptionPane.WARNING_MESSAGE); // Traducido
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "¿Está seguro de que desea eliminar al cliente seleccionado?", // Traducido
                "Confirmar Eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE); // Traducido

        if (confirm == JOptionPane.YES_OPTION) {
            Cliente clientToDelete = clientes.get(selectedIndex);

            EliminarClienteCommand command = new EliminarClienteCommand(clientes, clientToDelete, selectedIndex);
            command.execute();
            commandHistory.push(command);

            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
            JOptionPane.showMessageDialog(this, "Cliente eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE); // Traducido
        }
    }

    /**
     * Deshace la última acción registrada en el historial.
     */
    private void undoLastAction() {
        if (!commandHistory.isEmpty()) {
            Command lastCommand = commandHistory.pop();
            lastCommand.undo();

            updateClientList();
            saveClientsToExcel();
            updateUndoButtonState();
            JOptionPane.showMessageDialog(this, "Última acción deshecha.", "Deshacer", JOptionPane.INFORMATION_MESSAGE); // Traducido
        } else {
            JOptionPane.showMessageDialog(this, "No hay acciones para deshacer.", "Deshacer", JOptionPane.INFORMATION_MESSAGE); // Traducido
        }
    }

    /**
     * Muestra una ventana de diálogo con los detalles completos del cliente.
     */
    private void showClientDetails() {
        // Primero, intentar obtener el cliente seleccionado en la JList
        int selectedIndex = listaClientes.getSelectedIndex();
        Cliente client = null;

        if (selectedIndex != -1) {
            // Si hay un cliente seleccionado en la lista, lo usamos
            client = clientes.get(selectedIndex);
        } else {
            // Si no hay selección, pedir el DNI
            String dni = JOptionPane.showInputDialog(this, "No hay cliente seleccionado.\nIngrese el DNI del cliente para ver los detalles:", "Ver Detalles", JOptionPane.QUESTION_MESSAGE); // Traducido
            if (dni == null || dni.trim().isEmpty()) {
                return; // El usuario canceló o no ingresó nada
            }
            client = clientes.stream()
                    .filter(c -> c.getDni().equals(dni.trim()))
                    .findFirst()
                    .orElse(null);
        }

        if (client == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado.", "Error", JOptionPane.ERROR_MESSAGE); // Traducido
            return;
        }

        // --- Construir el contenido de los detalles ---
        StringBuilder details = new StringBuilder();
        details.append("--- Detalles del Cliente ---\n"); // Traducido
        details.append(String.format("Nombre: %s\n", client.getNombre())); // Traducido
        details.append(String.format("DNI: %s\n", client.getDni())); // Traducido
        details.append(String.format("Tipo de Cuota: %s\n", client.getTipoCuota())); // Traducido
        details.append(String.format("Total del Producto: $%.2f\n", client.getTotalProducto())); // Traducido
        details.append(String.format("Valor por Cuota (promedio): $%.2f\n", client.getValorCuota())); // Traducido
        details.append(String.format("Total de Cuotas: %d\n", client.getTotalCuotas())); // Traducido
        details.append(String.format("Cuotas Pagadas: %d\n", client.getCuotasPagadasCount())); // Traducido
        details.append(String.format("Cuotas Pendientes: %d\n", client.getCuotasRestantes())); // Traducido
        details.append(String.format("Monto Total Pagado: $%.2f\n", client.getTotalPagado())); // Traducido
        details.append(String.format("Deuda Restante Total: $%.2f\n", client.calcularDeudaRestante())); // Traducido

        details.append("\n--- Detalle de Cada Cuota ---\n"); // Traducido
        if (client.getCuotas().isEmpty()) {
            details.append("  No hay cuotas registradas para este cliente.\n"); // Traducido
        } else {
            for (Cuota c : client.getCuotas()) {
                String installmentTypeStr = c.isFaltante() ? "Faltante" : "Original"; // Traducido
                details.append(String.format("  Cuota %d (%s): Original $%.2f, Pagado $%.2f, Restante $%.2f\n", // Traducido
                        c.getNumeroCuota(), installmentTypeStr, c.getMontoOriginal(), c.getMontoPagado(), c.getMontoRestante()));
            }
        }

        // --- Crear y mostrar el JDialog ---
        JDialog dialog = new JDialog(this, "Detalles del Cliente: " + client.getNombre(), true); // Traducido
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));

        JTextArea textArea = new JTextArea(details.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        dialog.add(scrollPane, BorderLayout.CENTER);

        JButton closeButton = new JButton("Cerrar"); // Traducido
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }


    private void updateClientList() {
        clienteListModel.clear();
        if (clientes != null) {
            for (Cliente c : clientes) {
                clienteListModel.addElement(c.toString());
            }
        }
    }

    private void loadClientsFromExcel() {
        clientes = ExcelExporter.importarClientes(EXCEL_FILE_PATH);
        if (clientes == null) {
            clientes = new ArrayList<>();
        }
    }

    private void saveClientsToExcel() {
        ExcelExporter.exportarClientes(clientes, EXCEL_FILE_PATH);
    }

    // Clase auxiliar para simplificar DocumentListener
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private Runnable callback;
        public SimpleDocumentListener(Runnable callback) {
            this.callback = callback;
        }
        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) { callback.run(); }
    }
}

package com.inventario.ui;

import com.inventario.models.Cliente;
import com.inventario.utils.ExcelExporter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MainWindow extends JFrame {
    private List<Cliente> clientes = new ArrayList<>();
    private DefaultListModel<String> clienteListModel = new DefaultListModel<>();
    private JList<String> listaClientes = new JList<>(clienteListModel);

    public MainWindow() {
        setTitle("Inventario de Clientes - Tecnología");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());


        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Cliente");
        JButton btnPagarCuota = new JButton("Registrar Pago de Cuota");
        JButton btnActualizar = new JButton("Actualizar Lista");
        JButton btnExportarExcel = new JButton("Exportar a Excel");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnPagarCuota);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnExportarExcel);

        add(panelBotones, BorderLayout.NORTH);
        add(new JScrollPane(listaClientes), BorderLayout.CENTER);

        btnAgregar.addActionListener(e -> mostrarFormularioAgregarCliente());
        btnPagarCuota.addActionListener(e -> mostrarFormularioPagarCuota());
        btnActualizar.addActionListener(e -> actualizarLista());
        btnExportarExcel.addActionListener(e -> {
            String ruta = "clientes.xlsx";
            ExcelExporter.exportarClientes(clientes, ruta);
            JOptionPane.showMessageDialog(this, "Clientes exportados a: " + ruta);
        });

    }

    private void mostrarFormularioAgregarCliente() {
        JTextField nombre = new JTextField();
        JTextField dni = new JTextField();
        JTextField tipoCuota = new JTextField();
        JTextField total = new JTextField();

        Object[] campos = {
                "Nombre:", nombre,
                "DNI:", dni,
                "Tipo de Cuota (mensual/quincenal):", tipoCuota,
                "Total del producto:", total
        };

        int opcion = JOptionPane.showConfirmDialog(this, campos, "Nuevo Cliente", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                Cliente c = new Cliente(
                        nombre.getText(),
                        dni.getText(),
                        tipoCuota.getText(),
                        Double.parseDouble(total.getText())
                );
                clientes.add(c);
                actualizarLista();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "El total debe ser un número válido.");
            }
        }
    }

    private void mostrarFormularioPagarCuota() {
        String dni = JOptionPane.showInputDialog(this, "Ingrese el DNI del cliente:");
        Cliente cliente = clientes.stream()
                .filter(c -> c.getDni().equals(dni))
                .findFirst()
                .orElse(null);

        if (cliente == null) {
            JOptionPane.showMessageDialog(this, "Cliente no encontrado.");
            return;
        }

        String montoStr = JOptionPane.showInputDialog(this, "Ingrese el monto de la cuota:");
        try {
            double monto = Double.parseDouble(montoStr);
            cliente.pagarCuota(monto);
            actualizarLista();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Monto inválido.");
        }
    }

    private void actualizarLista() {
        clienteListModel.clear();
        for (Cliente c : clientes) {
            clienteListModel.addElement(c.toString());
        }
    }
}

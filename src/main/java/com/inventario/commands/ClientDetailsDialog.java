package com.inventario.ui;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClientDetailsDialog extends JDialog {

    private JLabel lblNombre, lblApellido, lblDni, lblTipoCuota, lblProducto, lblTotalProducto, lblValorCuota,
            lblCuotasTotales, lblCuotasPagadas, lblCuotasPendientes,
            lblMontoPagado, lblMontoPendiente, lblAdelantoAcumulado;
    private JTable tablaCuotas;
    private DefaultTableModel modeloTablaCuotas;
    private DecimalFormat currencyFormat = new DecimalFormat("$#,##0.00");
    private DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ClientDetailsDialog(JFrame parent, Cliente cliente) {
        super(parent, "Detalles del Cliente: " + cliente.getNombre() + " " + cliente.getApellido(), true);
        setSize(800, 600);
        setMinimumSize(new Dimension(750, 550));
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(15, 15));

        // --- Panel de Información General del Cliente ---
        JPanel panelInfoGeneral = new JPanel(new GridBagLayout());
        panelInfoGeneral.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Datos del Cliente"
        ));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("SansSerif", Font.BOLD, 12);
        Font valueFont = new Font("SansSerif", Font.PLAIN, 12);

        // Nombre
        gbc.gridx = 0; gbc.gridy = 0; gbc.anchor = GridBagConstraints.WEST;
        JLabel l1 = new JLabel("Nombre:"); l1.setFont(labelFont);
        panelInfoGeneral.add(l1, gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
        lblNombre = new JLabel(); lblNombre.setFont(valueFont);
        panelInfoGeneral.add(lblNombre, gbc);

        // Apellido
        gbc.gridx = 0; gbc.gridy = 1; gbc.anchor = GridBagConstraints.WEST;
        JLabel l6 = new JLabel("Apellido:"); l6.setFont(labelFont);
        panelInfoGeneral.add(l6, gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
        lblApellido = new JLabel(); lblApellido.setFont(valueFont);
        panelInfoGeneral.add(lblApellido, gbc);

        // DNI
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel l2 = new JLabel("DNI:"); l2.setFont(labelFont);
        panelInfoGeneral.add(l2, gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 1.0;
        lblDni = new JLabel(); lblDni.setFont(valueFont);
        panelInfoGeneral.add(lblDni, gbc);

        // Tipo de Cuota
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.0;
        JLabel l3 = new JLabel("Tipo Cuota:"); l3.setFont(labelFont);
        panelInfoGeneral.add(l3, gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 1.0;
        lblTipoCuota = new JLabel(); lblTipoCuota.setFont(valueFont);
        panelInfoGeneral.add(lblTipoCuota, gbc);

        // Producto (Nuevo campo)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0.0;
        JLabel l8 = new JLabel("Producto:"); l8.setFont(labelFont);
        panelInfoGeneral.add(l8, gbc);
        gbc.gridx = 1; gbc.gridy = 4; gbc.weightx = 1.0;
        lblProducto = new JLabel(); lblProducto.setFont(valueFont);
        panelInfoGeneral.add(lblProducto, gbc);


        // Total Producto
        gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
        JLabel l4 = new JLabel("Total Producto:"); l4.setFont(labelFont);
        panelInfoGeneral.add(l4, gbc);
        gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1.0;
        lblTotalProducto = new JLabel(); lblTotalProducto.setFont(valueFont);
        panelInfoGeneral.add(lblTotalProducto, gbc);

        // Valor Cuota
        gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
        JLabel l5 = new JLabel("Valor Cuota:"); l5.setFont(labelFont);
        panelInfoGeneral.add(l5, gbc);
        gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1.0;
        lblValorCuota = new JLabel(); lblValorCuota.setFont(valueFont);
        panelInfoGeneral.add(lblValorCuota, gbc);

        // Adelanto Acumulado
        gbc.gridx = 2; gbc.gridy = 2; gbc.weightx = 0.0;
        JLabel l7 = new JLabel("Adelanto Acumulado:"); l7.setFont(labelFont);
        panelInfoGeneral.add(l7, gbc);
        gbc.gridx = 3; gbc.gridy = 2; gbc.weightx = 1.0;
        lblAdelantoAcumulado = new JLabel(); lblAdelantoAcumulado.setFont(valueFont);
        panelInfoGeneral.add(lblAdelantoAcumulado, gbc);


        add(panelInfoGeneral, BorderLayout.NORTH);

        // --- Panel de Resumen de Cuotas ---
        JPanel panelResumenCuotas = new JPanel(new GridLayout(2, 3, 10, 5));
        panelResumenCuotas.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Resumen de Cuotas"
        ));

        Font summaryFont = new Font("SansSerif", Font.BOLD, 13);
        Font summaryValueFont = new Font("SansSerif", Font.PLAIN, 13);

        panelResumenCuotas.add(createSummaryLabel("Totales:", summaryFont));
        lblCuotasTotales = createSummaryValueLabel(summaryValueFont);
        panelResumenCuotas.add(lblCuotasTotales);

        panelResumenCuotas.add(createSummaryLabel("Pagadas:", summaryFont));
        lblCuotasPagadas = createSummaryValueLabel(summaryValueFont);
        panelResumenCuotas.add(lblCuotasPagadas);

        panelResumenCuotas.add(createSummaryLabel("Pendientes:", summaryFont));
        lblCuotasPendientes = createSummaryValueLabel(summaryValueFont);
        panelResumenCuotas.add(lblCuotasPendientes);

        panelResumenCuotas.add(createSummaryLabel("Monto Pagado:", summaryFont));
        lblMontoPagado = createSummaryValueLabel(summaryValueFont);
        panelResumenCuotas.add(lblMontoPagado);

        panelResumenCuotas.add(createSummaryLabel("Monto Pendiente:", summaryFont));
        lblMontoPendiente = createSummaryValueLabel(summaryValueFont);
        panelResumenCuotas.add(lblMontoPendiente);


        // --- Tabla de Cuotas ---
        String[] columnas = {"Nº", "Monto Original", "Monto Pagado", "Monto Restante", "Vencimiento", "Fecha Pago", "Estado"};
        modeloTablaCuotas = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaCuotas = new JTable(modeloTablaCuotas);
        tablaCuotas.setFillsViewportHeight(true);
        tablaCuotas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaCuotas.getTableHeader().setReorderingAllowed(false);
        tablaCuotas.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tablaCuotas.setRowHeight(20);

        JScrollPane scrollPane = new JScrollPane(tablaCuotas);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createEtchedBorder(), "Detalle de Cuotas"
        ));

        // --- Panel Central con Resumen y Tabla ---
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.add(panelResumenCuotas, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // --- Botón de Cerrar ---
        JPanel panelBotonCerrar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnCerrar.setBackground(new Color(100, 149, 237));
        btnCerrar.setForeground(Color.WHITE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setBorder(BorderFactory.createLineBorder(new Color(60, 120, 200), 1, true));
        btnCerrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());
        panelBotonCerrar.add(btnCerrar);

        add(panelBotonCerrar, BorderLayout.SOUTH);

        loadClientData(cliente);
    }

    private JLabel createSummaryLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
        return label;
    }

    private JLabel createSummaryValueLabel(Font font) {
        JLabel label = new JLabel();
        label.setFont(font);
        label.setHorizontalAlignment(SwingConstants.LEFT);
        return label;
    }

    private void loadClientData(Cliente cliente) {
        lblNombre.setText(cliente.getNombre());
        lblApellido.setText(cliente.getApellido());
        lblDni.setText(cliente.getDni());
        lblTipoCuota.setText(cliente.getTipoCuota());
        lblProducto.setText(cliente.getProducto()); // Mostrar el producto
        lblTotalProducto.setText(currencyFormat.format(cliente.getTotalProducto()));
        lblValorCuota.setText(currencyFormat.format(cliente.getValorCuota()));
        lblAdelantoAcumulado.setText(currencyFormat.format(cliente.getAdelantoAcumulado()));

        modeloTablaCuotas.setRowCount(0);

        List<Cuota> cuotas = cliente.getCuotas();
        int totalCuotas = cuotas.size();
        long cuotasPagadas = cuotas.stream().filter(Cuota::estaPagada).count();
        long cuotasPendientes = totalCuotas - cuotasPagadas;

        double montoTotalPagado = cuotas.stream()
                .mapToDouble(Cuota::getMontoPagado)
                .sum();
        double montoTotalPendiente = cuotas.stream()
                .mapToDouble(Cuota::getMontoRestante)
                .sum();

        lblCuotasTotales.setText(String.valueOf(totalCuotas));
        lblCuotasPagadas.setText(String.valueOf(cuotasPagadas));
        lblCuotasPendientes.setText(String.valueOf(cuotasPendientes));
        lblMontoPagado.setText(currencyFormat.format(montoTotalPagado));
        lblMontoPendiente.setText(currencyFormat.format(montoTotalPendiente));


        for (Cuota cuota : cuotas) {
            String fechaVencimientoStr = cuota.getFechaVencimiento() != null ? cuota.getFechaVencimiento().format(dateFormat) : "N/A";
            String fechaPagoStr = cuota.getFechaPago() != null ? cuota.getFechaPago().format(dateFormat) : "N/A";

            modeloTablaCuotas.addRow(new Object[]{
                    cuota.getNumeroCuota(),
                    currencyFormat.format(cuota.getMontoOriginal()),
                    currencyFormat.format(cuota.getMontoPagado()),
                    currencyFormat.format(cuota.getMontoRestante()),
                    fechaVencimientoStr,
                    fechaPagoStr,
                    cuota.estaPagada() ? "Pagada" : "Pendiente"
            });
        }
    }
}

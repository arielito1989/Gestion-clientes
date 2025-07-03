package com.inventario.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

public class Cliente {
    private String nombre;
    private String apellido;
    private String dni;
    private String tipoCuota; // Ej: "mensual", "quincenal"
    private String producto; // Nuevo campo para el nombre del producto
    private double totalProducto;
    private List<Cuota> cuotas = new ArrayList<>(); // Inicialización directa para evitar NullPointerException
    private double adelantoAcumulado;

    // Constructor para un nuevo cliente
    // Ahora incluye 'producto'
    public Cliente(String nombre, String apellido, String dni, String tipoCuota, String producto, double totalProducto, int totalCuotas, double valorCuota, LocalDate fechaInicioCuotas) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.producto = producto; // Asignar el producto
        this.totalProducto = totalProducto;
        this.cuotas = new ArrayList<>(); // Se mantiene para crear una nueva lista en cada nuevo cliente
        this.adelantoAcumulado = 0.0;
        for (int i = 1; i <= totalCuotas; i++) {
            LocalDate fechaVencimiento;
            if ("mensual".equalsIgnoreCase(tipoCuota)) {
                fechaVencimiento = fechaInicioCuotas.plusMonths(i - 1);
            } else if ("quincenal".equalsIgnoreCase(tipoCuota)) {
                fechaVencimiento = fechaInicioCuotas.plusWeeks((i - 1) * 2);
            } else {
                fechaVencimiento = null;
            }
            this.cuotas.add(new Cuota(i, valorCuota, 0.0, fechaVencimiento, null, false));
        }
    }

    // Constructor para cargar desde Excel (con la lista de Cuotas deserializada)
    // Ahora incluye 'producto' y 'adelantoAcumulado'
    public Cliente(String nombre, String apellido, String dni, String tipoCuota, String producto, double totalProducto, List<Cuota> cuotas, double adelantoAcumulado) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.producto = producto; // Cargar el producto
        this.totalProducto = totalProducto;
        this.cuotas = cuotas != null ? cuotas : new ArrayList<>(); // Asegura que cuotas nunca sea null
        this.adelantoAcumulado = adelantoAcumulado; // Cargar adelanto acumulado
    }

    public double getTotalPagado() {
        return cuotas.stream().mapToDouble(Cuota::getMontoPagado).sum();
    }

    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public String getDni() { return dni; }
    public String getTipoCuota() { return tipoCuota; }
    public String getProducto() { return producto; } // Nuevo getter para el producto
    public double getTotalProducto() { return totalProducto; }

    public List<Cuota> getCuotas() {
        return cuotas;
    }

    public double getAdelantoAcumulado() {
        return adelantoAcumulado;
    }

    public void setAdelantoAcumulado(double adelantoAcumulado) {
        this.adelantoAcumulado = adelantoAcumulado;
    }

    /**
     * Aplica un pago total al cliente, distribuyéndolo entre las cuotas pendientes
     * y manejando cualquier excedente como adelanto acumulado.
     *
     * @param montoPago El monto total del pago recibido.
     */
    public void aplicarPagoACuotas(double montoPago) {
        double pagoRestante = montoPago;

        // Primero, usar cualquier adelanto acumulado existente para cubrir cuotas
        pagoRestante += this.adelantoAcumulado;
        this.adelantoAcumulado = 0.0;

        List<Cuota> cuotasOrdenadas = new ArrayList<>(cuotas);
        cuotasOrdenadas.sort(Comparator
                .comparing(Cuota::estaPagada)
                .thenComparing(Cuota::getFechaVencimiento, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Cuota::getNumeroCuota));

        for (Cuota cuota : cuotasOrdenadas) {
            if (pagoRestante <= 0) {
                break;
            }

            if (!cuota.estaPagada()) {
                double excedente = cuota.aplicarPago(pagoRestante);
                pagoRestante = excedente;
            }
        }

        this.adelantoAcumulado = pagoRestante;
    }

    public double calcularDeudaRestante() {
        return cuotas.stream().mapToDouble(Cuota::getMontoRestante).sum();
    }

    public int getTotalCuotas() {
        return cuotas.size();
    }

    public int getCuotasPagadasCount() {
        return (int) cuotas.stream().filter(Cuota::estaPagada).count();
    }

    public int getCuotasRestantes() {
        return getTotalCuotas() - getCuotasPagadasCount();
    }

    public double getValorCuota() {
        return cuotas.stream()
                .filter(c -> !c.isFaltante())
                .findFirst()
                .map(Cuota::getMontoOriginal)
                .orElse(0.0);
    }

    @Override
    public String toString() {
        String progresoCuotas = String.format("Pagadas: %d/%d", getCuotasPagadasCount(), getTotalCuotas());
        String adelantoStr = (adelantoAcumulado > 0) ? String.format(" | Adelanto: $%.2f", adelantoAcumulado) : "";

        return String.format("%s %s (DNI: %s) | Producto: %s ($%.2f) | Cuotas: %s ($%.2f c/u) | %s | Deuda: $%.2f%s",
                nombre, apellido, dni, producto, totalProducto, tipoCuota, getValorCuota(),
                progresoCuotas, calcularDeudaRestante(), adelantoStr);
    }

    public String getDetalles() {
        StringBuilder detalles = new StringBuilder();
        detalles.append("Nombre: ").append(nombre).append(" ").append(apellido).append("\n");
        detalles.append("DNI: ").append(dni).append("\n");
        detalles.append("Tipo de Cuota: ").append(tipoCuota).append("\n");
        detalles.append("Producto: ").append(producto).append("\n"); // Mostrar el producto
        detalles.append("Total del Producto: $").append(String.format("%.2f", totalProducto)).append("\n");
        detalles.append("Valor por Cuota: $").append(String.format("%.2f", getValorCuota())).append("\n");
        detalles.append("Total de Cuotas: ").append(getTotalCuotas()).append("\n");
        detalles.append("Cuotas Pagadas: ").append(getCuotasPagadasCount()).append("\n");
        detalles.append("Cuotas Pendientes: ").append(getCuotasRestantes()).append("\n");
        detalles.append("Total Pagado: $").append(String.format("%.2f", getTotalPagado())).append("\n");
        detalles.append("Deuda Restante: $").append(String.format("%.2f", calcularDeudaRestante())).append("\n");
        if (adelantoAcumulado > 0) {
            detalles.append("Adelanto Acumulado: $").append(String.format("%.2f", adelantoAcumulado)).append("\n");
        }
        return detalles.toString();
    }
}

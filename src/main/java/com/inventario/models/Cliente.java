package com.inventario.models;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nombre;
    private String dni;
    private String tipoCuota; // Ej: "mensual", "quincenal"
    private double totalProducto;
    private int totalCuotas; // Nuevo: Número total de cuotas acordadas
    private double valorCuota; // Nuevo: Valor de cada cuota
    private int cuotasPagadasCount; // Nuevo: Contador de cuotas pagadas
    private List<Double> cuotasPagadas; // Lista de montos de pagos individuales

    // Constructor actualizado para incluir la información de cuotas
    public Cliente(String nombre, String dni, String tipoCuota, double totalProducto, int totalCuotas, double valorCuota) {
        this.nombre = nombre;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.totalProducto = totalProducto;
        this.totalCuotas = totalCuotas;
        this.valorCuota = valorCuota;
        this.cuotasPagadasCount = 0; // Inicialmente, no hay cuotas pagadas
        this.cuotasPagadas = new ArrayList<>();
    }

    // Constructor para cargar desde Excel (incluyendo cuotas pagadas y su contador)
    public Cliente(String nombre, String dni, String tipoCuota, double totalProducto, int totalCuotas, double valorCuota, int cuotasPagadasCount, double totalPagado) {
        this.nombre = nombre;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.totalProducto = totalProducto;
        this.totalCuotas = totalCuotas;
        this.valorCuota = valorCuota;
        this.cuotasPagadasCount = cuotasPagadasCount;
        this.cuotasPagadas = new ArrayList<>();
        // Al cargar desde Excel, el 'totalPagado' es la suma, lo añadimos para reconstruir el estado
        if (totalPagado > 0) {
            this.cuotasPagadas.add(totalPagado);
        }
    }


    public double getTotalPagado() {
        return cuotasPagadas.stream().mapToDouble(Double::doubleValue).sum();
    }

    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
    public String getTipoCuota() { return tipoCuota; }
    public double getTotalProducto() { return totalProducto; }

    public int getTotalCuotas() { return totalCuotas; } // Nuevo getter
    public double getValorCuota() { return valorCuota; } // Nuevo getter
    public int getCuotasPagadasCount() { return cuotasPagadasCount; } // Nuevo getter

    // Método para registrar un pago de monto (se mantiene como antes)
    public void pagarCuota(double monto) {
        cuotasPagadas.add(monto);
    }

    // Nuevo método para incrementar el contador de cuotas pagadas
    public void incrementarCuotaPagada() {
        if (cuotasPagadasCount < totalCuotas) {
            cuotasPagadasCount++;
        }
    }

    // Nuevo método para obtener las cuotas restantes
    public int getCuotasRestantes() {
        return totalCuotas - cuotasPagadasCount;
    }

    public double calcularDeudaRestante() {
        return totalProducto - this.getTotalPagado();
    }

    public List<Double> getCuotasPagadas() {
        return cuotasPagadas;
    }

    @Override
    public String toString() {
        // Formato mejorado para incluir la información de cuotas
        return String.format("%s | DNI: %s | Producto: $%.2f | Cuotas: %s (%.2f c/u) | Pagadas: %d/%d | Deuda: $%.2f",
                nombre, dni, totalProducto, tipoCuota, valorCuota,
                cuotasPagadasCount, totalCuotas, calcularDeudaRestante());
    }
}

package com.inventario.models;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nombre;
    private String dni;
    private String tipoCuota;
    private double totalProducto;
    private List<Double> cuotasPagadas;

    public Cliente(String nombre, String dni, String tipoCuota, double totalProducto) {
        this.nombre = nombre;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.totalProducto = totalProducto;
        this.cuotasPagadas = new ArrayList<>();
    }
    public double getTotalPagado() {
        return cuotasPagadas.stream().mapToDouble(Double::doubleValue).sum();
    }


    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
    public String getTipoCuota() { return tipoCuota; }
    public double getTotalProducto() { return totalProducto; }

    public void pagarCuota(double monto) {
        cuotasPagadas.add(monto);
    }

    public double calcularDeudaRestante() {
        return totalProducto - cuotasPagadas.stream().mapToDouble(Double::doubleValue).sum();
    }

    public List<Double> getCuotasPagadas() {
        return cuotasPagadas;
    }

    @Override
    public String toString() {
        return nombre + " | DNI: " + dni + " | Cuotas: " + tipoCuota +
                " | Total: $" + totalProducto +
                " | Pagado: $" + cuotasPagadas.stream().mapToDouble(Double::doubleValue).sum() +
                " | Deuda: $" + calcularDeudaRestante();
    }
}

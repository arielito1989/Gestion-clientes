package com.inventario.models;

import java.util.ArrayList;
import java.util.List;

public class Cliente {
    private String nombre;
    private String dni;
    private String tipoCuota; // Ej: "mensual", "quincenal"
    private double totalProducto;
    private List<Cuota> cuotas; // Lista de objetos Cuota

    // Constructor para un nuevo cliente
    public Cliente(String nombre, String dni, String tipoCuota, double totalProducto, int totalCuotas, double valorCuota) {
        this.nombre = nombre;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.totalProducto = totalProducto;
        this.cuotas = new ArrayList<>();
        // Generar las cuotas individuales al crear el cliente
        for (int i = 1; i <= totalCuotas; i++) {
            this.cuotas.add(new Cuota(i, valorCuota));
        }
    }

    // Constructor para cargar desde Excel (con la lista de Cuotas deserializada)
    public Cliente(String nombre, String dni, String tipoCuota, double totalProducto, List<Cuota> cuotas) {
        this.nombre = nombre;
        this.dni = dni;
        this.tipoCuota = tipoCuota;
        this.totalProducto = totalProducto;
        this.cuotas = cuotas != null ? cuotas : new ArrayList<>();
    }

    public double getTotalPagado() {
        return cuotas.stream().mapToDouble(Cuota::getMontoPagado).sum();
    }

    public String getNombre() { return nombre; }
    public String getDni() { return dni; }
    public String getTipoCuota() { return tipoCuota; }
    public double getTotalProducto() { return totalProducto; }

    public List<Cuota> getCuotas() {
        return cuotas;
    }

    // Método para aplicar un pago a las cuotas, manejando adelantos y faltantes
    public void aplicarPagoACuotas(double montoPago) {
        double pagoRestante = montoPago;
        List<Cuota> nuevasCuotasFaltantes = new ArrayList<>(); // Para recolectar nuevas cuotas de faltantes

        for (int i = 0; i < cuotas.size(); i++) {
            Cuota cuota = cuotas.get(i);
            if (pagoRestante <= 0) {
                break; // No hay más monto del pago para aplicar
            }

            if (!cuota.estaPagada()) {
                // Aplica el pago a la cuota actual
                pagoRestante = cuota.aplicarPago(pagoRestante);

                // Si la cuota no se pagó completamente y el pago se agotó en esta cuota (o es el final del pago)
                if (cuota.getMontoRestante() > 0 && pagoRestante == 0) {
                    // Esto significa que el pago fue parcial para esta cuota.
                    // El monto restante de esta cuota es el "faltante" que debe ir a una nueva cuota.
                    double faltante = cuota.getMontoRestante();

                    // Ajustar el monto original de la cuota actual a lo que realmente se pagó de ella.
                    // Esto "cierra" la cuota actual al monto cubierto.
                    cuota.setMontoOriginal(cuota.getMontoPagado());

                    // Crear una nueva cuota para el faltante
                    // El número de cuota para el faltante debe ser único y consecutivo
                    int nextCuotaNum = cuotas.size() + nuevasCuotasFaltantes.size() + 1;
                    Cuota nuevaCuotaFaltante = new Cuota(nextCuotaNum, faltante, 0.0, true); // Es un faltante
                    nuevasCuotasFaltantes.add(nuevaCuotaFaltante);

                    // El pago se agotó aquí.
                    break;
                }
            }
        }
        // Añadir las nuevas cuotas de faltantes al final de la lista principal
        cuotas.addAll(nuevasCuotasFaltantes);
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

    // Método para obtener el valor de la primera cuota original (si todas son iguales)
    public double getValorCuota() {
        // Busca la primera cuota que no sea un faltante para obtener su valor original
        return cuotas.stream()
                .filter(c -> !c.isFaltante())
                .findFirst()
                .map(Cuota::getMontoOriginal)
                .orElse(0.0);
    }

    @Override
    public String toString() {
        String progresoCuotas = String.format("Pagadas: %d/%d", getCuotasPagadasCount(), getTotalCuotas());

        return String.format("%s | DNI: %s | Producto: $%.2f | Cuotas: %s ($%.2f c/u) | %s | Deuda: $%.2f",
                nombre, dni, totalProducto, tipoCuota, getValorCuota(),
                progresoCuotas, calcularDeudaRestante());
    }
}

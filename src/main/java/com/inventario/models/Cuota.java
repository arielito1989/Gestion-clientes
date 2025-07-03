package com.inventario.models;

import java.time.LocalDate;

public class Cuota {
    private int numeroCuota;
    private double montoOriginal;
    private double montoPagado;
    private LocalDate fechaVencimiento;
    private LocalDate fechaPago;
    private boolean isFaltante;

    public Cuota(int numeroCuota, double montoOriginal) {
        this(numeroCuota, montoOriginal, 0.0, null, null, false);
    }

    public Cuota(int numeroCuota, double montoOriginal, double montoPagado, LocalDate fechaVencimiento, LocalDate fechaPago, boolean isFaltante) {
        this.numeroCuota = numeroCuota;
        this.montoOriginal = montoOriginal;
        this.montoPagado = montoPagado;
        this.fechaVencimiento = fechaVencimiento;
        this.fechaPago = fechaPago;
        this.isFaltante = isFaltante;
    }

    public Cuota(int numeroCuota, double montoOriginal, double montoPagado, boolean isFaltante) {
        this(numeroCuota, montoOriginal, montoPagado, null, null, isFaltante);
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public double getMontoOriginal() {
        return montoOriginal;
    }

    public void setMontoOriginal(double montoOriginal) {
        this.montoOriginal = montoOriginal;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public LocalDate getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDate fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public LocalDate getFechaPago() {
        return fechaPago;
    }

    public void setFechaPago(LocalDate fechaPago) {
        this.fechaPago = fechaPago;
    }

    public boolean isFaltante() {
        return isFaltante;
    }

    public double getMontoRestante() {
        return montoOriginal - montoPagado;
    }

    /**
     * Aplica un pago a esta cuota.
     * @param pago El monto del pago a aplicar.
     * @return El excedente del pago si la cuota se cubre completamente, o 0 si no hay excedente.
     */
    public double aplicarPago(double pago) {
        double restante = getMontoRestante();
        if (pago >= restante) {
            montoPagado += restante;
            this.setFechaPago(LocalDate.now());
            return pago - restante; // Devuelve el excedente
        } else {
            montoPagado += pago;
            this.setFechaPago(null); // Si el pago es parcial, la fecha de pago se anula
            return 0.0; // No hay excedente
        }
    }

    /**
     * Deshace un pago de esta cuota.
     * @param monto El monto a deshacer.
     * @return El monto que "sobró" al deshacer (ej. si se deshizo un pago que dejó la cuota en negativo).
     */
    public double deshacerPago(double monto) {
        double montoParaDeshacer = Math.min(monto, montoPagado);
        montoPagado -= montoParaDeshacer;
        if (montoPagado < montoOriginal) {
            this.setFechaPago(null);
        }
        return monto - montoParaDeshacer;
    }

    public boolean estaPagada() {
        return montoPagado >= montoOriginal;
    }

    @Override
    public String toString() {
        String tipo = isFaltante ? "FALTANTE" : "ORIGINAL";
        String estado = estaPagada() ? "Pagada" : "Pendiente";
        String fechaVenc = (fechaVencimiento != null) ? fechaVencimiento.toString() : "N/A";
        String fechaPag = (fechaPago != null) ? fechaPago.toString() : "N/A";

        return String.format("Cuota %d (%s): Original $%.2f, Pagado $%.2f, Restante $%.2f | Venc: %s | Pago: %s | Estado: %s",
                numeroCuota, tipo, montoOriginal, montoPagado, getMontoRestante(), fechaVenc, fechaPag, estado);
    }
}

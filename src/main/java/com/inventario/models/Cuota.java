package com.inventario.models;

// Clase para representar una cuota individual de un cliente.
public class Cuota {
    private int numeroCuota; // Número de la cuota (ej. 1, 2, 3...)
    private double montoOriginal; // Monto total que debe tener esta cuota
    private double montoPagado; // Cuánto se ha pagado de esta cuota
    private boolean isFaltante; // Nuevo: true si esta cuota es un "faltante" de una cuota anterior

    public Cuota(int numeroCuota, double montoOriginal) {
        this(numeroCuota, montoOriginal, 0.0, false); // Llama al constructor completo
    }

    // Constructor para deserialización desde JSON (usado por ExcelExporter) y para crear faltantes
    public Cuota(int numeroCuota, double montoOriginal, double montoPagado, boolean isFaltante) {
        this.numeroCuota = numeroCuota;
        this.montoOriginal = montoOriginal;
        this.montoPagado = montoPagado;
        this.isFaltante = isFaltante;
    }

    public int getNumeroCuota() {
        return numeroCuota;
    }

    public double getMontoOriginal() {
        return montoOriginal;
    }

    // Nuevo setter para ajustar el monto original (usado en la lógica de refinanciación)
    public void setMontoOriginal(double montoOriginal) {
        this.montoOriginal = montoOriginal;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public boolean isFaltante() {
        return isFaltante;
    }

    /**
     * Calcula el monto restante por pagar de esta cuota.
     * @return El monto restante.
     */
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
            // El pago cubre o excede el monto restante de esta cuota
            montoPagado += restante; // Paga el total restante de esta cuota
            return pago - restante; // Devuelve el excedente
        } else {
            // El pago es menor que el monto restante
            montoPagado += pago; // Paga una parte de la cuota
            return 0.0; // No hay excedente
        }
    }

    /**
     * Deshace un pago de esta cuota.
     * @param monto El monto a deshacer.
     * @return El monto que "sobró" al deshacer (ej. si se deshizo un pago que dejó la cuota en negativo).
     */
    public double deshacerPago(double monto) {
        double montoParaDeshacer = Math.min(monto, montoPagado); // No deshacer más de lo pagado
        montoPagado -= montoParaDeshacer;
        return monto - montoParaDeshacer; // Devuelve lo que no se pudo deshacer de esta cuota
    }

    /**
     * Verifica si la cuota ha sido pagada en su totalidad.
     * @return true si el monto pagado es igual o mayor al monto original.
     */
    public boolean estaPagada() {
        return montoPagado >= montoOriginal;
    }

    @Override
    public String toString() {
        String tipo = isFaltante ? "FALTANTE" : "ORIGINAL";
        return String.format("Cuota %d (%s): Original $%.2f, Pagado $%.2f, Restante $%.2f (%s)",
                numeroCuota, tipo, montoOriginal, montoPagado, getMontoRestante(), estaPagada() ? "Pagada" : "Pendiente");
    }
}

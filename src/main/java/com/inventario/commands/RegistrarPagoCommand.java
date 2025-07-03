package com.inventario.commands;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RegistrarPagoCommand implements Command {
    private Cliente cliente;
    private double montoPago;

    // Para deshacer: guardar el estado previo del cliente y sus cuotas
    private double clienteAdelantoAcumuladoBefore;
    private List<Cuota> cuotasBefore; // Una copia profunda de las cuotas

    public RegistrarPagoCommand(Cliente cliente, double montoPago) {
        this.cliente = cliente;
        this.montoPago = montoPago;
        // Guardar el estado actual del cliente para el deshacer
        this.clienteAdelantoAcumuladoBefore = cliente.getAdelantoAcumulado();
        this.cuotasBefore = new ArrayList<>();
        // Realizar una copia profunda de las cuotas
        for (Cuota c : cliente.getCuotas()) {
            this.cuotasBefore.add(new Cuota(c.getNumeroCuota(), c.getMontoOriginal(), c.getMontoPagado(), c.getFechaVencimiento(), c.getFechaPago(), c.isFaltante()));
        }
    }

    @Override
    public void execute() {
        // La lógica de aplicar el pago y distribuir el excedente ahora está en Cliente.aplicarPagoACuotas
        cliente.aplicarPagoACuotas(montoPago);
    }

    @Override
    public void undo() {
        // Restaurar el adelanto acumulado del cliente
        cliente.setAdelantoAcumulado(clienteAdelantoAcumuladoBefore);

        // Restaurar el estado de las cuotas desde la copia profunda
        cliente.getCuotas().clear();
        for (Cuota c : cuotasBefore) {
            cliente.getCuotas().add(c);
        }
    }
}

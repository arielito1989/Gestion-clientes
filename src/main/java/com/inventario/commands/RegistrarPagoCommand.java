package com.inventario.commands;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import java.util.ArrayList;
import java.util.List;

// Comando para registrar un pago.
public class RegistrarPagoCommand implements Command {
    private Cliente cliente;
    private double montoPagado;
    private List<Cuota> cuotasSnapshot; // Copia profunda del estado de las cuotas ANTES del pago

    public RegistrarPagoCommand(Cliente cliente, double montoPagado) {
        this.cliente = cliente;
        this.montoPagado = montoPagado;
        // Guardar una COPIA PROFUNDA del estado actual de las cuotas ANTES de ejecutar la acción.
        // Esto es crucial porque la ejecución del comando puede modificar la lista de cuotas (añadir faltantes).
        this.cuotasSnapshot = new ArrayList<>();
        for (Cuota c : cliente.getCuotas()) {
            // Creamos una nueva instancia de Cuota para cada una en el snapshot
            this.cuotasSnapshot.add(new Cuota(c.getNumeroCuota(), c.getMontoOriginal(), c.getMontoPagado(), c.isFaltante()));
        }
    }

    @Override
    public void execute() {
        // Ejecuta la acción de pago usando el nuevo método en Cliente
        cliente.aplicarPagoACuotas(montoPagado);
    }

    @Override
    public void undo() {
        // Deshace la acción: restaura el estado de las cuotas al snapshot.
        // Limpiamos la lista actual y la volvemos a poblar con el estado guardado.
        cliente.getCuotas().clear();
        for (Cuota c : cuotasSnapshot) {
            // Creamos nuevas instancias al restaurar para evitar referencias compartidas
            cliente.getCuotas().add(new Cuota(c.getNumeroCuota(), c.getMontoOriginal(), c.getMontoPagado(), c.isFaltante()));
        }
    }
}

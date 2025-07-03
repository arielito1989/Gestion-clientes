package com.inventario.commands;

import com.inventario.models.Cliente;
import java.util.List;

// Comando para agregar un cliente.
public class AgregarClienteCommand implements Command {
    private List<Cliente> clientes; // Referencia a la lista de clientes principal
    private Cliente clienteAgregado; // El cliente que se va a agregar/eliminar

    public AgregarClienteCommand(List<Cliente> clientes, Cliente clienteAgregado) {
        this.clientes = clientes;
        this.clienteAgregado = clienteAgregado;
    }

    @Override
    public void execute() {
        // Al ejecutar, simplemente añade el cliente a la lista.
        clientes.add(clienteAgregado);
    }

    @Override
    public void undo() {
        // Al deshacer, elimina el cliente de la lista.
        clientes.remove(clienteAgregado);
    }
}

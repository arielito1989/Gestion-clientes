package com.inventario.commands;

import com.inventario.models.Cliente;
import java.util.List;

// Comando para eliminar un cliente.
public class EliminarClienteCommand implements Command {
    private List<Cliente> clientes; // Referencia a la lista de clientes principal
    private Cliente clienteEliminado; // El cliente que se eliminó
    private int indiceEliminado; // El índice donde se eliminó el cliente (para reinsertarlo)

    public EliminarClienteCommand(List<Cliente> clientes, Cliente clienteEliminado, int indiceEliminado) {
        this.clientes = clientes;
        this.clienteEliminado = clienteEliminado;
        this.indiceEliminado = indiceEliminado;
    }

    @Override
    public void execute() {
        // Al ejecutar, elimina el cliente de la lista.
        clientes.remove(clienteEliminado);
    }

    @Override
    public void undo() {
        // Al deshacer, reinserta el cliente en su posición original.
        // Se asegura de que el índice sea válido si la lista ha cambiado de tamaño.
        if (indiceEliminado >= 0 && indiceEliminado <= clientes.size()) {
            clientes.add(indiceEliminado, clienteEliminado);
        } else {
            // Si el índice no es válido (ej. la lista se encogió mucho), simplemente añadir al final.
            clientes.add(clienteEliminado);
        }
    }
}

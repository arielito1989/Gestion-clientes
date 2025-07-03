package com.inventario.commands;

// Interfaz para el patrón Command.
// Cada acción que pueda ser deshecha implementará esta interfaz.
public interface Command {
    /**
     * Ejecuta la acción definida por el comando.
     */
    void execute();

    /**
     * Deshace la acción previamente ejecutada por este comando.
     */
    void undo();
}

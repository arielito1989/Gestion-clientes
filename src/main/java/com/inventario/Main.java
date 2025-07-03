package com.inventario;

import com.inventario.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Asegurarse de que la interfaz de usuario se ejecute en el Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar el Look and Feel de Nimbus para una apariencia moderna
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {
                // Si Nimbus no está disponible o falla, usar el Look and Feel por defecto del sistema
                System.err.println("No se pudo establecer el Look and Feel de Nimbus. Usando el predeterminado.");
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ex) {
                    System.err.println("Error al establecer el Look and Feel del sistema: " + ex.getMessage());
                }
            }

            // Crear y mostrar la ventana principal de la aplicación
            MainWindow mainWindow = new MainWindow();
            mainWindow.setVisible(true);
        });
    }
}

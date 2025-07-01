package com.inventario; // Este es el paquete correcto para este Main.java

import com.inventario.ui.MainWindow;
import javax.swing.SwingUtilities;
import javax.swing.UIManager; // ¡Importante: Necesitas importar UIManager!

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // --- INICIO: Código para establecer el Look and Feel de Nimbus ---
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break; // Salir del bucle una vez que Nimbus ha sido encontrado y establecido
                    }
                }
            } catch (Exception e) {
                // Si ocurre un error al establecer Nimbus (ej. no está disponible),
                // la aplicación continuará con el Look and Feel predeterminado del sistema.
                System.err.println("No se pudo establecer el Look and Feel de Nimbus. Usando el predeterminado. " + e.getMessage());
            }
            // --- FIN: Código para establecer el Look and Feel de Nimbus ---

            MainWindow app = new MainWindow();
            app.setVisible(true);
        });
    }
}

package com.inventario.utils;

import com.inventario.models.Cliente;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelExporter {

    public static void exportarClientes(List<Cliente> clientes, String rutaArchivo) {
        Workbook workbook = new XSSFWorkbook();
        Sheet hoja = workbook.createSheet("Clientes");

        // Cabecera
        Row cabecera = hoja.createRow(0);
        cabecera.createCell(0).setCellValue("Nombre");
        cabecera.createCell(1).setCellValue("DNI");
        cabecera.createCell(2).setCellValue("Tipo de Cuota");
        cabecera.createCell(3).setCellValue("Total Producto");
        cabecera.createCell(4).setCellValue("Pagado");
        cabecera.createCell(5).setCellValue("Deuda");

        // Cuerpo
        int rowNum = 1;
        for (Cliente c : clientes) {
            Row fila = hoja.createRow(rowNum++);
            fila.createCell(0).setCellValue(c.getNombre());
            fila.createCell(1).setCellValue(c.getDni());
            fila.createCell(2).setCellValue(c.getTipoCuota());
            fila.createCell(3).setCellValue(c.getTotalProducto());
            fila.createCell(4).setCellValue(c.getTotalPagado());
            fila.createCell(5).setCellValue(c.calcularDeudaRestante());
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < 6; i++) {
            hoja.autoSizeColumn(i);
        }

        // Guardar archivo
        try (FileOutputStream out = new FileOutputStream(rutaArchivo)) {
            workbook.write(out);
            workbook.close();
            System.out.println("📁 Archivo exportado: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("❌ Error al exportar: " + e.getMessage());
        }
    }
}

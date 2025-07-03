package com.inventario.utils;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Formato para guardar/leer fechas

    // --- Método para EXPORTAR clientes (asegúrate de que guarde el apellido y tipoCuota) ---
    public static void exportarClientes(List<Cliente> clientes, String filePath) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Clientes");

            // Crear cabecera
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Nombre", "Apellido", "DNI", "Tipo Cuota", "Total Producto", "Cuotas (JSON)"}; // Añadir Apellido y Tipo Cuota
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Llenar datos
            int rowNum = 1;
            for (Cliente cliente : clientes) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(cliente.getNombre());
                row.createCell(1).setCellValue(cliente.getApellido()); // Guardar apellido
                row.createCell(2).setCellValue(cliente.getDni());
                row.createCell(3).setCellValue(cliente.getTipoCuota()); // Guardar tipoCuota
                row.createCell(4).setCellValue(cliente.getTotalProducto());

                // Serializar la lista de cuotas a JSON para guardarla en una celda
                // Esto es una simplificación, necesitarías una librería JSON como Gson o Jackson
                // Para simplificar el ejemplo, asumiré un formato simple o que ya tienes tu propia lógica de serialización.
                StringBuilder cuotasJson = new StringBuilder("[");
                for (Cuota cuota : cliente.getCuotas()) {
                    cuotasJson.append("{");
                    cuotasJson.append("\"numeroCuota\":").append(cuota.getNumeroCuota()).append(",");
                    cuotasJson.append("\"montoOriginal\":").append(cuota.getMontoOriginal()).append(",");
                    cuotasJson.append("\"montoPagado\":").append(cuota.getMontoPagado()).append(",");
                    cuotasJson.append("\"fechaVencimiento\":\"").append(cuota.getFechaVencimiento() != null ? cuota.getFechaVencimiento().format(DATE_FORMATTER) : "").append("\",");
                    cuotasJson.append("\"fechaPago\":\"").append(cuota.getFechaPago() != null ? cuota.getFechaPago().format(DATE_FORMATTER) : "").append("\",");
                    cuotasJson.append("\"isFaltante\":").append(cuota.isFaltante());
                    cuotasJson.append("},");
                }
                if (cliente.getCuotas().size() > 0) {
                    cuotasJson.deleteCharAt(cuotasJson.length() - 1); // Eliminar la última coma
                }
                cuotasJson.append("]");
                row.createCell(5).setCellValue(cuotasJson.toString()); // Guardar cuotas como JSON

            }

            try (FileOutputStream outputStream = new FileOutputStream(filePath)) {
                workbook.write(outputStream);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al exportar clientes a Excel: " + e.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
        }
    }


    // --- Método para IMPORTAR clientes (AQUÍ ES DONDE NECESITAS EL CAMBIO) ---
    public static List<Cliente> importarClientes(String filePath) {
        List<Cliente> clientes = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter(); // Para leer celdas como String

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Saltar la fila de cabecera

                // Asegúrate de que las celdas no sean nulas antes de intentar leerlas
                Cell nombreCell = row.getCell(0);
                Cell apellidoCell = row.getCell(1); // Celda para apellido
                Cell dniCell = row.getCell(2);
                Cell tipoCuotaCell = row.getCell(3); // Celda para tipoCuota
                Cell totalProductoCell = row.getCell(4);
                Cell cuotasJsonCell = row.getCell(5); // Celda para cuotas JSON

                // Validar que las celdas principales no sean nulas
                if (nombreCell == null || apellidoCell == null || dniCell == null || tipoCuotaCell == null || totalProductoCell == null || cuotasJsonCell == null) {
                    System.err.println("Advertencia: Fila incompleta detectada en Excel, saltando fila " + row.getRowNum());
                    continue;
                }

                String nombre = formatter.formatCellValue(nombreCell);
                String apellido = formatter.formatCellValue(apellidoCell); // Leer apellido
                String dni = formatter.formatCellValue(dniCell);
                String tipoCuota = formatter.formatCellValue(tipoCuotaCell); // LEER TIPO_CUOTA AQUÍ
                double totalProducto = totalProductoCell.getNumericCellValue();

                // Deserializar la lista de cuotas desde JSON
                // Esto es una simplificación, necesitarías una librería JSON como Gson o Jackson
                // O tu propia lógica de parsing si no usas librerías externas.
                List<Cuota> cuotas = new ArrayList<>();
                try {
                    String cuotasJson = formatter.formatCellValue(cuotasJsonCell);
                    // Lógica de parsing de JSON aquí.
                    // Por ejemplo, si el JSON es simple como el que exportamos:
                    // Esto es un PARSER MUY BÁSICO y frágil, solo para ilustrar.
                    // ¡Es altamente recomendable usar una librería JSON real!
                    if (cuotasJson.startsWith("[") && cuotasJson.endsWith("]")) {
                        String content = cuotasJson.substring(1, cuotasJson.length() - 1);
                        String[] cuotaStrings = content.split("\\},\\{"); // Divide por }, {
                        for (String cs : cuotaStrings) {
                            if (cs.isEmpty()) continue;
                            if (!cs.startsWith("{")) cs = "{" + cs;
                            if (!cs.endsWith("}")) cs = cs + "}";

                            // Parsing rudimentario de cada cuota
                            int numeroCuota = Integer.parseInt(extractValue(cs, "numeroCuota"));
                            double montoOriginal = Double.parseDouble(extractValue(cs, "montoOriginal"));
                            double montoPagado = Double.parseDouble(extractValue(cs, "montoPagado"));
                            String fechaVencStr = extractValue(cs, "fechaVencimiento");
                            String fechaPagoStr = extractValue(cs, "fechaPago");
                            boolean isFaltante = Boolean.parseBoolean(extractValue(cs, "isFaltante"));

                            LocalDate fechaVencimiento = fechaVencStr.isEmpty() ? null : LocalDate.parse(fechaVencStr, DATE_FORMATTER);
                            LocalDate fechaPago = fechaPagoStr.isEmpty() ? null : LocalDate.parse(fechaPagoStr, DATE_FORMATTER);

                            cuotas.add(new Cuota(numeroCuota, montoOriginal, montoPagado, fechaVencimiento, fechaPago, isFaltante));
                        }
                    }
                } catch (Exception jsonE) {
                    System.err.println("Error al deserializar cuotas para cliente " + nombre + ": " + jsonE.getMessage());
                    // Puedes optar por saltar este cliente o inicializar cuotas vacías
                    cuotas = new ArrayList<>();
                }

                // LLAMAR AL CONSTRUCTOR CORRECTO AHORA
                clientes.add(new Cliente(nombre, apellido, dni, tipoCuota, totalProducto, cuotas)); // ¡Ahora con tipoCuota!
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error al importar clientes desde Excel: " + e.getMessage(), "Error de Importación", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        return clientes;
    }

    // Método auxiliar para el parsing rudimentario de JSON
    private static String extractValue(String json, String key) {
        String search = "\"" + key + "\":";
        int startIndex = json.indexOf(search);
        if (startIndex == -1) return "";
        startIndex += search.length();
        char firstChar = json.charAt(startIndex);
        if (firstChar == '"') { // Es un String
            startIndex++;
            int endIndex = json.indexOf("\"", startIndex);
            return json.substring(startIndex, endIndex);
        } else { // Es un número o booleano
            int endIndex = json.indexOf(",", startIndex);
            if (endIndex == -1) endIndex = json.indexOf("}", startIndex);
            return json.substring(startIndex, endIndex).trim();
        }
    }
}

package com.inventario.utils;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.JOptionPane; // Importar JOptionPane
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExcelExporter {

    private static final String DEFAULT_FILE_PATH = "clientes.xlsx";
    private static final String CLIENTES_SHEET_NAME = "Clientes";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Exporta la lista de clientes a un archivo Excel con una sola hoja:
     * "Clientes" donde se incluye el resumen y el detalle de cuotas en formato JSON.
     * @param clientes La lista de objetos Cliente a exportar.
     * @param rutaArchivo La ruta completa del archivo Excel de salida.
     */
    public static void exportarClientes(List<Cliente> clientes, String rutaArchivo) {
        Workbook workbook = new XSSFWorkbook();

        // --- Hoja de Clientes (Resumen y Cuotas JSON) ---
        Sheet clientesSheet = workbook.createSheet(CLIENTES_SHEET_NAME);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Cabecera para la hoja de Clientes (Añadido "Producto" y "Adelanto Acumulado")
        Row clientesHeader = clientesSheet.createRow(0);
        String[] clientesHeaders = {
                "Nombre", "Apellido", "DNI", "Tipo de Cuota", "Producto", "Total Producto",
                "Adelanto Acumulado", "Cuotas (JSON)" // Cuotas ahora en formato JSON
        };
        for (int i = 0; i < clientesHeaders.length; i++) {
            Cell cell = clientesHeader.createCell(i);
            cell.setCellValue(clientesHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Cuerpo para la hoja de Clientes (Ajustados índices de celdas)
        int rowNum = 1;
        for (Cliente cliente : clientes) {
            Row row = clientesSheet.createRow(rowNum++);
            row.createCell(0).setCellValue(cliente.getNombre());
            row.createCell(1).setCellValue(cliente.getApellido());
            row.createCell(2).setCellValue(cliente.getDni());
            row.createCell(3).setCellValue(cliente.getTipoCuota());
            row.createCell(4).setCellValue(cliente.getProducto()); // Escribir el producto (índice 4)
            row.createCell(5).setCellValue(cliente.getTotalProducto()); // Total Producto (índice 5)
            row.createCell(6).setCellValue(cliente.getAdelantoAcumulado()); // Adelanto Acumulado (índice 6)

            // Serializar la lista de cuotas a JSON para guardarla en una celda (índice 7)
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
            row.createCell(7).setCellValue(cuotasJson.toString()); // Cuotas (JSON)
        }

        // Ajustar ancho de columnas para la hoja de Clientes
        for (int i = 0; i < clientesHeaders.length; i++) {
            clientesSheet.autoSizeColumn(i);
        }

        // Guardar archivo
        try (FileOutputStream out = new FileOutputStream(rutaArchivo)) {
            workbook.write(out);
            workbook.close();
            System.out.println("📁 Archivo exportado: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("❌ Error al exportar: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al exportar clientes a Excel: " + e.getMessage(), "Error de Exportación", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Importa la lista de clientes desde un archivo Excel con una sola hoja (Clientes)
     * donde las cuotas están serializadas en JSON.
     * @param rutaArchivo La ruta completa del archivo Excel de entrada.
     * @return Una lista de objetos Cliente leídos del Excel.
     */
    public static List<Cliente> importarClientes(String rutaArchivo) {
        List<Cliente> clientes = new ArrayList<>();
        File excelFile = new File(rutaArchivo);

        if (!excelFile.exists()) {
            System.out.println("ℹ️ El archivo Excel no existe. Se iniciará con una lista de clientes vacía.");
            return clientes;
        }

        try (FileInputStream fis = new FileInputStream(excelFile);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // --- Importar Clientes desde la Hoja "Clientes" ---
            Sheet clientesSheet = workbook.getSheet(CLIENTES_SHEET_NAME);
            if (clientesSheet == null) {
                System.err.println("❌ Hoja '" + CLIENTES_SHEET_NAME + "' no encontrada en el archivo Excel. Se iniciará con lista vacía.");
                return new ArrayList<>();
            }

            DataFormatter formatter = new DataFormatter(); // Para leer celdas como String

            for (int rowNum = 1; rowNum <= clientesSheet.getLastRowNum(); rowNum++) {
                Row fila = clientesSheet.getRow(rowNum);
                if (fila == null) {
                    continue;
                }

                // Asegúrate de que las celdas no sean nulas antes de intentar leerlas
                // Ajustados los índices para leer Producto y Adelanto Acumulado
                Cell nombreCell = fila.getCell(0);
                Cell apellidoCell = fila.getCell(1);
                Cell dniCell = fila.getCell(2);
                Cell tipoCuotaCell = fila.getCell(3);
                Cell productoCell = fila.getCell(4); // Celda para producto (índice 4)
                Cell totalProductoCell = fila.getCell(5); // Celda para totalProducto (índice 5)
                Cell adelantoAcumuladoCell = fila.getCell(6); // Celda para adelantoAcumulado (índice 6)
                Cell cuotasJsonCell = fila.getCell(7); // Celda para cuotas JSON (índice 7)

                // Validar que las celdas principales no sean nulas
                if (nombreCell == null || apellidoCell == null || dniCell == null || tipoCuotaCell == null ||
                        productoCell == null || totalProductoCell == null || adelantoAcumuladoCell == null || cuotasJsonCell == null) {
                    System.err.println("Advertencia: Fila incompleta detectada en Excel, saltando fila " + rowNum);
                    continue;
                }

                String nombre = formatter.formatCellValue(nombreCell);
                String apellido = formatter.formatCellValue(apellidoCell);
                String dni = formatter.formatCellValue(dniCell);
                String tipoCuota = formatter.formatCellValue(tipoCuotaCell);
                String producto = formatter.formatCellValue(productoCell); // Leer producto
                double totalProducto = getNumericCellValue(totalProductoCell);
                double adelantoAcumulado = getNumericCellValue(adelantoAcumuladoCell); // Leer adelanto acumulado

                // Deserializar la lista de cuotas desde JSON
                List<Cuota> cuotas = new ArrayList<>();
                try {
                    String cuotasJson = formatter.formatCellValue(cuotasJsonCell);
                    // Lógica de parsing de JSON aquí (la misma que ya tenías)
                    if (cuotasJson.startsWith("[") && cuotasJson.endsWith("]")) {
                        String content = cuotasJson.substring(1, cuotasJson.length() - 1);
                        String[] cuotaStrings = content.split("\\},\\{");
                        for (String cs : cuotaStrings) {
                            if (cs.isEmpty()) continue;
                            if (!cs.startsWith("{")) cs = "{" + cs;
                            if (!cs.endsWith("}")) cs = cs + "}";

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
                    cuotas = new ArrayList<>();
                }

                // LLAMAR AL CONSTRUCTOR CORRECTO AHORA (con producto y adelantoAcumulado)
                clientes.add(new Cliente(nombre, apellido, dni, tipoCuota, producto, totalProducto, cuotas, adelantoAcumulado));
            }

            System.out.println("✅ Clientes y detalles de cuotas importados desde: " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("❌ Error al importar clientes desde Excel: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error al importar clientes desde Excel: " + e.getMessage(), "Error de Importación", JOptionPane.ERROR_MESSAGE);
            return null;
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al leer el archivo Excel: " + e.getMessage());
            JOptionPane.showMessageDialog(null, "Error inesperado al leer el archivo Excel: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
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

    private static String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    if (cell.getCachedFormulaResultType() == CellType.NUMERIC) {
                        return String.valueOf(cell.getNumericCellValue());
                    } else {
                        return cell.getStringCellValue();
                    }
                } catch (IllegalStateException e) {
                    return cell.getStringCellValue();
                }
            case BLANK:
                return "";
            default:
                return "";
        }
    }

    private static double getNumericCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0.0;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            try {
                return Double.parseDouble(cell.getStringCellValue());
            } catch (NumberFormatException e) {
                System.err.println("Advertencia: No se pudo parsear el valor numérico de la celda: '" + cell.getStringCellValue() + "'");
                return 0.0;
            }
        }
        return 0.0;
    }

    private static boolean getBooleanCellValue(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return false;
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return cell.getBooleanCellValue();
        } else if (cell.getCellType() == CellType.STRING) {
            return Boolean.parseBoolean(cell.getStringCellValue());
        }
        return false;
    }
}

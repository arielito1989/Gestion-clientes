package com.inventario.utils;

import com.inventario.models.Cliente;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelExporter {

    private static final String DEFAULT_FILE_PATH = "clientes.xlsx";

    /**
     * Exporta la lista de clientes a un archivo Excel.
     * Si el archivo no existe, lo crea. Si existe, lo sobrescribe.
     * @param clientes La lista de objetos Cliente a exportar.
     * @param rutaArchivo La ruta completa del archivo Excel de salida.
     */
    public static void exportarClientes(List<Cliente> clientes, String rutaArchivo) {
        Workbook workbook = new XSSFWorkbook();
        Sheet hoja = workbook.createSheet("Clientes");

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Cabecera actualizada para incluir los nuevos campos de cuotas
        Row cabecera = hoja.createRow(0);
        String[] headers = {"Nombre", "DNI", "Tipo de Cuota", "Total Producto", "Total Cuotas", "Valor Cuota", "Cuotas Pagadas", "Monto Pagado", "Deuda Restante"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = cabecera.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Cuerpo
        int rowNum = 1;
        for (Cliente c : clientes) {
            Row fila = hoja.createRow(rowNum++);
            fila.createCell(0).setCellValue(c.getNombre());
            fila.createCell(1).setCellValue(c.getDni());
            fila.createCell(2).setCellValue(c.getTipoCuota());
            fila.createCell(3).setCellValue(c.getTotalProducto());
            fila.createCell(4).setCellValue(c.getTotalCuotas()); // Nuevo: Total de cuotas
            fila.createCell(5).setCellValue(c.getValorCuota()); // Nuevo: Valor por cuota
            fila.createCell(6).setCellValue(c.getCuotasPagadasCount()); // Nuevo: Cuotas pagadas (contador)
            fila.createCell(7).setCellValue(c.getTotalPagado()); // Monto total pagado
            fila.createCell(8).setCellValue(c.calcularDeudaRestante()); // Deuda restante
        }

        // Ajustar ancho de columnas
        for (int i = 0; i < headers.length; i++) {
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

    /**
     * Importa la lista de clientes desde un archivo Excel.
     * Si el archivo no existe o está vacío, devuelve una lista vacía.
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

            Sheet hoja = workbook.getSheetAt(0);

            // Iterar sobre las filas, omitiendo la cabecera (fila 0)
            for (int rowNum = 1; rowNum <= hoja.getLastRowNum(); rowNum++) {
                Row fila = hoja.getRow(rowNum);
                if (fila == null) {
                    continue;
                }

                // Leer los datos de cada celda, incluyendo los nuevos campos de cuotas
                String nombre = getCellValue(fila.getCell(0));
                String dni = getCellValue(fila.getCell(1));
                String tipoCuota = getCellValue(fila.getCell(2));
                double totalProducto = getNumericCellValue(fila.getCell(3));
                int totalCuotas = (int) getNumericCellValue(fila.getCell(4)); // Nuevo: Total de cuotas
                double valorCuota = getNumericCellValue(fila.getCell(5)); // Nuevo: Valor por cuota
                int cuotasPagadasCount = (int) getNumericCellValue(fila.getCell(6)); // Nuevo: Cuotas pagadas (contador)
                double totalPagado = getNumericCellValue(fila.getCell(7)); // Monto total pagado

                // Usar el nuevo constructor de Cliente para cargar todos los datos
                Cliente cliente = new Cliente(nombre, dni, tipoCuota, totalProducto, totalCuotas, valorCuota, cuotasPagadasCount, totalPagado);

                clientes.add(cliente);
            }
            System.out.println("✅ Clientes importados desde: " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("❌ Error al importar clientes desde Excel: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al leer el archivo Excel: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
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
                    // Para números que pueden ser DNI, asegurarse de que no se trunquen decimales si no los tienen
                    return String.valueOf(cell.getNumericCellValue()).replaceAll("\\.0$", "");
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
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
                System.err.println("Advertencia: No se pudo parsear el valor numérico de la celda: " + cell.getStringCellValue());
                return 0.0;
            }
        }
        return 0.0;
    }
}

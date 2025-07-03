package com.inventario.utils;

import com.inventario.models.Cliente;
import com.inventario.models.Cuota;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors; // Para usar Collectors.toMap

public class ExcelExporter {

    private static final String DEFAULT_FILE_PATH = "clientes.xlsx";
    private static final String CLIENTES_SHEET_NAME = "Clientes";
    private static final String CUOTAS_SHEET_NAME = "DetalleCuotas";

    /**
     * Exporta la lista de clientes a un archivo Excel con dos hojas:
     * "Clientes" para el resumen y "DetalleCuotas" para el detalle de cada cuota.
     * @param clientes La lista de objetos Cliente a exportar.
     * @param rutaArchivo La ruta completa del archivo Excel de salida.
     */
    public static void exportarClientes(List<Cliente> clientes, String rutaArchivo) {
        Workbook workbook = new XSSFWorkbook();

        // --- Hoja de Clientes (Resumen) ---
        Sheet clientesSheet = workbook.createSheet(CLIENTES_SHEET_NAME);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        // CORRECCIÓN AQUÍ: Eliminar el argumento 'workbook' de createCellStyle()
        CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerCellStyle.setAlignment(HorizontalAlignment.CENTER);

        // Cabecera para la hoja de Clientes
        Row clientesHeader = clientesSheet.createRow(0);
        String[] clientesHeaders = {
                "Nombre", "DNI", "Tipo de Cuota", "Total Producto",
                "Total Cuotas", "Cuotas Pagadas", "Cuotas Pendientes", "Valor Cuota Promedio",
                "Monto Pagado Total", "Deuda Restante Total"
        };
        for (int i = 0; i < clientesHeaders.length; i++) {
            Cell cell = clientesHeader.createCell(i);
            cell.setCellValue(clientesHeaders[i]);
            cell.setCellStyle(headerCellStyle);
        }

        // Cuerpo para la hoja de Clientes
        int clienteRowNum = 1;
        for (Cliente c : clientes) {
            Row fila = clientesSheet.createRow(clienteRowNum++);
            fila.createCell(0).setCellValue(c.getNombre());
            fila.createCell(1).setCellValue(c.getDni());
            fila.createCell(2).setCellValue(c.getTipoCuota());
            fila.createCell(3).setCellValue(c.getTotalProducto());
            fila.createCell(4).setCellValue(c.getTotalCuotas());
            fila.createCell(5).setCellValue(c.getCuotasPagadasCount());
            fila.createCell(6).setCellValue(c.getCuotasRestantes());
            fila.createCell(7).setCellValue(c.getValorCuota());
            fila.createCell(8).setCellValue(c.getTotalPagado());
            fila.createCell(9).setCellValue(c.calcularDeudaRestante());
        }

        // Ajustar ancho de columnas para la hoja de Clientes
        for (int i = 0; i < clientesHeaders.length; i++) {
            clientesSheet.autoSizeColumn(i);
        }

        // --- Hoja de Detalle de Cuotas ---
        Sheet cuotasSheet = workbook.createSheet(CUOTAS_SHEET_NAME);

        // Cabecera para la hoja de Detalle de Cuotas
        Row cuotasHeader = cuotasSheet.createRow(0);
        String[] cuotasHeaders = {
                "DNI_Cliente", "Numero_Cuota", "Monto_Original", "Monto_Pagado", "Monto_Restante", "Es_Faltante"
        };
        for (int i = 0; i < cuotasHeaders.length; i++) {
            Cell cell = cuotasHeader.createCell(i);
            cell.setCellValue(cuotasHeaders[i]);
            cell.setCellStyle(headerCellStyle); // Reutiliza el mismo estilo de cabecera
        }

        // Cuerpo para la hoja de Detalle de Cuotas
        int cuotaRowNum = 1;
        for (Cliente c : clientes) {
            for (Cuota cuota : c.getCuotas()) {
                Row fila = cuotasSheet.createRow(cuotaRowNum++);
                fila.createCell(0).setCellValue(c.getDni()); // DNI para vincular
                fila.createCell(1).setCellValue(cuota.getNumeroCuota());
                fila.createCell(2).setCellValue(cuota.getMontoOriginal());
                fila.createCell(3).setCellValue(cuota.getMontoPagado());
                fila.createCell(4).setCellValue(cuota.getMontoRestante());
                fila.createCell(5).setCellValue(cuota.isFaltante());
            }
        }

        // Ajustar ancho de columnas para la hoja de Detalle de Cuotas
        for (int i = 0; i < cuotasHeaders.length; i++) {
            cuotasSheet.autoSizeColumn(i);
        }

        // Guardar archivo
        try (FileOutputStream out = new FileOutputStream(rutaArchivo)) {
            workbook.write(out);
            workbook.close();
            System.out.println("📁 Archivo exportado: " + rutaArchivo);
        } catch (IOException e) {
            System.err.println("❌ Error al exportar: " + e.getMessage());
            // Podrías lanzar una excepción o mostrar un JOptionPane aquí si quisieras notificar al usuario
        }
    }

    /**
     * Importa la lista de clientes desde un archivo Excel con dos hojas.
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

            for (int rowNum = 1; rowNum <= clientesSheet.getLastRowNum(); rowNum++) {
                Row fila = clientesSheet.getRow(rowNum);
                if (fila == null) {
                    continue;
                }

                String nombre = getCellValue(fila.getCell(0));
                String dni = getCellValue(fila.getCell(1));
                String tipoCuota = getCellValue(fila.getCell(2));
                double totalProducto = getNumericCellValue(fila.getCell(3));

                // Creamos el cliente con una lista de cuotas vacía por ahora
                clientes.add(new Cliente(nombre, dni, tipoCuota, totalProducto, new ArrayList<>()));
            }

            // --- Importar Detalle de Cuotas desde la Hoja "DetalleCuotas" ---
            Sheet cuotasSheet = workbook.getSheet(CUOTAS_SHEET_NAME);
            if (cuotasSheet == null) {
                System.err.println("❌ Hoja '" + CUOTAS_SHEET_NAME + "' no encontrada en el archivo Excel. Los clientes no tendrán detalle de cuotas.");
                return clientes; // Devolver clientes sin detalle de cuotas si la hoja no existe
            }

            // Mapear clientes por DNI para asignar cuotas fácilmente
            java.util.Map<String, Cliente> clientesMap = clientes.stream()
                    .collect(Collectors.toMap(Cliente::getDni, cliente -> cliente));


            for (int rowNum = 1; rowNum <= cuotasSheet.getLastRowNum(); rowNum++) {
                Row fila = cuotasSheet.getRow(rowNum);
                if (fila == null) {
                    continue;
                }

                String dniCliente = getCellValue(fila.getCell(0));
                Cliente cliente = clientesMap.get(dniCliente);

                if (cliente != null) {
                    int numeroCuota = (int) getNumericCellValue(fila.getCell(1));
                    double montoOriginal = getNumericCellValue(fila.getCell(2));
                    double montoPagado = getNumericCellValue(fila.getCell(3));
                    boolean isFaltante = getBooleanCellValue(fila.getCell(5));

                    cliente.getCuotas().add(new Cuota(numeroCuota, montoOriginal, montoPagado, isFaltante));
                } else {
                    System.err.println("Advertencia: Cuota encontrada para DNI " + dniCliente + " pero el cliente no existe en la hoja principal.");
                }
            }
            System.out.println("✅ Clientes y detalles de cuotas importados desde: " + rutaArchivo);

        } catch (IOException e) {
            System.err.println("❌ Error al importar clientes desde Excel: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Error inesperado al leer el archivo Excel: " + e.getMessage());
            e.printStackTrace();
        }
        return clientes;
    }

    // Métodos auxiliares para obtener valores de celdas de forma segura
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

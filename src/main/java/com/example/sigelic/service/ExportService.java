package com.example.sigelic.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Servicio para exportación de reportes a diferentes formatos
 */
@Service
@Slf4j
public class ExportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Exporta datos a Excel
     */
    public byte[] exportToExcel(String title, List<String> headers, List<List<String>> data) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(title);
            
            // Estilo para el título
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            
            // Estilo para headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            
            // Estilo para datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            
            int rowNum = 0;
            
            // Título
            Row titleRow = sheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(title);
            titleCell.setCellStyle(titleStyle);
            
            // Fecha de generación
            Row dateRow = sheet.createRow(rowNum++);
            Cell dateCell = dateRow.createCell(0);
            dateCell.setCellValue("Generado el: " + LocalDateTime.now().format(DATE_FORMATTER));
            
            // Línea vacía
            rowNum++;
            
            // Headers
            Row headerRow = sheet.createRow(rowNum++);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }
            
            // Data
            for (List<String> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.size(); i++) {
                    Cell cell = row.createCell(i);
                    cell.setCellValue(rowData.get(i) != null ? rowData.get(i) : "");
                    cell.setCellStyle(dataStyle);
                }
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
            
        } catch (Exception e) {
            log.error("Error exportando a Excel: ", e);
            throw new RuntimeException("Error generando Excel", e);
        }
    }

    /**
     * Exporta datos a CSV (como alternativa a PDF)
     */
    public byte[] exportToCsv(String title, List<String> headers, List<List<String>> data) {
        try {
            StringBuilder csv = new StringBuilder();
            
            // Título
            csv.append(title).append("\n");
            csv.append("Generado el: ").append(LocalDateTime.now().format(DATE_FORMATTER)).append("\n\n");
            
            // Headers
            csv.append(String.join(",", headers)).append("\n");
            
            // Data
            for (List<String> row : data) {
                csv.append(row.stream()
                    .map(cell -> cell != null ? "\"" + cell.replace("\"", "\"\"") + "\"" : "\"\"")
                    .reduce((a, b) -> a + "," + b)
                    .orElse(""))
                    .append("\n");
            }
            
            return csv.toString().getBytes("UTF-8");
            
        } catch (Exception e) {
            log.error("Error exportando a CSV: ", e);
            throw new RuntimeException("Error generando CSV", e);
        }
    }

    /**
     * Convierte un mapa de estadísticas a formato de tabla para exportación
     */
    public List<List<String>> convertStatsToTable(Map<String, Object> stats) {
        return stats.entrySet().stream()
                .map(entry -> List.of(
                    formatKey(entry.getKey()),
                    formatValue(entry.getValue())
                ))
                .toList();
    }

    /**
     * Exporta datos a PDF
     */
    public byte[] exportToPdf(String title, List<String> headers, List<List<String>> data) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            
            // Título del reporte
            Paragraph titleParagraph = new Paragraph(title)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(16)
                    .setBold()
                    .setMarginBottom(20);
            document.add(titleParagraph);
            
            // Fecha de generación
            Paragraph dateParagraph = new Paragraph("Generado el: " + LocalDateTime.now().format(DATE_FORMATTER))
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(dateParagraph);
            
            if (!headers.isEmpty() && !data.isEmpty()) {
                // Crear tabla
                Table table = new Table(UnitValue.createPercentArray(headers.size()))
                        .setWidth(UnitValue.createPercentValue(100));
                
                // Agregar headers
                for (String header : headers) {
                    Cell headerCell = new Cell()
                            .add(new Paragraph(header))
                            .setBold()
                            .setTextAlignment(TextAlignment.CENTER)
                            .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY);
                    table.addHeaderCell(headerCell);
                }
                
                // Agregar datos
                for (List<String> row : data) {
                    for (String cellValue : row) {
                        Cell cell = new Cell()
                                .add(new Paragraph(cellValue != null ? cellValue : ""))
                                .setTextAlignment(TextAlignment.LEFT);
                        table.addCell(cell);
                    }
                }
                
                document.add(table);
            } else {
                document.add(new Paragraph("No hay datos para mostrar"));
            }
            
            document.close();
            return outputStream.toByteArray();
            
        } catch (Exception e) {
            log.error("Error generando PDF", e);
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    private String formatKey(String key) {
        // Convierte camelCase a formato legible
        return key.replaceAll("([a-z])([A-Z])", "$1 $2")
                  .substring(0, 1).toUpperCase() + 
                  key.replaceAll("([a-z])([A-Z])", "$1 $2").substring(1);
    }

    private String formatValue(Object value) {
        if (value == null) return "";
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_FORMATTER);
        }
        return value.toString();
    }
}

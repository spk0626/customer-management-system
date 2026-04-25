package com.customermgmt.util;

import org.apache.poi.xssf.model.SharedStrings;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * SAX content handler for XLSX files.
 *
 * Why SAX and not XSSFWorkbook?
 *   XSSFWorkbook loads the entire workbook into memory (DOM).
 *   For 1,000,000 rows that can mean 500MB–2GB of heap.
 *   SAX processes the XML as a stream — memory stays bounded to
 *   roughly one row at a time, regardless of file size.
 *
 * The handler fires the rowConsumer callback once per data row.
 * Each row is a String[] of cell values in column order.
 */
public class ExcelRowHandler extends DefaultHandler {

    private final SharedStrings sharedStrings;
    private final StylesTable styles;
    private final Consumer<String[]> rowConsumer;

    private final StringBuilder cellValue = new StringBuilder();
    private final List<String> currentRow = new ArrayList<>();

    private boolean inlineString = false;
    private boolean isSharedString = false;

    public ExcelRowHandler(SharedStrings sharedStrings,
                           StylesTable styles,
                           Consumer<String[]> rowConsumer) {
        this.sharedStrings = sharedStrings;
        this.styles = styles;
        this.rowConsumer = rowConsumer;
    }

    @Override
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) throws SAXException {
        if ("c".equals(qName)) {
            // Determine cell type
            String type = attributes.getValue("t");
            isSharedString = "s".equals(type);
            inlineString = "inlineStr".equals(type);
            cellValue.setLength(0);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if ("v".equals(qName) || ("is".equals(qName) && inlineString)) {
            String raw = cellValue.toString().trim();
            if (isSharedString) {
                int idx = Integer.parseInt(raw);
                currentRow.add(new XSSFRichTextString(
                    sharedStrings.getItemAt(idx).toString()).getString());
            } else {
                currentRow.add(raw);
            }
        } else if ("row".equals(qName)) {
            if (!currentRow.isEmpty()) {
                rowConsumer.accept(currentRow.toArray(new String[0]));
                currentRow.clear();
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) {
        cellValue.append(ch, start, length);
    }
}

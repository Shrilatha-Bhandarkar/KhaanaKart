package com.onlinefoodorder.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.onlinefoodorder.entity.Order;
import com.onlinefoodorder.entity.Payment;

@Component
public class PdfGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PdfGenerator.class);

    // ✅ Set Invoice Directory Path
    private static final String INVOICE_DIR = "C:\\Users\\91938\\OneDrive\\Desktop\\Incture\\Project\\Final Project\\Invoices";

    public String generateInvoice(Order order, Payment payment) {
        // Ensure the directory exists
        new File(INVOICE_DIR).mkdirs();

        // Define Invoice File Name
        String invoiceFileName = "Invoice_" + payment.getTransactionId() + ".pdf";
        String invoicePath = INVOICE_DIR + File.separator + invoiceFileName;

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.setLeading(20f);
                contentStream.newLineAtOffset(50, 700);

                // ✅ Invoice Header
                contentStream.showText("Food Order Invoice");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLine();

                // ✅ Order & Payment Details
                contentStream.showText("Order ID: " + order.getOrderId());
                contentStream.newLine();
                contentStream.showText("Transaction ID: " + payment.getTransactionId());
                contentStream.newLine();
                contentStream.showText("Payment Method: " + payment.getPaymentMethod());
                contentStream.newLine();
                contentStream.showText("Payment Status: " + payment.getPaymentStatus());
                contentStream.newLine();
                contentStream.showText("Payment Time: " + payment.getPaymentTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                contentStream.newLine();
                contentStream.newLine();

                // ✅ Ordered Items
                contentStream.showText("Ordered Items:");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
                contentStream.showText("----------------------------------------------------");
                contentStream.newLine();
                contentStream.showText("Item Name             |  Price");
                contentStream.newLine();
                contentStream.showText("----------------------------------------------------");
                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA, 12);

                BigDecimal totalAmount = BigDecimal.ZERO;
                for (var item : order.getOrderItems()) {
                    contentStream.showText(item.getMenuItem().getName() + "  |  " + item.getMenuItem().getPrice());
                    contentStream.newLine();
                    totalAmount = totalAmount.add(item.getMenuItem().getPrice());
                }

                contentStream.newLine();
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
                contentStream.showText("Total Amount: $" + totalAmount);
                contentStream.endText();
            }

            // ✅ Save the PDF Invoice
            document.save(new FileOutputStream(invoicePath));
            logger.info("Invoice generated successfully: {}", invoicePath);
        } catch (IOException e) {
            logger.error("Error generating invoice", e);
            return null;
        }

        return invoicePath;
    }
}

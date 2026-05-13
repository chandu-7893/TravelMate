package com.travel.controller;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.travel.service.QRCodeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class TicketController {

    private final QRCodeService qrCodeService;

    public TicketController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/ticket/download")
    public ResponseEntity<byte[]> downloadTicket(HttpSession session) {

        try {
            String username = (String) session.getAttribute("username");
            String destination = (String) session.getAttribute("destination");
            String travelDate = (String) session.getAttribute("travelDate");
            String travelers = String.valueOf(session.getAttribute("travelers"));

            if (username == null) username = "Guest";
            if (destination == null) destination = "Goa";
            if (travelDate == null) travelDate = "Not Selected";
            if (travelers == null || travelers.equals("null")) travelers = "1";

            String bookingId = "TM" + System.currentTimeMillis();

            String qrData =
                    "TravelMate Ticket\n" +
                    "Booking ID: " + bookingId + "\n" +
                    "Name: " + username + "\n" +
                    "Destination: " + destination + "\n" +
                    "Travel Date: " + travelDate + "\n" +
                    "Travelers: " + travelers + "\n" +
                    "Payment Status: SUCCESS";

            byte[] qrImageBytes = qrCodeService.generateQRCode(qrData, 250, 250);

            ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream();

            Document document = new Document();
            PdfWriter.getInstance(document, pdfOutput);

            document.open();

            Font titleFont = new Font(Font.HELVETICA, 24, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 14, Font.NORMAL);
            Font successFont = new Font(Font.HELVETICA, 16, Font.BOLD);

            Paragraph title = new Paragraph("TravelMate E-Ticket", titleFont);
            title.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Booking ID: " + bookingId, normalFont));
            document.add(new Paragraph("Passenger Name: " + username, normalFont));
            document.add(new Paragraph("Destination: " + destination, normalFont));
            document.add(new Paragraph("Travel Date: " + travelDate, normalFont));
            document.add(new Paragraph("Travelers: " + travelers, normalFont));
            document.add(new Paragraph("Payment Status: SUCCESS", successFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Scan this QR Code at check-in:", normalFont));

            Image qrImage = Image.getInstance(qrImageBytes);
            qrImage.setAlignment(Image.ALIGN_CENTER);
            qrImage.scaleAbsolute(180, 180);

            document.add(qrImage);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Generated On: " +
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")),
                    normalFont
            ));

            document.close();

            byte[] pdfBytes = pdfOutput.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "TravelMate-Ticket.pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(pdfBytes);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
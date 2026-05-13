package com.travel.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.travel.entity.TourBooking;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generateBillPdf(TourBooking booking) {
        try {
            String html = """
                    <html>
                    <head>
                    <style>
                        body { font-family: Arial; padding: 30px; }
                        .box { border: 2px solid #111; padding: 25px; }
                        h1 { color: #f59e0b; }
                        p { font-size: 16px; }
                        .total { font-size: 24px; font-weight: bold; }
                    </style>
                    </head>
                    <body>
                        <div class='box'>
                            <h1>TravelMate Invoice</h1>
                            <p><b>Place:</b> %s</p>
                            <p><b>Vehicle:</b> %s</p>
                            <p><b>Hotel:</b> %s</p>
                            <p><b>Days:</b> %d</p>
                            <p><b>Total:</b> ₹ %.2f</p>
                            <p><b>Discount:</b> ₹ %.2f</p>
                            <p class='total'>Final Amount: ₹ %.2f</p>
                        </div>
                    </body>
                    </html>
                    """.formatted(
                    booking.getPlace(),
                    booking.getVehicle(),
                    booking.getHotel(),
                    booking.getDays(),
                    booking.getTotalAmount(),
                    booking.getDiscount(),
                    booking.getFinalAmount()
            );

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed", e);
        }
    }
}
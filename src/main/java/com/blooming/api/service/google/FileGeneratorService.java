package com.blooming.api.service.google;

import com.blooming.api.entity.WateringPlan;
import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.*;

import java.util.List;

@Service
public class FileGeneratorService implements IFileGeneratorService {

    @Override
    public void generateGoogleCalendarFile(List<String> wateringSchedule) {
        String calendarContent = "BEGIN:VCALENDAR\nVERSION:2.0\n";

        for (String date : wateringSchedule) {
            calendarContent += "BEGIN:VEVENT\nSUMMARY:Watering\nDTSTART:" + formatDate(date) + "\nDTEND:" + formatDate(date) + "\nEND:VEVENT\n";
        }
        calendarContent += "END:VCALENDAR";
        File icsFile = new File("watering_schedule.ics");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(icsFile))) {
            writer.write(calendarContent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateWateringPlanPdf(WateringPlan wateringPlan) {
        // Crear un documento PDF
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            // Configurar el escritor de PDF para escribir el documento
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            // Agregar contenido al PDF
            document.add(new Paragraph("Watering Plan for User: " + wateringPlan.getUser().getEmail()));
            document.add(new Paragraph("--------------------------------------------------"));

            // Agregar detalles de los días de riego
            for (var wateringDay : wateringPlan.getWateringDays()) {
                document.add(new Paragraph("Date: " + wateringDay.getDate()));
                document.add(new Paragraph("Watering Amount: " + wateringDay.getWateringAmount()));
                document.add(new Paragraph("--------------------------------------------------"));
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private String formatDate(String date) {
        return date.replace(" ", "T") + "Z";
    }
}

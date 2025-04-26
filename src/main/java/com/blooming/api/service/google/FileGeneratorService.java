package com.blooming.api.service.google;

import com.blooming.api.entity.WateringDay;
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
    public byte[] generateGoogleCalendarFile(List<String> wateringSchedule) {
        try {
            StringBuilder calendarContent = new StringBuilder("BEGIN:VCALENDAR\nVERSION:2.0\n");

            for (String date : wateringSchedule) {
                calendarContent.append("BEGIN:VEVENT\nSUMMARY:Watering\nDTSTART:")
                        .append(formatDate(date))
                        .append("\nDTEND:")
                        .append(formatDate(date))
                        .append("\nEND:VEVENT\n");
            }
            calendarContent.append("END:VCALENDAR");

            return calendarContent.toString().getBytes();
        } catch (Exception e) {
            throw new RuntimeException("Error while generating ICS content", e);
        }
    }

    @Override
    public byte[] generateWateringPlanPdf(WateringPlan wateringPlan) {
        Document document = new Document();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, byteArrayOutputStream);
            document.open();

            document.add(new Paragraph("Watering Plan for User: " + wateringPlan.getPlant().getUser().getEmail()));
            document.add(new Paragraph("--------------------------------------------------"));

            for (WateringDay wateringDay : wateringPlan.getWateringDays()) {
                document.add(new Paragraph("Date: " + wateringDay.getYear() + "-"
                        + String.format("%02d", wateringDay.getMonth()) + "-"
                        + String.format("%02d", wateringDay.getDay())));
                document.add(new Paragraph("Recommendation: " + wateringDay.getRecommendation()));
                document.add(new Paragraph("--------------------------------------------------"));
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error while generating PDF", e);
        }
        return byteArrayOutputStream.toByteArray();
    }


    private String formatDate(String date) {
        return date.replace(" ", "T") + "Z";
    }
}

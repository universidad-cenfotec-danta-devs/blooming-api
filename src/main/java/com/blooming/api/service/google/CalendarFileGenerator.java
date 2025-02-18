package com.blooming.api.service.google;

import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.List;

@Service
public class CalendarFileGenerator implements ICalendarFileGenerator {

    public File generateWateringScheduleFile(List<String> wateringSchedule) {
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
        return icsFile;
    }

    private String formatDate(String date) {
        return date.replace(" ", "T") + "Z";
    }
}

package com.blooming.api.service.google;

import java.io.File;
import java.util.List;

public interface ICalendarFileGenerator {
    File generateWateringScheduleFile(List<String> wateringSchedule);
}

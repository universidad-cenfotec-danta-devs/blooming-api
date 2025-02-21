package com.blooming.api.service.google;

import com.blooming.api.entity.WateringPlan;

import java.util.List;

public interface IFileGeneratorService {
    void generateGoogleCalendarFile(List<String> wateringSchedule);
    byte[] generateWateringPlanPdf(WateringPlan wateringPlan);
}

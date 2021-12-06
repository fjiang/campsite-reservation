package com.upgrade.techchallenge.campsitereserve.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class DateAvailabilityRequest {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateAvailabilityRequest() {
        this.startDate = LocalDate.now();
        this.endDate = LocalDate.now().plusDays(30);
    }

    public DateAvailabilityRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}

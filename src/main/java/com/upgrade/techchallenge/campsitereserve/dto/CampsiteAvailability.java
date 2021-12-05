package com.upgrade.techchallenge.campsitereserve.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CampsiteAvailability {
    private List<LocalDate> availableDates;

    public CampsiteAvailability(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }
}

package com.upgrade.techchallenge.campsitereserve.dto;

import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import lombok.Data;

import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
public class DateAvailabilityRequest {
    private LocalDate startDate;
    private LocalDate endDate;

    public DateAvailabilityRequest() {
        this.startDate = LocalDate.now().plusDays(1);
        this.endDate = LocalDate.now().plusDays(30);
    }

    public DateAvailabilityRequest(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Custom validation to check rules for start and end date
     * @throws ReserveRequestParameterException throws when request parameters validation error happens
     */
    public void validate() throws ReserveRequestParameterException {
        if (startDate.isAfter(endDate)) {
            throw new ReserveRequestParameterException(
                    "Validation error",
                    Map.of("startDate", "Start date can't be later than end date")
            );
        } else if (startDate.isBefore(LocalDate.now().plusDays(1))) {
            throw new ReserveRequestParameterException(
                    "Validation error",
                    Map.of("startDate", "Start date can't be early then tomorrow")
            );
        } else if (endDate.isBefore(LocalDate.now().plusDays(1))) {
            throw new ReserveRequestParameterException(
                    "Validation error",
                    Map.of("endDate", "End date can't be early then tomorrow")
            );
        }
    }
}

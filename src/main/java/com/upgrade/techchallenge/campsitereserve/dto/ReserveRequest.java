package com.upgrade.techchallenge.campsitereserve.dto;

import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
public class ReserveRequest {
    @NotNull
    private String firstName;
    @NotNull
    private String lastName;
    @Email
    private String email;
    @Future
    private LocalDate startDate;
    @Future
    private LocalDate endDate;

    public void validate() throws ReserveRequestParameterException {
        if (startDate.isAfter(endDate)) {
            throw new ReserveRequestParameterException(
                  "Validation error",
                  Map.of("startDate", "Start date can't be later than end date")
            );
        } else if (endDate.isAfter(LocalDate.now().plusDays(30))) {
            throw new ReserveRequestParameterException(
                    "Validation error",
                    Map.of("endDate", "End date can't be later than 30 days from today")
            );
        } else if (DAYS.between(startDate, endDate) > 2) {
            throw new ReserveRequestParameterException(
                    "Validation error",
                    Map.of("endDate", "End date can't be more than 3 days later than start date")
            );
        }
    }
}

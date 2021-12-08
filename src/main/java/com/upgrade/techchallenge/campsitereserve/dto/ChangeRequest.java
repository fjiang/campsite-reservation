package com.upgrade.techchallenge.campsitereserve.dto;

import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
@NoArgsConstructor
public class ChangeRequest {
    @NotNull
    private ChangeReserveOperation changeReserveOperation;
    @NotNull
    private String bookingId;
    @Future
    private LocalDate startDate;
    @Future
    private LocalDate endDate;

    public ChangeRequest(@NotNull ChangeReserveOperation changeReserveOperation, @NotNull String bookingId,
                         @Future LocalDate startDate, @Future LocalDate endDate) {
        this.changeReserveOperation = changeReserveOperation;
        this.bookingId = bookingId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void validate() throws ReserveRequestParameterException {
        // Skip validation for cancel operation
        if (changeReserveOperation == ChangeReserveOperation.CANCEL) {
            return;
        }
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

package com.upgrade.techchallenge.campsitereserve.dto;

import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;

@Data
@NoArgsConstructor
public class ReserveRequest {
    @NotNull
    @ApiModelProperty(example = "feng")
    private String firstName;

    @NotNull
    @ApiModelProperty(example = "jiang")
    private String lastName;

    @Email
    @ApiModelProperty(example = "fjiang@upgrade.com")
    private String email;

    @Future
    @ApiModelProperty(example = "2021-12-10")
    private LocalDate startDate;

    @Future
    @ApiModelProperty(example = "2021-12-15")
    private LocalDate endDate;


    public ReserveRequest(@NotNull String firstName, @NotNull String lastName, @Email String email,
                          @Future LocalDate startDate, @Future LocalDate endDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
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

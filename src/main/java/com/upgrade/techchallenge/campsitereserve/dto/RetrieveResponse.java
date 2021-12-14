package com.upgrade.techchallenge.campsitereserve.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RetrieveResponse extends BaseResponse{

    private LocalDate startDate;
    private LocalDate endDate;
    private String firstName;
    private String lastName;
    private String email;

    public RetrieveResponse(ProcessingStatus processingStatus, List<BaseServiceError> errors,
                            LocalDate startDate, LocalDate endDate,
                            String firstName, String lastName, String email) {
        super(processingStatus, errors);
        this.startDate = startDate;
        this.endDate = endDate;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public RetrieveResponse(ProcessingStatus processingStatus, List<BaseServiceError> errors) {
        this(processingStatus, errors, null, null, null, null, null);
    }
}

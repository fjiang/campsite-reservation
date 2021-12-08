package com.upgrade.techchallenge.campsitereserve.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ReserveResponse extends BaseResponse{

    @ApiModelProperty(example = "cd412b369aba400496699032c1e587f8")
    private String bookingId;

    public ReserveResponse(ProcessingStatus processingStatus, List<BaseServiceError> errors, String bookingId) {
        super(processingStatus, errors);
        this.bookingId = bookingId;
    }
}

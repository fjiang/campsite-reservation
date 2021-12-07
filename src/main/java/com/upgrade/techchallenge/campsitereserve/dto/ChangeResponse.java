package com.upgrade.techchallenge.campsitereserve.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ChangeResponse extends BaseResponse{

    private String trackId;

    public ChangeResponse(ProcessingStatus processingStatus, List<BaseServiceError> errors, String trackId) {
        super(processingStatus, errors);
        this.trackId = trackId;
    }
}

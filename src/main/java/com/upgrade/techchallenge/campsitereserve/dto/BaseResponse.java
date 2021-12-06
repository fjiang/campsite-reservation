package com.upgrade.techchallenge.campsitereserve.dto;

import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import lombok.Data;

import java.util.List;

@Data
public class BaseResponse {
    private ProcessingStatus processingStatus;
    private List<BaseServiceError> errors;

    BaseResponse(ProcessingStatus processingStatus, List<BaseServiceError> errors) {
        this.processingStatus = processingStatus;
        this.errors = errors;
    }

    protected void addError(BaseServiceError error) {
        errors.add(error);
    }
}

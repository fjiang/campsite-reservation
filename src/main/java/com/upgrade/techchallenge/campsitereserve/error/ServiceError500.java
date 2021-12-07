package com.upgrade.techchallenge.campsitereserve.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ServiceError500 extends BaseServiceError {

    public ServiceError500(String errorMessage) {
        super(errorMessage, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    @Override
    @ApiModelProperty(example = "INTERNAL_SERVER_ERROR")
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    @ApiModelProperty(example = "Encountered internal server error")
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}

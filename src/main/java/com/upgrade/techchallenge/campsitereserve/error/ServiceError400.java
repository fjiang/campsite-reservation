package com.upgrade.techchallenge.campsitereserve.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Getter @Setter
@NoArgsConstructor
public class ServiceError400 extends BaseServiceError {
    private List<FieldError> errors = new ArrayList<>();

    public ServiceError400(String errorMessage) {
        super(errorMessage, ErrorCode.BAD_REQUEST);
    }

    public void addError(String field, String errorMessage) {
        FieldError error = new FieldError(field, errorMessage);
        errors.add(error);
    }

    @Override
    @ApiModelProperty(example = "BAD_REQUEST")
    public ErrorCode getErrorCode() {
        return super.getErrorCode();
    }

    @Override
    @ApiModelProperty(example = "must be a future date")
    public String getErrorMessage() {
        return super.getErrorMessage();
    }
}

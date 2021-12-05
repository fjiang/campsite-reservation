package com.upgrade.techchallenge.campsitereserve.error;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ServiceError400 extends BaseServiceError {
    private List<FieldError> errors = new ArrayList<>();

    public ServiceError400(String errorMessage) {
        super(errorMessage, ErrorCode.BAD_REQUEST);
    }

    public void addError(String field, String errorMessage) {
        FieldError error = new FieldError(field, errorMessage);
        errors.add(error);
    }
}

package com.upgrade.techchallenge.campsitereserve.error;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ServiceError500 extends BaseServiceError {
    private List<FieldError> errors = new ArrayList<>();

    public ServiceError500(String errorMessage) {
        super(errorMessage, ErrorCode.INTERNAL_SERVER_ERROR);
    }


}

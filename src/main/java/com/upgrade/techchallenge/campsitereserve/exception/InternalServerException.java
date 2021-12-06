package com.upgrade.techchallenge.campsitereserve.exception;

import com.upgrade.techchallenge.campsitereserve.error.BaseServiceError;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError500;
import org.springframework.http.HttpStatus;

import java.util.Map;

public class InternalServerException extends CampsiteReserveException {

    public InternalServerException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    protected BaseServiceError createServiceError() {
        return new ServiceError500(getMessage());
    }
}

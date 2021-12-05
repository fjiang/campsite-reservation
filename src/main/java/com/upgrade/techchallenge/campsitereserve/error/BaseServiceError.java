package com.upgrade.techchallenge.campsitereserve.error;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseServiceError {
    private String errorMessage;
    private ErrorCode errorCode;

    BaseServiceError(String errorMessage, ErrorCode errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }
}

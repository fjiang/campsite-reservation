package com.upgrade.techchallenge.campsitereserve.error;

import com.upgrade.techchallenge.campsitereserve.utils.TrackIdGenerator;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseServiceError {
    private String errorMessage;
    private ErrorCode errorCode;
    private String trackId;

    BaseServiceError(String errorMessage, ErrorCode errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.trackId = TrackIdGenerator.generateTrackId();
    }
}

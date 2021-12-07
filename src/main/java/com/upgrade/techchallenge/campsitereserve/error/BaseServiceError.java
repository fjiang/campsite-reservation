package com.upgrade.techchallenge.campsitereserve.error;

import com.upgrade.techchallenge.campsitereserve.utils.TrackIdGenerator;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BaseServiceError {
    private String errorMessage;
    private ErrorCode errorCode;
    @ApiModelProperty(example = "cd412b369aba400496699032c1e587f8")
    private String trackId;

    BaseServiceError(String errorMessage, ErrorCode errorCode) {
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.trackId = TrackIdGenerator.generateTrackId();
    }
}

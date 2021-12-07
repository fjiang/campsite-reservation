package com.upgrade.techchallenge.campsitereserve.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.upgrade.techchallenge.campsitereserve.Serializer.LocalDateSerializer;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CampsiteAvailability {
    @ApiModelProperty(example = "[\"2021-10-15\"]")
    @JsonSerialize(contentUsing = LocalDateSerializer.class)
    private List<LocalDate> availableDates;

    public CampsiteAvailability(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }
}

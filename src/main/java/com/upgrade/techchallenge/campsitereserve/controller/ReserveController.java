package com.upgrade.techchallenge.campsitereserve.controller;

import com.upgrade.techchallenge.campsitereserve.dto.*;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError500;
import com.upgrade.techchallenge.campsitereserve.exception.InternalServerException;
import com.upgrade.techchallenge.campsitereserve.exception.ReserveRequestParameterException;
import com.upgrade.techchallenge.campsitereserve.service.CampsiteService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller with 3 endpoints to check campsite availability,
 * reserve campsite and change reservation.
 */
@RestController
@Api(tags = "Campsite Reservation API")
@RequestMapping(path="/campsite/")
public class ReserveController {

    @Autowired
    private CampsiteService campsiteService;

    @GetMapping(path = "availability", produces = "application/json")
    @ApiOperation(value = "Get available dates")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request", response = ServiceError400.class),
            @ApiResponse(code = 500, message = "Internal server error", response = ServiceError500.class)
    })
    public ResponseEntity<CampsiteAvailability> checkCampsiteAvailability(
            @ApiParam(
                    name = "startDate",
                    type = "local date",
                    value = "start date",
                    example = "2021-12-10")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="startDate", required=false) LocalDate startDate,
            @ApiParam(
                    name = "endDate",
                    type = "local date",
                    value = "end date",
                    example = "2021-12-15")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) @RequestParam(value="endDate", required=false) LocalDate endDate
    ) throws InternalServerException, ReserveRequestParameterException {
        DateAvailabilityRequest dateAvailabilityRequest = new DateAvailabilityRequest();
        if (startDate != null) dateAvailabilityRequest.setStartDate(startDate);
        if (endDate != null) dateAvailabilityRequest.setEndDate(endDate);
        dateAvailabilityRequest.validate();
        List<LocalDate> availList = campsiteService.getAvailable(dateAvailabilityRequest);
        return new ResponseEntity<>(new CampsiteAvailability(availList), HttpStatus.OK);
    }

    @PostMapping(path = "reserve", produces = "application/json")
    @ApiOperation(value = "Reserve campsite")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request", response = ServiceError400.class),
            @ApiResponse(code = 500, message = "Internal server error", response = ServiceError500.class)
    })
    public ResponseEntity<ReserveResponse> reserve(
            @Valid @RequestBody ReserveRequest reserveRequest
    ) throws ReserveRequestParameterException, InternalServerException {
        reserveRequest.validate();
        return new ResponseEntity<>(campsiteService.reserve(reserveRequest), HttpStatus.OK);
    }

    @GetMapping(path = "retrieve", produces = "application/json")
    @ApiOperation(value = "Retrieve reservation by booking id")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request", response = ServiceError400.class),
            @ApiResponse(code = 500, message = "Internal server error", response = ServiceError500.class)
    })
    public ResponseEntity<RetrieveResponse> retrieve(
            @ApiParam(
                    name = "bookingId",
                    type = "String",
                    value = "booking id",
                    example = "84c6894ecb6e4ef18bb1e68aa10bc8a5")
            @RequestParam(value="bookingId") String bookingId) throws InternalServerException {
        return new ResponseEntity<>(campsiteService.retrieve(bookingId), HttpStatus.OK);
    }

    @PostMapping(path = "change", produces = "application/json")
    @ApiOperation(value = "Update or cancel reservation")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad request", response = ServiceError400.class),
            @ApiResponse(code = 500, message = "Internal server error", response = ServiceError500.class)
    })
    public ResponseEntity<ChangeResponse> change(
            @Valid @RequestBody ChangeRequest changeRequest
    ) throws ReserveRequestParameterException {
        changeRequest.validate();
        return new ResponseEntity<>(campsiteService.change(changeRequest), HttpStatus.OK);
    }
}

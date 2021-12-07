package com.upgrade.techchallenge.campsitereserve.repository;

import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import org.springframework.boot.availability.AvailabilityState;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateAvailabilityRepository extends CrudRepository<DateAvailability, Long> {

    Optional<DateAvailability> findById(Long id);

    @Query(value="SELECT da FROM DateAvailability da WHERE campsiteStatus = :status AND date BETWEEN :startDate AND :endDate")
    List<DateAvailability> findDatesBetween(CampsiteStatus status, LocalDate startDate, LocalDate endDate);

    @Query(value="SELECT da FROM DateAvailability da WHERE bookingId = :bookingId")
    List<DateAvailability> findDatesByBookingId(String bookingId);

}

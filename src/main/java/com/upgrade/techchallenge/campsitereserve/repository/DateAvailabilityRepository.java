package com.upgrade.techchallenge.campsitereserve.repository;

import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateAvailabilityRepository extends JpaRepository<DateAvailability, Long> {

    Optional<DateAvailability> findById(Long id);

    @Query(value="SELECT da FROM DateAvailability da WHERE campsiteStatus = :status AND date BETWEEN :startDate AND :endDate")
    List<DateAvailability> findDatesBetween(CampsiteStatus status, LocalDate startDate, LocalDate endDate);

    @Query(value="SELECT da FROM DateAvailability da WHERE bookingId = :bookingId")
    List<DateAvailability> findDatesByBookingId(String bookingId);

}

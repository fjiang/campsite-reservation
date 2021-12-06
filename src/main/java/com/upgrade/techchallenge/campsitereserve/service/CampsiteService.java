package com.upgrade.techchallenge.campsitereserve.service;

import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import com.upgrade.techchallenge.campsitereserve.dto.DateAvailabilityRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ProcessingStatus;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveResponse;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import com.upgrade.techchallenge.campsitereserve.exception.InternalServerException;
import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import com.upgrade.techchallenge.campsitereserve.utils.TrackIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import static java.time.temporal.ChronoUnit.DAYS;

@Service
public class CampsiteService {

    private Logger logger = LoggerFactory.getLogger(CampsiteService.class);

    @Autowired
    private DateAvailabilityRepository dateAvailabilityRepository;

    @Autowired
    private UserRepository userRepository;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public List<LocalDate> getAvailable(DateAvailabilityRequest request)
            throws InternalServerException {
        List<DateAvailability> campsiteAvailabilities;
        try {
            boolean acquired = lock.readLock().tryLock(2000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                String errorMessage = "Timeout acquiring available dates";
                logger.error(errorMessage);
                throw new InternalServerException(errorMessage);
            }
            LocalDate startDate = request.getStartDate();
            LocalDate endDate = request.getEndDate();
            campsiteAvailabilities =
                    dateAvailabilityRepository.findDatesBetween(CampsiteStatus.AVAILABLE, startDate, endDate);
        } catch (InterruptedException ex) {
            String errorMessage = "Interrupted acquiring available dates";
            logger.error(errorMessage, ex);
            throw new InternalServerException(errorMessage);
        } finally {
            lock.readLock().unlock();
        }

        return campsiteAvailabilities.stream().map(DateAvailability::getDate).collect(Collectors.toList());
    }

    public ReserveResponse reserve(ReserveRequest reserveRequest)
            throws InternalServerException {
        try {
            boolean acquired = lock.writeLock().tryLock(5000, TimeUnit.MILLISECONDS);
            if (!acquired) {
                String errorMessage = String.format("Timeout reserving campsite - [%s]", reserveRequest);
                logger.error(errorMessage);
                throw new InternalServerException("Timeout reserving campsite");
            }
            LocalDate startDate = reserveRequest.getStartDate();
            LocalDate endDate = reserveRequest.getEndDate();
            List<DateAvailability> dateAvailabilities = dateAvailabilityRepository.findDatesBetween(
                    CampsiteStatus.AVAILABLE, startDate, endDate);
            if (dateAvailabilities.size() - 1 != DAYS.between(startDate, endDate)) {
                String errorMessage = "Date has already booked";
                return new ReserveResponse(
                        ProcessingStatus.FAILED, List.of(new ServiceError400(errorMessage)), null);
            } else {
                User user = userRepository.findOneByEmail(reserveRequest.getEmail());
                if (user == null) {
                    user = new User(reserveRequest.getFirstName(),
                                    reserveRequest.getLastName(),
                                    reserveRequest.getEmail());
                    userRepository.save(user);
                }
                String trackId = reserve(dateAvailabilities, user);
                return new ReserveResponse(
                        ProcessingStatus.SUCCEEDED, null, trackId);
            }
        } catch (InterruptedException ex) {
            String innerErrorMessage = String.format("Interrupted reserving campsite - [%s]", reserveRequest);
            throw new InternalServerException("Interrupted reserving campsite", innerErrorMessage);
        } catch (Exception ex) {
            String errorMessage = "Internal error generated";
            throw new InternalServerException(errorMessage, errorMessage);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String reserve(List<DateAvailability> dateAvailabilities, User user) {
        String bookingId = TrackIdGenerator.generateTrackId();

        for (DateAvailability dateAvailability : dateAvailabilities) {
            dateAvailability.setCampsiteStatus(CampsiteStatus.RESERVED);
            dateAvailability.setUser(user);
            dateAvailability.setBookingId(bookingId);
        }
        dateAvailabilityRepository.saveAll(dateAvailabilities);

        logger.info("User info found with findByAvailability()");
        logger.info("-----------------------------------------");
        for (DateAvailability dateAvailabilityFetched : dateAvailabilityRepository.findAll()) {
            logger.info(dateAvailabilityFetched.toString());
        }
        logger.info("");
        return bookingId;
    }
}

package com.upgrade.techchallenge.campsitereserve;

import com.upgrade.techchallenge.campsitereserve.controller.ReserveController;
import com.upgrade.techchallenge.campsitereserve.dto.*;
import com.upgrade.techchallenge.campsitereserve.error.ErrorCode;
import com.upgrade.techchallenge.campsitereserve.error.ServiceError400;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CampsiteReserveApplicationTests {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @LocalServerPort
    private int port;

	@Autowired
	private ReserveController reserveController;

	@Autowired
    private TestRestTemplate restTemplate;

	@Test
    @Order(1)
	void contextLoads() {
        assertThat(reserveController).isNotNull();
	}

	@Test
    @Order(2)
    void defaultAvailabilityCheckShouldReturn30Dates() {
        CampsiteAvailability availability =
                restTemplate.getForObject("http://localhost:" + port + "/campsite/availability", CampsiteAvailability.class);
        assertThat(availability.getAvailableDates()).hasSize(30);
    }

    @Test
    @Order(3)
    void reserveWithStartDateInPastShouldReturn400Error() {
	    LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);
	    ReserveRequest reserveRequest = new ReserveRequest("feng", "jiang", "fjiang@upgrade.com",
                startDate, endDate);
        HttpEntity<ReserveRequest> requestEntity = new HttpEntity<>(reserveRequest);
        ResponseEntity<ServiceError400> response = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                requestEntity, ServiceError400.class);
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualByComparingTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    @Order(4)
    void reserveWithDateRangeOver3ShouldReturn400Error() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(6);
        ReserveRequest reserveRequest = new ReserveRequest("feng", "jiang", "fjiang@upgrade.com",
                startDate, endDate);
        HttpEntity<ReserveRequest> requestEntity = new HttpEntity<>(reserveRequest);
        ResponseEntity<ServiceError400> response = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                requestEntity, ServiceError400.class);
        assertThat(response.getStatusCode()).isEqualByComparingTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualByComparingTo(ErrorCode.BAD_REQUEST);
    }

    @Test
    @Order(5)
    void reserveWithQualifiedDateRangeThenCancelShouldSucceed() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        ReserveRequest reserveRequest = new ReserveRequest("feng", "jiang", "fjiang@upgrade.com",
                startDate, endDate);
        ResponseEntity<ReserveResponse> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                new HttpEntity<>(reserveRequest), ReserveResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getProcessingStatus()).isEqualByComparingTo(ProcessingStatus.SUCCEEDED);
        assertThat(updateResponse.getBody().getBookingId()).isNotEmpty();

        ChangeRequest changeRequest = new ChangeRequest(
                ChangeReserveOperation.CANCEL, updateResponse.getBody().getBookingId(), null, null);
        ResponseEntity<ChangeResponse> changeResponse = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/change", HttpMethod.POST,
                new HttpEntity<>(changeRequest), ChangeResponse.class);
        assertThat(changeResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(changeResponse.getBody()).isNotNull();
        assertThat(changeResponse.getBody().getProcessingStatus()).isEqualByComparingTo(ProcessingStatus.SUCCEEDED);
    }

    @Test
    @Order(6)
    void reserveWithQualifiedDateRangeThenChangeShouldSucceed() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        ReserveRequest reserveRequest = new ReserveRequest("feng", "jiang", "fjiang@upgrade.com",
                startDate, endDate);
        ResponseEntity<ReserveResponse> updateResponse = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                new HttpEntity<>(reserveRequest), ReserveResponse.class);
        assertThat(updateResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(updateResponse.getBody()).isNotNull();
        assertThat(updateResponse.getBody().getProcessingStatus()).isEqualByComparingTo(ProcessingStatus.SUCCEEDED);
        assertThat(updateResponse.getBody().getBookingId()).isNotEmpty();

        ChangeRequest changeRequest = new ChangeRequest(
                ChangeReserveOperation.CHANGE, updateResponse.getBody().getBookingId(), startDate.plusDays(1), endDate.plusDays(1));
        ResponseEntity<ChangeResponse> changeResponse = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/change", HttpMethod.POST,
                new HttpEntity<>(changeRequest), ChangeResponse.class);
        assertThat(changeResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(changeResponse.getBody()).isNotNull();
        assertThat(changeResponse.getBody().getProcessingStatus()).isEqualByComparingTo(ProcessingStatus.SUCCEEDED);

        // Cancel reservation
        cancel(changeResponse.getBody().getTrackId());
    }

    @Test
    @Order(7)
    void multipleReserveWithOverLappedDateShouldHaveOneSucceedAndOthersFail() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(3);
        ExecutorService executor = Executors.newFixedThreadPool(200);
        ExecutorCompletionService<ReserveResponse> service = new ExecutorCompletionService<>(executor);
        for (int i = 0; i < 200; i++) {
            String firstName = "firstName" + i;
            String lastName = "lastName" + i;
            String email = "email" + i + "@upgrade.com";
            service.submit(() -> {
                ReserveRequest reserveRequest = new ReserveRequest(firstName, lastName, email,
                        startDate, endDate);
                ResponseEntity<ReserveResponse> response = restTemplate.exchange(
                        "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                        new HttpEntity<>(reserveRequest), ReserveResponse.class);
                return response.getBody();
            });
        }
        try {
            int succeedCount = 0;
            ReserveResponse succeeded = new ReserveResponse(ProcessingStatus.FAILED, null, null);
            for (int i = 0; i < 200; i++) {
                Future<ReserveResponse> finished = service.take();
                if (finished.get().getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                    succeedCount += 1;
                    succeeded = finished.get();
                    logger.info("succeeded {} i = {}", finished.get(), i);
                }
            }
            assertThat(succeedCount).isEqualTo(1);
            if (succeeded.getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                cancel(succeeded.getBookingId());
            }
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    @Test
    @Order(8)
    void multipleReserveWithNonOverLappedDateShouldAllSucceed() {
        ExecutorService executor = Executors.newFixedThreadPool(200);
        ExecutorCompletionService<ReserveResponse> service = new ExecutorCompletionService<>(executor);
        for (int i = 1; i < 20; i+=2) {
            String firstName = "firstName" + i;
            String lastName = "lastName" + i;
            String email = "email" + i + "@upgrade.com";
            LocalDate startDate = LocalDate.now().plusDays(i);
            LocalDate endDate = LocalDate.now().plusDays(i+1);
            service.submit(() -> {
                ReserveRequest reserveRequest = new ReserveRequest(firstName, lastName, email,
                        startDate, endDate);
                ResponseEntity<ReserveResponse> response = restTemplate.exchange(
                        "http://localhost:" + port + "/campsite/reserve", HttpMethod.POST,
                        new HttpEntity<>(reserveRequest), ReserveResponse.class);
                return response.getBody();
            });
        }
        try {
            int succeedCount = 0;
            List<ReserveResponse> succeeded = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                Future<ReserveResponse> finished = service.take();
                if (finished.get().getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                    succeedCount += 1;
                    succeeded.add(finished.get());
                }
            }
            assertThat(succeedCount).isEqualTo(10);
            for (ReserveResponse reserveResponse : succeeded) {
                if (reserveResponse.getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                    cancel(reserveResponse.getBookingId());
                }
            }
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

    private void cancel(String bookingId) {
        ChangeRequest changeRequest = new ChangeRequest(
                ChangeReserveOperation.CANCEL, bookingId, null, null);
        ResponseEntity<ChangeResponse> changeResponse = restTemplate.exchange(
                "http://localhost:" + port + "/campsite/change", HttpMethod.POST,
                new HttpEntity<>(changeRequest), ChangeResponse.class);
        assertThat(changeResponse.getStatusCode()).isEqualByComparingTo(HttpStatus.OK);
        assertThat(changeResponse.getBody()).isNotNull();
        assertThat(changeResponse.getBody().getProcessingStatus()).isEqualByComparingTo(ProcessingStatus.SUCCEEDED);
    }

}

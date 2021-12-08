package com.upgrade.techchallenge.campsitereserve;

import com.upgrade.techchallenge.campsitereserve.controller.ReserveController;
import com.upgrade.techchallenge.campsitereserve.dto.CampsiteAvailability;
import com.upgrade.techchallenge.campsitereserve.dto.ProcessingStatus;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveRequest;
import com.upgrade.techchallenge.campsitereserve.dto.ReserveResponse;
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
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
            for (int i = 0; i < 200; i++) {
                Future<ReserveResponse> finished = service.take();
                if (finished.get().getProcessingStatus() == ProcessingStatus.SUCCEEDED) {
                    succeedCount += 1;
                    logger.info("succeeded {} i = {}", finished.get(), i);
                }
            }
            assertThat(succeedCount).isEqualTo(1);
        } catch (Exception ex) {
            Assertions.fail(ex);
        }
    }

}

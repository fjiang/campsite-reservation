package com.upgrade.techchallenge.campsitereserve;

import com.upgrade.techchallenge.campsitereserve.domain.DateAvailability;
import com.upgrade.techchallenge.campsitereserve.domain.CampsiteStatus;
import com.upgrade.techchallenge.campsitereserve.domain.User;
import com.upgrade.techchallenge.campsitereserve.repository.DateAvailabilityRepository;
import com.upgrade.techchallenge.campsitereserve.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class CampsiteReserveApplication {

	private static final Logger log = LoggerFactory.getLogger(CampsiteReserveApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(CampsiteReserveApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(DateAvailabilityRepository dateAvailabilityRepository,
								  UserRepository userRepository) {
		return (args) -> {
		    User user = new User("feng", "jiang", "abc.gmail.com");
		    DateAvailability dateAvailability = new DateAvailability(LocalDate.now(), CampsiteStatus.AVAILABLE, user);

		    userRepository.save(user);
		    dateAvailabilityRepository.save(dateAvailability);

			log.info("User info found with findByAvailability()");
			log.info("-----------------------------------------");
			for (DateAvailability dateAvailabilityFetched : dateAvailabilityRepository.findByAvailability(CampsiteStatus.AVAILABLE)) {
				log.info(dateAvailabilityFetched.toString());
			}
			log.info("");
		};
	}

}

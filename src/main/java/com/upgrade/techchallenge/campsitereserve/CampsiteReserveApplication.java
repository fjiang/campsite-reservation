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

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class CampsiteReserveApplication {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	public static void main(String[] args) {
		SpringApplication.run(CampsiteReserveApplication.class, args);
	}

	@Bean
	public CommandLineRunner init(DateAvailabilityRepository dateAvailabilityRepository,
								  UserRepository userRepository) {
		return (args) -> {
			List<DateAvailability> dateAvails = new ArrayList<>();
			for (int i = 1; i <= 30; i++) {
				dateAvails.add(new DateAvailability(LocalDate.now().plusDays(i), CampsiteStatus.AVAILABLE, null));
			}
			dateAvailabilityRepository.saveAll(dateAvails);

			log.info("User info found with findByAvailability()");
			log.info("-----------------------------------------");
			for (DateAvailability dateAvailabilityFetched : dateAvailabilityRepository.findAll()) {
				log.info(dateAvailabilityFetched.toString());
			}
			log.info("");
		};
	}

}

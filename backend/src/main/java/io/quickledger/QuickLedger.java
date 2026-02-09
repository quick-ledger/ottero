package io.quickledger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("io.quickledger.entities")
@EnableJpaRepositories(basePackages = "io.quickledger.repositories")
@EnableScheduling
public class QuickLedger {

	public static void main(String[] args) {
		System.setProperty("spring.devtools.restart.enabled", "false");
		SpringApplication.run(QuickLedger.class, args);
		// print the value of spring.datasource.password from application.properties
		// file

	}

}

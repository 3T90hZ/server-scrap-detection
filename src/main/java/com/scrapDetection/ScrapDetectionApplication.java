package com.scrapDetection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.scrapDetection.repository")
public class ScrapDetectionApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScrapDetectionApplication.class, args);
	}

}

package com.joel.omnicron;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.joel.omnicron.core.entity")
@EnableJpaRepositories("com.joel.omnicron.core.repository")
public class OmnicronApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmnicronApplication.class, args);
	}

}

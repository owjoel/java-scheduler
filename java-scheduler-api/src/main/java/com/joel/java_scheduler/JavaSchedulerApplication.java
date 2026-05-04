package com.joel.java_scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.joel.java_scheduler.core.entity")
@EnableJpaRepositories("com.joel.java_scheduler.core.repository")
public class JavaSchedulerApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaSchedulerApplication.class, args);
	}

}

package com.joel.omnicron.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan("com.joel.omnicron.core.entity")
@EnableJpaRepositories("com.joel.omnicron.core.repository")
@EnableScheduling
public class OmnicronWorkerApplication {
    public static void main(String[] args) {
        SpringApplication.run(OmnicronWorkerApplication.class, args);
    }    
}

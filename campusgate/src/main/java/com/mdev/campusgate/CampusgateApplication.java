package com.mdev.campusgate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class CampusgateApplication {
	static void main(String[] args) {
		SpringApplication.run(CampusgateApplication.class, args);
	}
}
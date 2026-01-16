package com.stockviewer.stockapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class StockapiApplication {
	public static void main(String[] args) {
		SpringApplication.run(StockapiApplication.class, args);
	}
}
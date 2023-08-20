package com.flightbook.TgFlightBook;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestTgFlightBookApplication {

	public static void main(String[] args) {
		SpringApplication.from(TgFlightBookApplication::main).with(TestTgFlightBookApplication.class).run(args);
	}

}

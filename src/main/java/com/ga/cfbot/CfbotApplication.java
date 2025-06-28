package com.ga.cfbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CfbotApplication {

	public static void main(String[] args) {
		SpringApplication.run(CfbotApplication.class, args);
	}

}

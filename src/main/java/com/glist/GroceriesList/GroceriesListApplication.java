package com.glist.GroceriesList;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@SpringBootApplication
@EnableMongoRepositories
public class GroceriesListApplication {
	public static void main(String[] args) {
		SpringApplication.run(GroceriesListApplication.class, args);
	}
}

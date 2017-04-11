package com.yoshio3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;


@EnableAutoConfiguration
@ComponentScan(basePackages = {
				"com.yoshio3",
				"com.yoshio3.services"})
public class AzureFileUploadApplication {

	public static void main(String[] args) {
		SpringApplication.run(AzureFileUploadApplication.class, args);
	}
}

package com.liha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties
@ComponentScan
public class Application {

	public static void main(String[] args) {
		//由于paas平台是零时区，所以设置为东八区
		TimeZone.setDefault(TimeZone.getTimeZone("GMT+8"));
		SpringApplication.run(WebConfig.class, args);
	}
}

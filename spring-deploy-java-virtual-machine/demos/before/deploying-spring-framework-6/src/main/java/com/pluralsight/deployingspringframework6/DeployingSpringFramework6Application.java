package com.pluralsight.deployingspringframework6;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.LinkedList;
import java.util.List;

@SpringBootApplication
@Slf4j
public class DeployingSpringFramework6Application {

	public static void main(String[] args) {
		triggerOom();
		SpringApplication.run(DeployingSpringFramework6Application.class, args);
	}

	private static void triggerOom() {
		List<byte[]> list = new LinkedList<>();
		int index = 1;

		while (true) {
			byte[] b = new byte[10 * 1024 * 1024];
			list.add(b);
			log.info("[{}] Available heap memory: {}", index++, Runtime.getRuntime().freeMemory());
		}
	}

}

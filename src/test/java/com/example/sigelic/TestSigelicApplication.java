package com.example.sigelic;

import org.springframework.boot.SpringApplication;

public class TestSigelicApplication {

	public static void main(String[] args) {
		SpringApplication.from(SigelicApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}

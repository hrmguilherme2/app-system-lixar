package com.spark.redis.demo;


import com.spark.redis.demo.model.Progress;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class Main {

	public static void main(String[] args) {
		Progress.getInstance().setProgress(false);
		SpringApplication.run(Main.class, args);
	}

}

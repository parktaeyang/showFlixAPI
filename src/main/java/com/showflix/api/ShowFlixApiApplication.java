package com.showflix.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.showflix.api.**.mapper")
@EnableScheduling
public class ShowFlixApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShowFlixApiApplication.class, args);
    }

}

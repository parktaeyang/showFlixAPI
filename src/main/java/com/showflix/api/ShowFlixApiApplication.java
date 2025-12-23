package com.showflix.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.showflix.api.**.mapper")
public class ShowFlixApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ShowFlixApiApplication.class, args);
    }

}

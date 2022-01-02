package org.example.opcDemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * @author PanYi
 */
@EnableScheduling
@SpringBootApplication
public class SpringbootApplication{
    public static void main(String[] args) {
        SpringApplication.run(SpringbootApplication.class, args);
    }
}


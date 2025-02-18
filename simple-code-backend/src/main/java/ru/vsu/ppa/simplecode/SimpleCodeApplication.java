package ru.vsu.ppa.simplecode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SimpleCodeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleCodeApplication.class, args);
    }

}

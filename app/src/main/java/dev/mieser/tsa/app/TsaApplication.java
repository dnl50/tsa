package dev.mieser.tsa.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "dev.mieser.tsa")
public class TsaApplication {

    public static void main(String[] args) {
        SpringApplication.run(TsaApplication.class, args);
    }

}

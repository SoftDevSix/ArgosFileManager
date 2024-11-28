package org.argos.file.manager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ArgosFileManagerApplication {

    @SuppressWarnings("java:S1186") // Ignorar advertencia de método vacío
    public static void main(String[] args) {
        SpringApplication.run(ArgosFileManagerApplication.class, args);
    }
}

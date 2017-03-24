package io.github.huiyu.sophon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.lang.management.ManagementFactory;

@SpringBootApplication
public class SophonApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SophonApplication.class, args);
        String name = ManagementFactory.getRuntimeMXBean().getName();
        System.out.println(name);
    }
}

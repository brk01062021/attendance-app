package com.school.attendance;

import com.school.attendance.config.AppProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class AttendanceAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(AttendanceAppApplication.class, args);
    }
}

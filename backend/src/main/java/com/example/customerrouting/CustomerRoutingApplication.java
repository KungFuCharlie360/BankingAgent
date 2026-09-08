package com.example.customerrouting;
import org.springframework.boot.*; import org.springframework.boot.autoconfigure.*; import org.springframework.cache.annotation.*; import org.springframework.scheduling.annotation.*;
@SpringBootApplication @EnableCaching @EnableScheduling public class CustomerRoutingApplication { public static void main(String[] args){SpringApplication.run(CustomerRoutingApplication.class,args);} }

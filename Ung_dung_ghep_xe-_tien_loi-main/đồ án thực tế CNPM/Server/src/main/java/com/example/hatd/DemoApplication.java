package com.example.hatd; 

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan; // <-- Import cần thiết

@SpringBootApplication
// ✅ Thêm @ComponentScan để đảm bảo Spring quét tất cả các package con của "com.example.hatd"
@ComponentScan(basePackages = {"com.example.hatd"}) 
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
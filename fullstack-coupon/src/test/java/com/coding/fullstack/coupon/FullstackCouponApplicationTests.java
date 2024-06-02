package com.coding.fullstack.coupon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class FullstackCouponApplicationTests {

    @Test
    void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalDate plus1 = now.plusDays(1);
        LocalDate plus2 = now.plusDays(2);

        System.out.println("now = " + now);
        System.out.println("plus1 = " + plus1);
        System.out.println("plus2 = " + plus2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;
        System.out.println("min = " + min);
        System.out.println("max = " + max);

        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(now, max);
        System.out.println("start = " + start);
        System.out.println("end = " + end);

        System.out.println("startTime() = " + startTime());
        System.out.println("endTime() = " + endTime());
    }


    private String startTime() {
        LocalDate now = LocalDate.now();
        LocalTime min = LocalTime.MIN;
        LocalDateTime start = LocalDateTime.of(now, min);
        return start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    private String endTime() {
        LocalDate now = LocalDate.now();
        LocalTime max = LocalTime.MAX;
        LocalDateTime end = LocalDateTime.of(now.plusDays(2), max);
        return end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}

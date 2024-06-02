package com.coding.fullstack.order.controller;

import java.util.Date;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.entity.OrderReturnReasonEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
public class RabbitController {

    private final RabbitTemplate rabbitTemplate;

    @GetMapping("/rabbit/sendMq")
    public String sendMq(@RequestParam(value = "num", required = false, defaultValue = "10") Integer num) {
        for (int i = 0; i < num; i++) {
            if (i % 2 == 0) {

                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId((long)i);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("reason?哈哈！");
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity,
                    new CorrelationData(UUID.randomUUID().toString()));
                log.info("Message[{}]发送完成", reasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId((long)i);
                orderEntity.setCreateTime(new Date());
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity,
                    new CorrelationData(UUID.randomUUID().toString()));
                log.info("Message[{}]发送完成", orderEntity);
            }
        }
        return "ok";
    }

    @GetMapping("/rabbit/createOrderTest")
    public String createOrderTest() {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId((long)1);
        orderEntity.setCreateTime(new Date());
        orderEntity.setOrderSn(UUID.randomUUID().toString());
        rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderEntity,
            new CorrelationData(UUID.randomUUID().toString()));
        log.info("Message[{}]发送完成", orderEntity);
        return "ok";
    }
}

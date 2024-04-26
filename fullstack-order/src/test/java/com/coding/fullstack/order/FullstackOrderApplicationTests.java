package com.coding.fullstack.order;

import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.entity.OrderReturnReasonEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
class FullstackOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void contextLoads() {}

    @Test
    void testCreateExchange() {
        DirectExchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功", directExchange);
    }

    @Test
    void testCreateQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功", queue);
    }

    @Test
    void testCreateBinding() {
        Binding binding =
            new Binding("hello-java-queue", Binding.DestinationType.QUEUE, "hello-java-exchange", "hello.java", null);
        amqpAdmin.declareBinding(binding);
        log.info("Binding[{}]创建成功", binding);
    }

    @Test
    void testSendMessage() {
        String msg = "Hello World!";
        rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", msg);
        log.info("Message[{}]发送完成", msg);
    }

    @Test
    void testSendMessage2() {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {

                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId((long)i);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("reason?哈哈！");
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", reasonEntity);
                log.info("Message[{}]发送完成", reasonEntity);
            } else {
                OrderEntity orderEntity = new OrderEntity();
                orderEntity.setId((long)i);
                orderEntity.setCreateTime(new Date());
                orderEntity.setOrderSn(UUID.randomUUID().toString());
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java", orderEntity);
                log.info("Message[{}]发送完成", orderEntity);
            }
        }
    }

}

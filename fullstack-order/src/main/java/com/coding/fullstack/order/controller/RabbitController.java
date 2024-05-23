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

    /**
     * 定制RabbitTemplate
     */
    @PostConstruct
    public void initRabbitTemplate() {

        /*
         * 如何防止消息丢失？
         * 1、做好消息确认机制(publisher,consumer)
         * 2、每一个发送的消息都在数据库做好记录。定期将失败的消息重发。
         */
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            /**
             * 只要消息抵达Broker服务器，ack=true
             * 
             * @param correlationData - 当前消息的唯一关联数据
             * @param ack true for ack, false for nack
             * @param cause An optional cause, for nack, when available, otherwise null.
             */
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                // 服务器收到了
                log.info("confirm===>correlationData={} ack={} cause={}", correlationData, ack, cause);
            }
        });
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            /**
             * 只要消息没有投递给指定的队列，就会触发回调
             * 
             * @param message the returned message. 投递失败的消息详细信息
             * @param replyCode the reply code. 回复的状态码
             * @param replyText the reply text. 回复的文本内容
             * @param exchange the exchange. 当时这个消息发给哪一个交换机
             * @param routingKey the routing key. 当时这个消息用的哪一个路由键
             */
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange,
                String routingKey) {

                // 报错误了。修改数据库当前消息的状态->错误
                log.error("return===>message={} replyCode={} replyText={} exchange={} routingKey={}", message,
                    replyCode, replyText, exchange, routingKey);
            }

        });
    }

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

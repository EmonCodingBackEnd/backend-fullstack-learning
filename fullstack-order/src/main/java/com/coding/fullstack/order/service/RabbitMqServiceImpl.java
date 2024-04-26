package com.coding.fullstack.order.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.entity.OrderReturnReasonEntity;
import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

/**
 * RabbitListener - 一般标注在类上，指定监听的队列
 * 
 * RabbitHandler - 标注在方法上，重载不同的消息类型
 */
@Slf4j
@Service
@RabbitListener(queues = {"hello-java-queue"}) // 类+方法，标注在类上需要配合@RabbitHandler使用
public class RabbitMqServiceImpl {

    /**
     * 监听队列
     *
     * @param reasonEntity - 消息
     * @param message - 类型是 class org.springframework.amqp.core.Message
     * @param channel - 通道
     * 
     *            Queue：可以很多人来监听。只要收到消息，队列删除消息，而且只能有一个人收到此消息。 场景： 1）、订单服务启动多个，同一个消息，只能有一个客户端收到
     *            2）、同一个客户端，只有在处理完成一个消息后，才会处理下一个消息。
     */
    /*@RabbitListener(queues = {"hello-java-queue"})
    public void receiveMessageWithListener(OrderReturnReasonEntity reasonEntity, Message message, Channel channel) {
        // 消息体
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        Object typeId__ = messageProperties.getHeader("__TypeId__");
        log.info("receiveMessageWithListener接收到消息...内容==>{} 详情==>{} 类型==>{}", reasonEntity, message, typeId__);
    }*/

    @RabbitHandler // 方法，仅接收OrderReturnReasonEntity
    public void receiveMessageWithHandler(OrderReturnReasonEntity reasonEntity, Message message, Channel channel) {
        // 消息体
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        Object typeId__ = messageProperties.getHeader("__TypeId__");
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            log.info("receiveMessageWithHandler1接收到消息...deliveryTag={} 内容==>{} 详情==>{} 类型==>{}", deliveryTag,
                reasonEntity, message, typeId__);
            if (deliveryTag % 2 == 0) {
                log.info("签收了货物 {}", deliveryTag);
                // multiple 是否批量ack？
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                log.info("未签收货物 {}", deliveryTag);
                // multiple 是否批量拒绝？ requeue 被拒绝后，是否重新入队？true-是；false-否
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, false);
                // requeue 被拒绝后，是否重新入队？true-是；false-否
                // channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (IOException e) {
            log.error("receiveMessageWithHandler1处理异常", e);
            throw new RuntimeException(e);
        }
    }

    @RabbitHandler // 方法，仅接收OrderEntity
    public void receiveMessageWithHandler(OrderEntity orderEntity, Message message, Channel channel) {
        // 消息体
        byte[] body = message.getBody();
        // 消息头属性信息
        MessageProperties messageProperties = message.getMessageProperties();
        Object typeId__ = messageProperties.getHeader("__TypeId__");
        try {
            long deliveryTag = message.getMessageProperties().getDeliveryTag();
            log.info("receiveMessageWithHandler2接收到消息...deliveryTag={}, 内容==>{} 详情==>{} 类型==>{}", deliveryTag,
                orderEntity, message, typeId__);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("receiveMessageWithHandler2处理异常", e);
            throw new RuntimeException(e);
        }
    }

}
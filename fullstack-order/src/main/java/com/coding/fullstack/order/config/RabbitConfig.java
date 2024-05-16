package com.coding.fullstack.order.config;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.coding.fullstack.order.entity.OrderEntity;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 监听消息时必须启用 @EnableRabbit ，创建交换器、队列和发送消息时不需要
 */
@Slf4j
@EnableRabbit
@Configuration
@RequiredArgsConstructor
public class RabbitConfig {

    /**
     * 使用JSON序列化机制，进行消息转换
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // @Bean注解后，容器中的 Binding、Queue、Exchange都会自动创建（RabbitMQ不存在的情况下）
    // RabbitMQ中存在的情况下，哪怕属性发生变化，也不会重新创建去覆盖已有的配置

    /**
     * 订单服务交换器
     */
    @Bean
    public Exchange orderEventExchange() {
        return new TopicExchange("order-event-exchange", true, false);
    }

    /**
     * 订单延时队列
     */
    @Bean
    public Queue orderDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000);
        return new Queue("order.delay.queue", true, false, false, arguments);
    }

    /**
     * 订单释放订单队列
     */
    @Bean
    public Queue orderReleaseOrderQueue() {
        return new Queue("order.release.order.queue", true, false, false);
    }

    /**
     * 把订单延时任务绑定到延时队列
     */
    @Bean
    public Binding orderCreateOrderBinding() {
        return new Binding("order.delay.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.create.order", null);
    }

    /**
     * 把订单延时任务绑定到释放队列
     */
    @Bean
    public Binding orderReleaseOrderBinding() {
        return new Binding("order.release.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.release.order", null);
    }

    @RabbitListener(queues = {"order.release.order.queue"})
    public void listener(OrderEntity reasonEntity, Message message, Channel channel) throws IOException {
        log.info("收到死信队列的消息：{}", reasonEntity);
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }

}

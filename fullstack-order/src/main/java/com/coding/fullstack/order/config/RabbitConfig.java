package com.coding.fullstack.order.config;

import java.util.HashMap;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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

    /**
     * 订单释放成功后，再次发送解锁库存消息给库存服务
     */
    @Bean
    public Binding orderReleaseOtherBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.release.other.#", null);
    }

    /*
     * @Bean注解后，容器中的 Binding、Queue、Exchange都会自动创建（RabbitMQ不存在的情况下）
     * RabbitMQ中存在的情况下，哪怕属性发生变化，也不会重新创建去覆盖已有的配置
     *
     * 问：项目中引入了RabbitMQ,但是在加了@bean配置交换机和queue，启动项目却没自动化创建队列
     * 答：RabbitMQ懒加载模式， 需要配置消费者监听才会创建
     */
    // @RabbitListener(queues = {"order.release.order.queue"})
    // public void listener(OrderEntity reasonEntity, Message message, Channel channel) throws IOException {
    // log.info("收到死信队列的消息：{}", reasonEntity);
    // channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    // }

    /**
     * 秒杀削峰队列
     */
    @Bean
    public Queue orderSeckillOrderQueue() {
        return new Queue("order.seckill.order.queue", true, false, false);
    }

    /**
     * 把秒杀削峰任务绑定到削峰队列
     */
    @Bean
    public Binding orderSeckillOrderBinding() {
        return new Binding("order.seckill.order.queue", Binding.DestinationType.QUEUE, "order-event-exchange",
            "order.seckill.order", null);
    }

}

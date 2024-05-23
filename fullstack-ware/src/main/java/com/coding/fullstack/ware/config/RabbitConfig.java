package com.coding.fullstack.ware.config;

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
     * 库存服务交换器
     */
    @Bean
    public Exchange stockEventExchange() {
        return new TopicExchange("stock-event-exchange", true, false);
    }

    /**
     * 库存延时队列
     */
    @Bean
    public Queue stockDelayQueue() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "stock-event-exchange");
        arguments.put("x-dead-letter-routing-key", "stock.release");
        arguments.put("x-message-ttl", 120000);
        return new Queue("stock.delay.queue", true, false, false, arguments);
    }

    /**
     * 库存释放库存队列
     */
    @Bean
    public Queue stockReleaseStockQueue() {
        return new Queue("stock.release.stock.queue", true, false, false);
    }

    /**
     * 把订单延时任务绑定到延时队列
     */
    @Bean
    public Binding stockLockedBinding() {
        return new Binding("stock.delay.queue", Binding.DestinationType.QUEUE, "stock-event-exchange", "stock.locked",
            null);
    }

    /**
     * 把订单延时任务绑定到释放队列
     */
    @Bean
    public Binding stockReleaseStockBinding() {
        return new Binding("stock.release.stock.queue", Binding.DestinationType.QUEUE, "stock-event-exchange",
            "stock.release.#", null);
    }

    /*
     * @Bean注解后，容器中的 Binding、Queue、Exchange都会自动创建（RabbitMQ不存在的情况下）
     * RabbitMQ中存在的情况下，哪怕属性发生变化，也不会重新创建去覆盖已有的配置
     *
     * 问：项目中引入了RabbitMQ,但是在加了@bean配置交换机和queue，启动项目却没自动化创建队列
     * 答：RabbitMQ懒加载模式， 需要配置消费者监听才会创建
     */
    // @RabbitListener(queues = {"stock.release.stock.queue"})
    // public void listener(Message message, Channel channel) throws IOException {
    // log.info("收到释放库存的消息...忽略处理！");
    // }

}

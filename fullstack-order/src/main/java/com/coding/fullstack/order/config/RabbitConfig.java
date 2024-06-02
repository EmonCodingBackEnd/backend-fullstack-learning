package com.coding.fullstack.order.config;

import java.util.HashMap;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.amqp.RabbitTemplateConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import lombok.extern.slf4j.Slf4j;

/**
 * 监听消息时必须启用 @EnableRabbit ，创建交换器、队列和发送消息时不需要
 */
@Slf4j
@EnableRabbit
@Configuration
public class RabbitConfig {

    /**
     * 自定义 rabbitTemplate 解决直接注入 rabbitTemplate 时报错问题： The dependencies of some of the beans in the application context
     * form a cycle
     */
    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(RabbitTemplateConfigurer configurer, ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate();
        template.setMessageConverter(messageConverter());
        configurer.configure(template, connectionFactory);
        initRabbitTemplate(template);
        return template;
    }

    /**
     * 定制RabbitTemplate
     */
    public void initRabbitTemplate(RabbitTemplate rabbitTemplate) {

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

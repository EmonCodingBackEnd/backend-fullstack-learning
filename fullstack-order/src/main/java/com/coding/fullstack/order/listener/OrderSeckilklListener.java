package com.coding.fullstack.order.listener;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.coding.common.to.mq.SeckilklOrderTo;
import com.coding.fullstack.order.service.OrderService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@RabbitListener(queues = {"order.seckill.order.queue"})
public class OrderSeckilklListener {
    private final OrderService orderService;

    // @formatter:off
    /**
     */
    // @formatter:on
    @RabbitHandler
    public void handleStockLockedRelease(SeckilklOrderTo orderTo, Message message, Channel channel) throws IOException {
        log.info("准备创建秒杀单的详细信息...");
        try {
            orderService.createSeckillOrder(orderTo);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("释放订单消息失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}

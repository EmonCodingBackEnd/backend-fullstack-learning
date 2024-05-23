package com.coding.fullstack.order.listener;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.service.OrderService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@RabbitListener(queues = {"order.release.order.queue"})
public class OrderReleaseListener {
    private final OrderService orderService;

    // @formatter:off
    /**
     */
    // @formatter:on
    @RabbitHandler
    public void handleStockLockedRelease(OrderEntity entity, Message message, Channel channel) throws IOException {
        log.info("收到释放订单消息");
        try {
            // 当前消息是否被第二次及以后（重新）派发过来了。
            // Boolean redelivered = message.getMessageProperties().getRedelivered();
            orderService.closeOrder(entity);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("释放订单消息失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}

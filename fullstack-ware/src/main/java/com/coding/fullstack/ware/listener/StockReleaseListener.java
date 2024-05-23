package com.coding.fullstack.ware.listener;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.coding.common.to.mq.OrderTo;
import com.coding.common.to.mq.StockLockedTo;
import com.coding.fullstack.ware.service.WareSkuService;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@RabbitListener(queues = {"stock.release.stock.queue"})
public class StockReleaseListener {

    private final WareSkuService wareSkuService;

    // @formatter:off
    /**
     * 1、库存自动解锁。
     * 下单成功，库存锁定成功，接下来的业务调用失败，导致订单回滚。之前锁定的库存就要自动解锁。
     * 2、下单失败。
     * 锁库存失败。
     *
     * 只要解锁库存的消息失败。一定要告诉服务解锁失败。
     */
    // @formatter:on
    @RabbitHandler
    public void handleStockLockedRelease(StockLockedTo to, Message message, Channel channel) throws IOException {
        log.info("收到库存锁定成功触发解锁库存消息");
        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("解锁库存消息失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

    @RabbitHandler
    public void handleOrderClosedRelease(OrderTo to, Message message, Channel channel) throws IOException {
        log.info("收到订单关闭成功触发解锁库存消息");
        try {
            wareSkuService.unLockStock(to);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("解锁库存消息失败", e);
            channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);
        }
    }

}

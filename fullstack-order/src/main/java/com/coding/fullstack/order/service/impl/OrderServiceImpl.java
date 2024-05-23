package com.coding.fullstack.order.service.impl;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.constant.OrderConstant;
import com.coding.common.to.mq.OrderTo;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.utils.R;
import com.coding.common.vo.MemberEntityVo;
import com.coding.fullstack.order.dao.OrderDao;
import com.coding.fullstack.order.dto.OrderCreateTo;
import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.entity.OrderItemEntity;
import com.coding.fullstack.order.enums.OrderStatusEnum;
import com.coding.fullstack.order.feign.CartFeignService;
import com.coding.fullstack.order.feign.MemberFeignService;
import com.coding.fullstack.order.feign.ProductFeignService;
import com.coding.fullstack.order.feign.WareFeignService;
import com.coding.fullstack.order.interceptor.LoginUserInterceptor;
import com.coding.fullstack.order.service.OrderItemService;
import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.*;

import lombok.RequiredArgsConstructor;

@Service("orderService")
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;
    private final ProductFeignService productFeignService;
    private final WareFeignService wareFeignService;
    private final ThreadPoolExecutor executor;
    private final StringRedisTemplate stringRedisTemplate;

    private final OrderItemService orderItemService;

    private final RabbitTemplate rabbitTemplate;

    public static ThreadLocal<OrderSubmitVo> threadLocal = new ThreadLocal<>();

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderEntity> page = this.page(new Query<OrderEntity>().getPage(params), new QueryWrapper<OrderEntity>());

        return new PageUtils(page);
    }

    @Override
    public OrderConfirmVo confirmOrder() throws ExecutionException, InterruptedException {
        MemberEntityVo memberEntityVo = LoginUserInterceptor.threadLocal.get();
        Long id = memberEntityVo.getId();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        OrderConfirmVo orderConfirmVo = new OrderConfirmVo();
        // 1、远程查询所有的收货地址列表
        CompletableFuture<Void> addressFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<MemberAddressVo> address = memberFeignService.getAddress(id);
            orderConfirmVo.setAddress(address);
            RequestContextHolder.resetRequestAttributes();
        }, executor);
        // 2、远程查询购物车所有选中的商品信息=>feign在远程调用之前要构造请求，调用很多拦截器RequestInterceptor
        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.currentUserCartItems();
            orderConfirmVo.setItems(items);
            RequestContextHolder.resetRequestAttributes();
        }, executor).thenRunAsync(() -> {
            List<OrderItemVo> items = orderConfirmVo.getItems();
            List<Long> collect = items.stream().map(OrderItemVo::getSkuId).collect(Collectors.toList());
            R skusHasStock = wareFeignService.getSkusHasStock(collect);
            if (skusHasStock.getCode() == 0) {
                List<SkuHasStockVo> data = skusHasStock.getData(new TypeReference<List<SkuHasStockVo>>() {});
                Map<Long, Boolean> stocks =
                    data.stream().collect(Collectors.toMap(SkuHasStockVo::getSkuId, SkuHasStockVo::getHasStock));
                orderConfirmVo.setStocks(stocks);
            }
        }, executor);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(addressFuture, itemsFuture);
        allOf.get();

        // 3、查询用户积分
        orderConfirmVo.setIntegration(memberEntityVo.getIntegration());

        // 4、其他数据自动计算
        String uuid = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId(), uuid, 30,
            TimeUnit.MINUTES);
        orderConfirmVo.setOrderToken(uuid);
        return orderConfirmVo;
    }

    // 本地事务，在分布式系统，只能控制住自己的回滚，控制不了其他服务的回滚。
    // 分布式事务：最大原因，是网络原因。
    // @GlobalTransactional 不适用于高并发场景
    @Transactional
    @Override
    public SubmitOrderResponseVo submitOrder(OrderSubmitVo vo) {
        SubmitOrderResponseVo submitResponse = new SubmitOrderResponseVo();
        submitResponse.setCode(0);
        try {
            threadLocal.set(vo);
            MemberEntityVo memberEntityVo = LoginUserInterceptor.threadLocal.get();
            // 下单：去创建订单、验证令牌、验证价格、锁库存...
            // 1、验证令牌【令牌的对比和删除必须保证原子性】
            /*String orderToken = vo.getOrderToken();
            String redisOrderToken =
            stringRedisTemplate.opsForValue().get(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId());
            // 令牌验证是否通过
            boolean flag = orderToken != null && orderToken.equals(redisOrderToken);
            if (!flag) {
            return submitResponse;
            }
            // 令牌验证通过
            stringRedisTemplate.delete(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId());*/

            String script =
                "if(redis.call('get', KEYS[1]) == ARGV[1]) then return redis.call('del', KEYS[1]) else return 0 end";
            String orderToken = vo.getOrderToken();
            Long result = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class),
                Collections.singletonList(OrderConstant.USER_ORDER_TOKEN_PREFIX + memberEntityVo.getId()), orderToken);
            if (result != null && result.intValue() == 0) {
                submitResponse.setCode(1);
                return submitResponse;
            }
            // 2、验价
            OrderCreateTo orderTo = createOrder();
            BigDecimal payAmount = orderTo.getOrder().getPayAmount();
            BigDecimal payPrice = vo.getPayPrice();
            // 如果验价成功（误差小于0.01算作成功）
            if (Math.abs(payAmount.subtract(payPrice).doubleValue()) > 0.01) {
                submitResponse.setCode(2);
                return submitResponse;
            }
            // 3、保存订单
            saveOrder(orderTo);
            // 4、库存锁定。只要有异常，回滚库存的锁定
            WareSkuLockVo wareSkuLockVo = new WareSkuLockVo();
            wareSkuLockVo.setOrderSn(orderTo.getOrder().getOrderSn());
            wareSkuLockVo.setLocks(orderTo.getOrderItems().stream().map(item -> {
                OrderItemVo itemVo = new OrderItemVo();
                itemVo.setSkuId(item.getSkuId());
                itemVo.setCount(item.getSkuQuantity());
                itemVo.setTitle(item.getSkuName());
                return itemVo;
            }).collect(Collectors.toList()));
            R r = wareFeignService.orderLockStock(wareSkuLockVo);
            if (r.getCode() == 0) {
                // 锁定成功
                submitResponse.setOrder(orderTo.getOrder());
                // 订单创建成功发送消息给MQ
                rabbitTemplate.convertAndSend("order-event-exchange", "order.create.order", orderTo.getOrder(),
                    new CorrelationData(UUID.randomUUID().toString()));
                return submitResponse;
            } else {
                // 锁定失败
                submitResponse.setCode(3);
                return submitResponse;
            }

        } catch (Exception e) {
            log.error("异常", e);
            throw e;
        } finally {
            threadLocal.remove();
        }
    }

    /**
     * 保存订单数据
     *
     * @param orderTo
     */
    private void saveOrder(OrderCreateTo orderTo) {
        OrderEntity order = orderTo.getOrder();
        order.setModifyTime(new Date());
        this.save(order);
        List<OrderItemEntity> orderItems = orderTo.getOrderItems();
        orderItemService.saveBatch(orderItems);
    }

    private OrderCreateTo createOrder() {
        OrderCreateTo orderCreateTo = new OrderCreateTo();
        // 1、生成订单号
        String orderSn = IdWorker.getTimeId();
        OrderEntity order = buildOrder(orderSn);
        // 2、获取所有的订单项
        List<OrderItemEntity> orderItemEntities = buildOrderItem(orderSn);
        if (orderItemEntities == null) {
            throw new RuntimeException("没有选中的商品");
        }
        // 3、计算价格
        BigDecimal payPrice = computePrice(order, orderItemEntities);

        orderCreateTo.setOrder(order);
        orderCreateTo.setOrderItems(orderItemEntities);
        orderCreateTo.setPayPrice(payPrice);
        return orderCreateTo;
    }

    private BigDecimal computePrice(OrderEntity order, List<OrderItemEntity> orderItemEntities) {
        OrderSubmitVo orderSubmitVo = threadLocal.get();
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal paTotal = BigDecimal.ZERO;
        BigDecimal caTotal = BigDecimal.ZERO;
        BigDecimal iaTotal = BigDecimal.ZERO;
        BigDecimal growth = BigDecimal.ZERO;
        BigDecimal integ = BigDecimal.ZERO;
        for (OrderItemEntity entity : orderItemEntities) {
            total = total.add(entity.getRealAmount());
            paTotal = paTotal.add(entity.getPromotionAmount());
            caTotal = caTotal.add(entity.getCouponAmount());
            iaTotal = iaTotal.add(entity.getIntegrationAmount());
            growth = growth.add(new BigDecimal(entity.getGiftGrowth().toString())); // 成长值
            integ = integ.add(new BigDecimal(entity.getGiftIntegration().toString())); // 积分
        }
        // 1、订单相关价格
        order.setTotalAmount(total);
        order.setPayAmount(total.add(order.getFreightAmount())); // 应付总额
        order.setPromotionAmount(paTotal);
        order.setCouponAmount(caTotal);
        order.setIntegrationAmount(iaTotal);
        // 设置积分、成长值
        order.setGrowth(growth.intValue());
        order.setIntegration(integ.intValue());
        order.setDeleteStatus(0); // 未删除
        return order.getPayAmount();
    }

    /**
     * 构建订单数据
     *
     * @param orderSn
     * @return
     */
    private OrderEntity buildOrder(String orderSn) {
        OrderSubmitVo orderSubmitVo = threadLocal.get();

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSn);
        orderEntity.setMemberId(LoginUserInterceptor.threadLocal.get().getId());
        // 获取收货地址信息
        R fare = wareFeignService.getFare(orderSubmitVo.getAddrId());
        FareVo fareVo = fare.getData(new TypeReference<FareVo>() {});
        MemberAddressVo address = fareVo.getAddress();
        orderEntity.setFreightAmount(fareVo.getFare());
        // 设置收货人信息
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverRegion(address.getRegion());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        // 设置订单状态信息
        orderEntity.setStatus(OrderStatusEnum.CREATE_NEW.getCode());
        orderEntity.setAutoConfirmDay(7);
        return orderEntity;
    }

    /**
     * 构建所有订单项数据
     *
     * @param orderSn
     * @return
     */
    private List<OrderItemEntity> buildOrderItem(String orderSn) {
        // 最后确定每个购物项的价格
        List<OrderItemVo> currentUserCartItems = cartFeignService.currentUserCartItems();
        if (currentUserCartItems != null && currentUserCartItems.size() > 0) {
            List<OrderItemEntity> orderItemEntities = currentUserCartItems.stream().map(item -> {
                OrderItemEntity orderItemEntity = new OrderItemEntity();
                // 1、订单信息：订单号
                orderItemEntity.setOrderSn(orderSn);
                // 2、商品的SPU信息
                R spuInfoBySkuId = productFeignService.getSpuInfoBySkuId(item.getSkuId());
                SpuInfoVo spuInfoVo = spuInfoBySkuId.getData(new TypeReference<SpuInfoVo>() {});
                orderItemEntity.setSpuId(spuInfoVo.getId());
                orderItemEntity.setSpuBrand(spuInfoVo.getBrandId().toString());
                orderItemEntity.setSpuName(spuInfoVo.getSpuName());
                orderItemEntity.setCategoryId(spuInfoVo.getCatalogId());
                // 3、商品的sku信息
                orderItemEntity.setSkuId(item.getSkuId());
                orderItemEntity.setSkuName(item.getTitle());
                orderItemEntity.setSkuPic(item.getImage());
                orderItemEntity.setSkuPrice(item.getPrice());
                String skuAttrsVals = StringUtils.collectionToDelimitedString(item.getSkuAttr(), ";");
                orderItemEntity.setSkuAttrsVals(skuAttrsVals);
                orderItemEntity.setSkuQuantity(item.getCount());
                // 4、优惠信息【不做】
                // 5、积分信息
                orderItemEntity.setGiftGrowth(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
                orderItemEntity
                    .setGiftIntegration(item.getPrice().multiply(new BigDecimal(item.getCount())).intValue());
                // 6、订单项的价格信息
                orderItemEntity.setPromotionAmount(BigDecimal.ZERO);
                orderItemEntity.setCouponAmount(BigDecimal.ZERO);
                orderItemEntity.setIntegrationAmount(BigDecimal.ZERO);
                BigDecimal orign =
                    orderItemEntity.getSkuPrice().multiply(new BigDecimal(orderItemEntity.getSkuQuantity()));
                orderItemEntity.setRealAmount(orign.subtract(orderItemEntity.getPromotionAmount())
                    .subtract(orderItemEntity.getCouponAmount()).subtract(orderItemEntity.getIntegrationAmount()));
                return orderItemEntity;
            }).collect(Collectors.toList());
            return orderItemEntities;
        }
        return null;
    }

    @Override
    public OrderEntity getOrderByOrderSn(String orderSn) {
        OrderEntity one = this.getOne(Wrappers.lambdaQuery(OrderEntity.class).eq(OrderEntity::getOrderSn, orderSn));
        return one;
    }

    @Override
    public void closeOrder(OrderEntity entity) {
        // 查询当前这个订单的最新状态
        OrderEntity orderEntity = this.getById(entity.getId());
        if (OrderStatusEnum.CREATE_NEW.getCode().equals(orderEntity.getStatus())) {
            // 关单
            OrderEntity updEntity = new OrderEntity();
            updEntity.setId(orderEntity.getId());
            updEntity.setStatus(OrderStatusEnum.CANCLED.getCode());
            this.updateById(updEntity);

            OrderTo orderTo = new OrderTo();
            BeanUtils.copyProperties(orderEntity, orderTo);

            // 订单解锁成功发送消息给MQ
            try {
                // TODO: 2024/5/22 保证消息一定发送出去，每一个消息都日志记录（给数据库保存每一个消息的详细信息），定期扫描数据库，将失败的消息再发送一遍
                rabbitTemplate.convertAndSend("order-event-exchange", "order.release.other", orderTo,
                    new CorrelationData(UUID.randomUUID().toString()));
            } catch (AmqpException e) {
                // TODO: 2024/5/22 将没有发送成功的消息，再次发送出去
                throw new RuntimeException(e);
            }
        }
    }
}
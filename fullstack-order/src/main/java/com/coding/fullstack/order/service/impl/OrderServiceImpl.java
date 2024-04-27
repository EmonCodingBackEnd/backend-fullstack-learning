package com.coding.fullstack.order.service.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.common.vo.MemberEntityVo;
import com.coding.fullstack.order.dao.OrderDao;
import com.coding.fullstack.order.entity.OrderEntity;
import com.coding.fullstack.order.feign.CartFeignService;
import com.coding.fullstack.order.feign.MemberFeignService;
import com.coding.fullstack.order.interceptor.LoginUserInterceptor;
import com.coding.fullstack.order.service.OrderService;
import com.coding.fullstack.order.vo.MemberAddressVo;
import com.coding.fullstack.order.vo.OrderConfirmVo;
import com.coding.fullstack.order.vo.OrderItemVo;

import lombok.RequiredArgsConstructor;

@Service("orderService")
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {
    private final MemberFeignService memberFeignService;
    private final CartFeignService cartFeignService;

    private final ThreadPoolExecutor executor;

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
            orderConfirmVo.setAddresss(address);
            RequestContextHolder.resetRequestAttributes();
        }, executor);
        // 2、远程查询购物车所有选中的商品信息=>feign在远程调用之前要构造请求，调用很多拦截器RequestInterceptor
        CompletableFuture<Void> itemsFuture = CompletableFuture.runAsync(() -> {
            RequestContextHolder.setRequestAttributes(requestAttributes);
            List<OrderItemVo> items = cartFeignService.currentUserCartItems();
            orderConfirmVo.setItems(items);
            RequestContextHolder.resetRequestAttributes();
        }, executor);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(addressFuture, itemsFuture);
        allOf.get();

        // 3、查询用户积分
        orderConfirmVo.setIntegration(memberEntityVo.getIntegration());

        return orderConfirmVo;
    }
}
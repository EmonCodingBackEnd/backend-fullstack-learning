package com.coding.fullstack.cart.service.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.coding.common.utils.R;
import com.coding.fullstack.cart.dto.SkuInfoDto;
import com.coding.fullstack.cart.dto.UserInfoDto;
import com.coding.fullstack.cart.feign.ProductFeignService;
import com.coding.fullstack.cart.interceptor.CartInterceptor;
import com.coding.fullstack.cart.service.CartService;
import com.coding.fullstack.cart.vo.Cart;
import com.coding.fullstack.cart.vo.CartItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ProductFeignService productFeignService;
    private final ThreadPoolExecutor executor;

    public static final String CART_PREFIX = "fsmall:cart:";

    @Override
    public CartItem addToCart(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();

        Object o = operations.get(skuId.toString());
        if (o == null) {
            // 购物车无此商品
            // 将商品信息转为 CartItem 存储到 Redis 的购物车
            CartItem cartItem = new CartItem();
            // 1、远程查询当前要添加的商品的信息
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R info = productFeignService.info(skuId);
                SkuInfoDto skuInfo = info.getData("skuInfo", new TypeReference<SkuInfoDto>() {});
                cartItem.setSkuId(skuId);
                cartItem.setTitle(skuInfo.getSkuTitle());
                cartItem.setImage(skuInfo.getSkuDefaultImg());
                cartItem.setPrice(skuInfo.getPrice());
                cartItem.setCount(num);
            }, executor);

            // 2、远程查询sku的组合信息
            CompletableFuture<Void> skuAttrsFuture = CompletableFuture.runAsync(() -> {
                R info = productFeignService.getSkuSaleAttrValues(skuId);
                List<String> skuAttr = info.getData(new TypeReference<List<String>>() {});
                cartItem.setSkuAttr(skuAttr);
            }, executor);

            try {
                CompletableFuture.allOf(skuInfoFuture, skuAttrsFuture).get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            String s = JSON.toJSONString(cartItem);
            operations.put(skuId.toString(), s);
            return cartItem;
        } else {
            // 购物车有此商品
            CartItem cartItem = JSON.parseObject((String)o, CartItem.class);
            cartItem.setCount(cartItem.getCount() + num);
            operations.put(skuId.toString(), JSON.toJSONString(cartItem));
            return cartItem;
        }
    }

    /**
     * 获取到我们要操作的购物车
     *
     * @return
     */
    private BoundHashOperations<String, Object, Object> getCartOps() {
        UserInfoDto userInfoDto = CartInterceptor.threadLocal.get();

        String cartKey;
        if (userInfoDto.getUserId() != null) {
            cartKey = CART_PREFIX + userInfoDto.getUserId();
        } else {
            cartKey = CART_PREFIX + userInfoDto.getUserKey();
        }

        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        return operations;
    }

    @Override
    public CartItem getCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> operations = getCartOps();
        Object o = operations.get(skuId.toString());
        return JSON.parseObject((String)o, CartItem.class);
    }

    /**
     * 获取整个购物车
     * 
     * @return
     */
    @Override
    public Cart getCart() {
        Cart cart = new Cart();
        UserInfoDto userInfoDto = CartInterceptor.threadLocal.get();
        // 登录了
        if (userInfoDto.getUserId() != null) {
            // 如果临时购物车的数据还没有进行合并【合并购物车】
            String tempCartKey = CART_PREFIX + userInfoDto.getUserKey();
            List<CartItem> tempCartItems = getCartItems(tempCartKey);
            // 如果临时购物车有数据需要合并
            if (tempCartItems != null) {
                for (CartItem item : tempCartItems) {
                    addToCart(item.getSkuId(), item.getCount());
                }
            }
            // 清空临时购物车数据
            clearCart(tempCartKey);

            // 获取登录后的购物车数据【包含合并过来的临时购物车数据，和登录后的购物车的数据】
            String cartKey = CART_PREFIX + userInfoDto.getUserId();
            List<CartItem> collect = getCartItems(cartKey);
            cart.setItems(collect);
        }
        // 未登录
        else {
            String cartKey = CART_PREFIX + userInfoDto.getUserKey();
            List<CartItem> collect = getCartItems(cartKey);
            cart.setItems(collect);
        }
        return cart;
    }

    private List<CartItem> getCartItems(String cartKey) {
        BoundHashOperations<String, Object, Object> operations = stringRedisTemplate.boundHashOps(cartKey);
        List<Object> values = operations.values();
        if (values != null && values.size() > 0) {
            return values.stream().map(o -> JSON.parseObject((String)o, CartItem.class)).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public void clearCart(String cartKey) {
        // 清空临时购物车数据
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public void checkItem(Long skuId, Integer check) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCheck(check == 1);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void countItem(Long skuId, Integer num) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        CartItem cartItem = getCartItem(skuId);
        cartItem.setCount(num);
        cartOps.put(skuId.toString(), JSON.toJSONString(cartItem));
    }

    @Override
    public void deleteItem(Long skuId) {
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        cartOps.delete(skuId.toString());
    }
}

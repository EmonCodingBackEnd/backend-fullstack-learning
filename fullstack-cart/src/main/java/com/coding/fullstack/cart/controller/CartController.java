package com.coding.fullstack.cart.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.coding.fullstack.cart.service.CartService;
import com.coding.fullstack.cart.vo.Cart;
import com.coding.fullstack.cart.vo.CartItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping("/deleteItem")
    public String deleteItem(@RequestParam("skuId") Long skuId) {
        cartService.deleteItem(skuId);
        return "redirect:http://cart.fsmall.com/cart.html";
    }

    @GetMapping("/checkItem")
    public String checkItem(@RequestParam("skuId") Long skuId, @RequestParam("check") Integer check) {
        cartService.checkItem(skuId, check);
        return "redirect:http://cart.fsmall.com/cart.html";
    }

    @GetMapping("/countItem")
    public String countItem(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num) {
        cartService.countItem(skuId, num);
        return "redirect:http://cart.fsmall.com/cart.html";
    }

    // @formatter:off
    /**
     * 浏览器有一个cookie；user-key；标识用户身份，一个月后过期；
     * 如果第一次使用jd的购物车功能，都会给一个临时的用户身份；
     * 登录了，session有；如果没有登录；按照cookie里面user-key来取数据；
     * 第一次使用购物车功能，都会进行创建一个临时的用户身份；
     *
     * @return
     */
    // @formatter:on
    @GetMapping("/cart.html")
    public String cartListPage(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cartList";
    }

    // @formatter:off
    /**
     * 添加商品到购物车
     * addFlashAttribute - 将数据放在session里面可以在页面取出，但是只能取出一次。
     * addAttribute - 将数据放在url后面
     */
    // @formatter:on

    @GetMapping("/addToCart")
    public String addToCart(@RequestParam("skuId") Long skuId, @RequestParam("num") Integer num,
        RedirectAttributes ra) {
        CartItem cartItem = cartService.addToCart(skuId, num);

        ra.addAttribute("skuId", skuId); // model中的数据，在重定向后会加入新页面的请求参数中
        // 避免添加到购物车成功页面刷新，而导致的重复添加
        return "redirect:http://cart.fsmall.com/addToCartSuccess.html";
    }

    @GetMapping("/addToCartSuccess.html")
    public String success(@RequestParam("skuId") Long skuId, Model model) {
        // 为了让重定向后的页面也能正常显示数据，且刷新也能显示，最好是让skuId传递过来，再次查询
        CartItem cartItem = cartService.getCartItem(skuId);
        model.addAttribute("item", cartItem);
        return "success";
    }
}

package com.coding.fullstack.cart.interceptor;

import java.util.UUID;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.coding.common.constant.AuthConstant;
import com.coding.common.constant.CartConstant;
import com.coding.common.vo.MemberEntityVo;
import com.coding.fullstack.cart.dto.UserInfoDto;

/**
 * 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标方法的request
 */
@Component
public class CartInterceptor implements HandlerInterceptor {
    public static ThreadLocal<UserInfoDto> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        UserInfoDto userInfoDto = new UserInfoDto();
        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo)session.getAttribute(AuthConstant.LOGIN_USER);
        if (member != null) {
            // 用户登录了
            userInfoDto.setUserId(member.getId());
        }
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (CartConstant.TEMP_USER_COOKIE_NAME.equals(name)) {
                    userInfoDto.setUserKey(cookie.getValue());
                    userInfoDto.setTempUser(true);
                    break;
                }
            }
        }

        // 如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoDto.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoDto.setUserKey(uuid);
        }
        threadLocal.set(userInfoDto);
        return true;
    }

    /**
     * 业务执行之后：分配临时用户，让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        UserInfoDto userInfoDto = threadLocal.get();
        if (!userInfoDto.isTempUser()) {
            Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoDto.getUserKey());
            cookie.setDomain("fsmall.com");
            cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }
        threadLocal.remove();
    }
}

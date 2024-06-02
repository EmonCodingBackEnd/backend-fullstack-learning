package com.coding.fullstack.seckill.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.coding.common.constant.AuthConstant;
import com.coding.common.vo.MemberEntityVo;

import lombok.extern.slf4j.Slf4j;

/**
 * 在执行目标方法之前，判断用户的登录状态。并封装传递给controller目标方法的request
 */
@Slf4j
@Component
public class LoginUserInterceptor implements HandlerInterceptor {
    public static ThreadLocal<MemberEntityVo> threadLocal = new ThreadLocal<>();

    /**
     * 目标方法执行之前
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        log.info("拦截请求：uri={} method={}", request.getRequestURI(), request.getMethod());
        HttpSession session = request.getSession();
        MemberEntityVo member = (MemberEntityVo)session.getAttribute(AuthConstant.LOGIN_USER);
        if (member != null) {
            // 用户登录了
            threadLocal.set(member);
            return true;
        } else {
            // 用户没有登录
            session.setAttribute("msg", "请先进行登录");
            response.sendRedirect("http://auth.fsmall.com/login.html");
            return false;
        }
    }

    /**
     * 业务执行之后：分配临时用户，让浏览器保存
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
        ModelAndView modelAndView) throws Exception {
        log.info("拦截响应：uri={} method={}", request.getRequestURI(), request.getMethod());
        request.getSession().removeAttribute("msg");
    }
}

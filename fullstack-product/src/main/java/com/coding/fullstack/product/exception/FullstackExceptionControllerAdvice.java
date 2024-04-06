package com.coding.fullstack.product.exception;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.coding.common.exception.BizCodeEnum;
import com.coding.common.utils.R;

import lombok.extern.slf4j.Slf4j;

/**
 * 集中处理所有的异常
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.coding.fullstack.product.controller")
public class FullstackExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleValidException(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getFieldErrors().forEach(fieldError -> errorMap.put(fieldError.getField(), fieldError.getDefaultMessage()));
        return R.error(BizCodeEnum.VALID_EXCEPTION.getCode(), BizCodeEnum.VALID_EXCEPTION.getMsg()).put("data",
            errorMap);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleValidException(Throwable e) {
        String message = ExceptionUtils.getMessage(e);
        return R.error(BizCodeEnum.UNKNOW_EXCEPTION.getCode(), BizCodeEnum.UNKNOW_EXCEPTION.getMsg() + ":" + message);
    }
}

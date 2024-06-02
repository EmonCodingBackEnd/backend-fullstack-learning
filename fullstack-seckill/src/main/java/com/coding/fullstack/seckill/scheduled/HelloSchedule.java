package com.coding.fullstack.seckill.scheduled;

import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

// @formatter:off
/**
 * 定时任务
 *  1、EnableScheduling开启定时任务
 *  2、@Scheduled开启一个定时任务
 *  3、自动配置类org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration
 * 异步任务：
 *  1、@EnableAsync开启异步任务功能
 *  2、@Async给希望异步执行的方法上标注
 *  3、自动配置类org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
 */
// @formatter:on
@Slf4j
@Component
//@EnableAsync
//@EnableScheduling
public class HelloSchedule {

   // @formatter:off
    /**
     * 1、Spring中6位组成，不允许第7位的年
     * 2、在周几的位置，1-7分别代表周一到周五；MON-SUN
     * 3、定时任务不应该阻塞。默认是阻塞的
     *  1）、可以让业务运行以异步的方式，自己提交到线程池
     *      CompletableFuture.runAsync(()->{},xxx);
     *  2）、支持定时任务线程池
     *      spring.task.execution.pool.core-size
     *  3）、让定时任务异步执行
     * 解决：使用异步任务+定时任务来完成定时任务不阻塞的功能
     */
   // @formatter:on
    // @Scheduled(cron = "* * * * * ?")
    @Scheduled(cron = "* * * ? * 1")
    @Async
    // @Scheduled(cron = "*/5 * * ? * 1")
    public void hello() throws InterruptedException {
        log.info("hello......");
        Thread.sleep(3000);
    }
}

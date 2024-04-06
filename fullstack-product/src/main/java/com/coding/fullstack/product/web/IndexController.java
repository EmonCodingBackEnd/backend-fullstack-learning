package com.coding.fullstack.product.web;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.redisson.api.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.service.CategoryService;
import com.coding.fullstack.product.vo.Catelog2Vo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexController {

    private final CategoryService categoryService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @GetMapping({"/", "/index.html"})
    public String indexPage(Model model) {
        // 1、查询所有的一级分类
        List<CategoryEntity> categoryEntityList = categoryService.getLevel1Categorys();
        model.addAttribute("categorys", categoryEntityList);
        // 视图解析器进行拼串
        // classpath:/templates/ .html

        return "index";
    }

    @ResponseBody
    @GetMapping({"/index/catalog.json"})
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        Map<String, List<Catelog2Vo>> catalogJson = categoryService.getCatalogJson();
        return catalogJson;
    }

    @ResponseBody
    @GetMapping({"/hello"})
    public String hello() {
        // 获取一把锁，只要锁的名字一样，就是同一把锁。
        RLock lock = redissonClient.getLock("normal-lock");
        try {
            lock.lock(); // 阻塞等待，看门狗机制默认锁30秒在程序执行期间可自动续期，程序中断后不在续期过期自动删除。
            log.info("加锁成功，执行业务...{}", Thread.currentThread().getId());
            // 睡眠30秒
            Thread.sleep(30000);

            /*lock.lock(10, TimeUnit.SECONDS); // 丢失了看门狗机制，保持了阻塞等待
            log.info("加锁成功，执行业务...{}", Thread.currentThread().getId());
            // 睡眠30秒
            Thread.sleep(30000);*/

            /*boolean lockResult = lock.tryLock(0, 10, TimeUnit.SECONDS);
            if (lockResult) {
                log.info("加锁成功，执行业务...{}", Thread.currentThread().getId());
                // 睡眠30秒
                Thread.sleep(30000);
            } else {
                log.info("加锁失败，返回结果...{}", Thread.currentThread().getId());
                return "锁被占用";
            }*/
        } catch (Exception ignored) {
        } finally {
            log.info("释放锁...{}", Thread.currentThread().getId());
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return "hello";
    }

    @ResponseBody
    @GetMapping({"/write"})
    public String writeValue() {
        String uuid = "";
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        try {
            readWriteLock.writeLock().lock();
            log.info("写锁加锁成功...{}", Thread.currentThread().getId());
            uuid = UUID.randomUUID().toString();
            Thread.sleep(30000);
            stringRedisTemplate.opsForValue().set("writeValue", uuid);
        } catch (Exception ignored) {
        } finally {
            readWriteLock.writeLock().unlock();
            log.info("写锁释放成功...{}", Thread.currentThread().getId());
        }
        return uuid;
    }

    @ResponseBody
    @GetMapping({"/read"})
    public String readValue() {
        String uuid = "";
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("rw-lock");
        try {
            readWriteLock.readLock().lock();
            log.info("读锁加锁成功...{}", Thread.currentThread().getId());
            Thread.sleep(30000);
            uuid = stringRedisTemplate.opsForValue().get("writeValue");
        } catch (Exception ignored) {
        } finally {
            readWriteLock.readLock().unlock();
            log.info("读锁释放成功...{}", Thread.currentThread().getId());
        }
        return uuid;
    }

    @ResponseBody
    @GetMapping({"/park"})
    public String park() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        boolean permitResult = park.trySetPermits(2);
        log.info("设置信号量数量结果=>{}", permitResult); // 只有初次可成功！
        // park.acquire();
        boolean b = park.tryAcquire();// 获取一个信号量，获取一个车位
        if (b) {
            log.info("获取车位...{}", Thread.currentThread().getId());
        } else {
            log.info("获取车位失败...{}", Thread.currentThread().getId());
        }
        return "ok=>" + b;
    }

    @GetMapping("/go")
    @ResponseBody
    public String go() throws InterruptedException {
        RSemaphore park = redissonClient.getSemaphore("park");
        park.release(); // 释放车位
        return "ok";
    }

    /**
     * 放假，锁门 1班没人了，2班... 5个班全部走完，我们可以锁大门
     */
    @GetMapping("/lockDoor")
    @ResponseBody
    public String lockDoor() throws InterruptedException {
        RCountDownLatch door = redissonClient.getCountDownLatch("door");
        door.trySetCount(5);
        door.await(); // 等待闭锁都完成
        return "放假了...";
    }

    @GetMapping("/gogogo/{id}")
    @ResponseBody
    public String gogogo(@PathVariable("id") Long id) {
        redissonClient.getCountDownLatch("door").countDown();
        return id + "班的人都走了...";
    }
}

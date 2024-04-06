package com.coding.fullstack.product;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.coding.fullstack.product.entity.BrandEntity;
import com.coding.fullstack.product.service.BrandService;

@SpringBootTest
class FullstackProductApplicationTests {

    @Autowired
    BrandService brandService;

    @Test
    void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setDescript("");
        brandEntity.setName("华为");
        brandService.save(brandEntity);
        System.out.println("保存成功......");
    }

    @Test
    void test1() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setDescript("yyds");
        brandService.updateById(brandEntity);
        System.out.println("更新成功");
    }

    @Test
    void test2() {
        List<BrandEntity> list =
            brandService.list(new QueryWrapper<BrandEntity>().lambda().eq(BrandEntity::getBrandId, 1L));
        list.forEach(System.out::println);
    }

    @Test
    void testAccessKeyId() {
        // 从环境变量中获取RAM用户的访问密钥（AccessKey ID和AccessKey Secret）。
        String accessKeyId = System.getenv("OSS_ACCESS_KEY_ID");
        String accessKeySecret = System.getenv("OSS_ACCESS_KEY_SECRET");
        System.out.println(accessKeyId);
        System.out.println(accessKeySecret);
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void testRedis() {
        stringRedisTemplate.opsForValue().set("hello", "world_" + System.currentTimeMillis());
        String hello = stringRedisTemplate.opsForValue().get("hello");
        System.out.println(hello);
    }

    @Autowired
    private RedissonClient redissonClient;

    @Test
    void testRedisson() {
        RBucket<String> bucket = redissonClient.getBucket("hi");
        bucket.set("redisson");
        String hello = bucket.get();
        System.out.println(hello);
    }

}

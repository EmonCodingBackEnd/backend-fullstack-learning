package com.coding.fullstack.product.service.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.Query;
import com.coding.fullstack.product.dao.CategoryDao;
import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.service.CategoryBrandRelationService;
import com.coding.fullstack.product.service.CategoryService;
import com.coding.fullstack.product.vo.Catelog2Vo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("categoryService")
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    private final CategoryBrandRelationService categoryBrandRelationService;
    private final StringRedisTemplate stringRedisTemplate;
    private final RedissonClient redissonClient;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page =
            this.page(new Query<CategoryEntity>().getPage(params), new QueryWrapper<CategoryEntity>());

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        // 1、查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
        // 2、组装成父子的树形结构
        // 2.1、找出所有的一级分类
        return categoryEntities.stream().filter(categoryEntity -> categoryEntity.getParentCid() == 0)
            .peek(menu -> menu.setChildren(getChildrens(menu, categoryEntities)))
            .sorted(Comparator.comparing(CategoryEntity::getSortDefault0)).collect(Collectors.toList());
    }

    private List<CategoryEntity> getChildrens(CategoryEntity root, List<CategoryEntity> all) {
        return all.stream().filter(categoryEntity -> categoryEntity.getParentCid().equals(root.getCatId()))
            .peek(menu -> menu.setChildren(getChildrens(menu, all)))
            .sorted(Comparator.comparing(CategoryEntity::getSortDefault0)).collect(Collectors.toList());
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO: 2024/3/4 检查当前删除的菜单是否被别的地方引用过
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();
        findParentPath(catelogId, paths);

        return paths.toArray(new Long[0]);
    }

    private void findParentPath(Long catelogId, List<Long> paths) {
        CategoryEntity category = this.getById(catelogId);
        if (category != null && category.getParentCid() != 0) {
            findParentPath(category.getParentCid(), paths);
        }

        paths.add(catelogId);
    }

    /*@Caching(evict = {@CacheEvict(value = "category", key = "'catelogLevel1'"),
        @CacheEvict(value = "category", key = "'catalogJson'")})*/
    @CacheEvict(value = "category", allEntries = true) // 失效模式，删除某个分区下所有数据，等效于@Caching
    // @CachePut // 双写模式，需要方法具有返回值，会缓存返回值
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @Override
    public boolean updateCascade(CategoryEntity category) {
        boolean result = this.updateById(category);
        if (!result) {
            return false;
        }

        if (StringUtils.isNotEmpty(category.getName())) {
            // 同步更新其他关联表中的数据
            categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
        }

        return true;
    }

    // @formatter:off
    /**
     * 代表当前方法的结果需要缓存，如果缓存中有，方法不用调用。如果缓存中没有，调用方法，将方法结果放入缓存
     * 示例：
     * Cacheable (value = "category") => category::SimpleKey []
     * Cacheable(value = "category", key = "#root.method.name") => category::getLevel1Categorys
     * Cacheable(value = "category", key = "'catelogLevel1'") => category::catelogLevel1
     * @return
     */
    // @formatter:on

    @Cacheable(value = "category", key = "'catelogLevel1'")
    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        List<CategoryEntity> categoryEntities =
            baseMapper.selectList(Wrappers.lambdaQuery(CategoryEntity.class).eq(CategoryEntity::getParentCid, 0));
        return categoryEntities;
    }

    @Cacheable(value = "category", key = "'catalogJson'")
    @Override
    public Map<String, List<Catelog2Vo>> getCatalogJson() {
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        // 1、查询所有的一级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();

        // 2、封装数据
        Map<String, List<Catelog2Vo>> map =
            level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 遍历每一个一级分类，查询到一级分类对应的二级分类
                List<CategoryEntity> level2Categorys = getCategorysByParentCid(categoryEntities, v);
                // 封装数据
                List<Catelog2Vo> catelog2Vos = null;
                if (level2Categorys != null) {
                    catelog2Vos = level2Categorys.stream().map(item2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(item2.getParentCid().toString(), null,
                            item2.getCatId().toString(), item2.getName());
                        // 查询到二级分类对应的三级分类
                        List<CategoryEntity> level3Categorys = getCategorysByParentCid(categoryEntities, item2);
                        // 封装数据
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                        if (level3Categorys != null) {
                            catelog3Vos = level3Categorys.stream()
                                .map(item3 -> new Catelog2Vo.Catelog3Vo(item3.getParentCid().toString(),
                                    item3.getCatId().toString(), item3.getName()))
                                .collect(Collectors.toList());
                        }
                        catelog2Vo.setCatalog3List(catelog3Vos);
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
        return map;
    }

    public Map<String, List<Catelog2Vo>> getCatalogJsonCustomCache() {
        /*
        1、空结果缓存：解决缓存穿透
        2、设置过期时间（加随机值）：解决缓存雪崩
        3、加锁：解决缓存击穿
         */
        Map<String, List<Catelog2Vo>> catalogJsonMap;
        // 1、加入缓存逻辑
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isEmpty(catalogJSON)) {
            log.info("======>缓存未命中!即将查询数据库！");
            // 2、缓存中没有，查询数据库获取并加入缓存
            catalogJsonMap = getCatalogJsonFromDbWithLock();
            return catalogJsonMap;
        }
        log.info("======>缓存命中!即将返回！");
        // 3、缓存中有，直接返回
        catalogJsonMap = JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
        return catalogJsonMap;
    }

    // @formatter:off
    /**
     * 缓存里面的数据如何和数据库保持一致
     *
     * 缓存数据一致性问题
     * 1）双写模式
     * 2）失效模式
     * @return
     */
    // @formatter:on

    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDbWithLock() {
        /*
        第一种：本地锁
        本地锁：synchronized，JUC（Lock），在分布式情况下，想要锁住所有，必须使用分布式锁。
        只要是同一把锁，就能锁住需要这个锁的所有线程。由于SpringBoot所有的组件在容器中都是单例的，使用synchronized(this)可以锁住。
        */
        /*Map<String, List<Catelog2Vo>> catalogJsonMap;
        synchronized (this) {
            catalogJsonMap = getCatalogJsonFromDb();
            return catalogJsonMap;
        }*/

        /*
        第二种：分布式锁
        1、占分布式锁：去redis占坑，去redis占锁。
         */
        /*// TODO: 2024/4/4 该方法有一个弊端，若业务执行超时锁的时间，也会释放锁，无法避免其他线程的进入！另外也无法重入具有同一把锁的其他方法！
        String uuid = UUID.randomUUID().toString();
        // 设置过期时间必须是原子操作
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS); // 原子操作
        if (Boolean.TRUE.equals(lock)) {
            log.info("获取分布式锁成功！");
            Map<String, List<Catelog2Vo>> catalogJsonFromDb;
            try {
                catalogJsonFromDb = getCatalogJsonFromDb();
            } finally {
                // 删除锁，非原子操作，废弃
                // String lockValue = stringRedisTemplate.opsForValue().get("lock");
                // if (uuid.equals(lockValue)) {
                // stringRedisTemplate.delete("lock");
                // }
                // 删除锁，必须原子操作，使用lua脚本
                String script =
                    "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 1成功0失败
                Long delResult = stringRedisTemplate.execute(new DefaultRedisScript<>(script, Long.class),
                    Collections.singletonList("lock"), uuid);
            }
            return catalogJsonFromDb;
        } else {
            log.info("获取分布式锁失败，等待300ms后再试！");
            try {
                TimeUnit.MILLISECONDS.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return getCatalogJsonFromDbWithLock();
        }*/

        /*
        第三种：分布式锁
        1、占分布式锁：去redis占坑，去redis占锁。
         */
        RLock lock = redissonClient.getLock("catalogJson-lock");
        lock.lock();
        Map<String, List<Catelog2Vo>> catalogJsonFromDb;
        try {
            catalogJsonFromDb = getCatalogJsonFromDb();
        } finally {
            lock.unlock();
        }
        return catalogJsonFromDb;
    }

    private Map<String, List<Catelog2Vo>> getCatalogJsonFromDb() {
        // 得到锁以后，我们应该再去缓存中确定一次，如果没有才需要继续查询
        String catalogJSON = stringRedisTemplate.opsForValue().get("catalogJson");
        if (StringUtils.isNotEmpty(catalogJSON)) {
            Map<String, List<Catelog2Vo>> catalogJsonMap =
                JSON.parseObject(catalogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {});
            return catalogJsonMap;
        }
        log.info("======>查询了数据库！");

        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);

        // 1、查询所有的一级分类
        List<CategoryEntity> level1Categorys = getLevel1Categorys();

        // 2、封装数据
        Map<String, List<Catelog2Vo>> map =
            level1Categorys.stream().collect(Collectors.toMap(k -> k.getCatId().toString(), v -> {
                // 遍历每一个一级分类，查询到一级分类对应的二级分类
                List<CategoryEntity> level2Categorys = getCategorysByParentCid(categoryEntities, v);
                // 封装数据
                List<Catelog2Vo> catelog2Vos = null;
                if (level2Categorys != null) {
                    catelog2Vos = level2Categorys.stream().map(item2 -> {
                        Catelog2Vo catelog2Vo = new Catelog2Vo(item2.getParentCid().toString(), null,
                            item2.getCatId().toString(), item2.getName());
                        // 查询到二级分类对应的三级分类
                        List<CategoryEntity> level3Categorys = getCategorysByParentCid(categoryEntities, item2);
                        // 封装数据
                        List<Catelog2Vo.Catelog3Vo> catelog3Vos = null;
                        if (level3Categorys != null) {
                            catelog3Vos = level3Categorys.stream()
                                .map(item3 -> new Catelog2Vo.Catelog3Vo(item3.getParentCid().toString(),
                                    item3.getCatId().toString(), item3.getName()))
                                .collect(Collectors.toList());
                        }
                        catelog2Vo.setCatalog3List(catelog3Vos);
                        return catelog2Vo;
                    }).collect(Collectors.toList());
                }
                return catelog2Vos;
            }));
        // 查询到的数据再放入缓存
        String json = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set("catalogJson", json, 1, TimeUnit.DAYS);

        return map;
    }

    private List<CategoryEntity> getCategorysByParentCid(List<CategoryEntity> list, CategoryEntity v) {
        return list.stream().filter(item -> item.getParentCid().equals(v.getCatId())).collect(Collectors.toList());
    }
}
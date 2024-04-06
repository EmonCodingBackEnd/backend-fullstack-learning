package com.coding.fullstack.product.app;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.coding.common.utils.R;
import com.coding.fullstack.product.entity.CategoryEntity;
import com.coding.fullstack.product.service.CategoryService;

/**
 * 商品三级分类
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查出所有分类以及子分类，以树形结构组装起来
     */
    @GetMapping("/list/tree")
    // @RequiresPermissions("product:category:list")
    public R list() {
        List<CategoryEntity> list = categoryService.listWithTree();
        return R.ok().put("data", list);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{catId}")
    // @RequiresPermissions("product:category:info")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);
        return R.ok().put("data", category);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:category:save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);
        return R.ok();
    }

    /**
     * 级联更新所有关联的数据
     */
    @PostMapping("/update")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity category) {
        boolean result = categoryService.updateCascade(category);
        if (result) {
            return R.ok();
        } else {
            return R.error("更新失败！");
        }
    }

    /**
     * 批量修改
     */
    @PostMapping("/update/sort")
    // @RequiresPermissions("product:category:update")
    public R update(@RequestBody CategoryEntity[] category) {
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

    /**
     * 删除
     * 
     * @RequestBody: 获取请求体，必须发送POST请求，否则无法获取请求体
     */
    @PostMapping("/delete")
    // @RequiresPermissions("product:category:delete")
    public R delete(@RequestBody Long[] catIds) {
        // TODO: 2024/3/7 增加是否引用的判断
        categoryService.removeMenuByIds(Arrays.asList(catIds));
        return R.ok();
    }

}

package com.coding.fullstack.product.app;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.coding.common.utils.PageUtils;
import com.coding.common.utils.R;
import com.coding.common.validation.group.AddGroup;
import com.coding.common.validation.group.UpdateGroup;
import com.coding.fullstack.product.entity.BrandEntity;
import com.coding.fullstack.product.service.BrandService;

import lombok.extern.slf4j.Slf4j;

/**
 * 品牌
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@Slf4j
@RestController
@RequestMapping("product/brand")
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 列表
     */
    @GetMapping("/list")
    // @RequiresPermissions("product:brand:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = brandService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{brandId}")
    // @RequiresPermissions("product:brand:info")
    public R info(@PathVariable("brandId") Long brandId) {
        BrandEntity brand = brandService.getById(brandId);

        return R.ok().put("brand", brand);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:brand:save")
    public R save(@Validated({AddGroup.class}) @RequestBody BrandEntity brand/*, BindingResult bindingResult*/) {
        // if (bindingResult.hasErrors()) {
        // Map<String, String> map = new HashMap<>();
        // bindingResult.getFieldErrors().forEach(error -> {
        // String field = error.getField();
        // String defaultMessage = error.getDefaultMessage();
        // map.put(field, defaultMessage);
        // });
        // return R.error(400, "提交的数据不合法！").put("data", map);
        // } else {
        // brandService.save(brand);
        // }

        boolean result = brandService.save(brand);
        if (result) {
            return R.ok();
        } else {
            return R.error("保存失败！");
        }
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    // @RequiresPermissions("product:brand:update")
    public R update(@Validated({UpdateGroup.class}) @RequestBody BrandEntity brand) {
        boolean result = brandService.updateDetail(brand);
        if (result) {
            return R.ok();
        } else {
            return R.error("更新失败！");
        }
    }

    /**
     * 修改状态
     */
    @PostMapping("/update/status")
    // @RequiresPermissions("product:brand:update")
    public R updateStatus(@RequestBody BrandEntity brand) {
        if (brand.getBrandId() == null || brand.getShowStatus() == null) {
            log.error("品牌ID和品牌是否显示的状态不允许为空！");
            throw new RuntimeException("品牌ID和品牌是否显示的状态不允许为空！");
        }

        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(brand.getBrandId());
        brandEntity.setShowStatus(brand.getShowStatus());
        boolean result = brandService.updateById(brandEntity);
        if (result) {
            return R.ok();
        } else {
            return R.error("更新失败！");
        }
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    // @RequiresPermissions("product:brand:delete")
    public R delete(@RequestBody Long[] brandIds) {
        brandService.removeByIds(Arrays.asList(brandIds));

        return R.ok();
    }

}

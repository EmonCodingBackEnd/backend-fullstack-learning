package com.coding.fullstack.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.coding.common.utils.PageUtils;
import com.coding.common.utils.R;
import com.coding.fullstack.product.entity.AttrEntity;
import com.coding.fullstack.product.entity.AttrGroupEntity;
import com.coding.fullstack.product.service.AttrAttrgroupRelationService;
import com.coding.fullstack.product.service.AttrGroupService;
import com.coding.fullstack.product.service.AttrService;
import com.coding.fullstack.product.service.CategoryService;
import com.coding.fullstack.product.vo.AttrGroupRelationVo;
import com.coding.fullstack.product.vo.AttrGroupWithAttrsVo;

/**
 * 属性分组
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@RestController
@RequestMapping("product/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    AttrService attrService;

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    /**
     * 获取属性分组的关联的所有属性
     * 
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/attr/relation")
    public R attrRelation(@PathVariable("attrgroupId") Long attrgroupId) {
        List<AttrEntity> entities = attrService.getRelationAttr(attrgroupId);
        return R.ok().put("data", entities);
    }

    /**
     * 获取属性分组没有关联的其他属性
     *
     * @param attrgroupId
     * @return
     */
    @GetMapping("/{attrgroupId}/noattr/relation")
    public R attrNoRelation(@RequestParam Map<String, Object> params, @PathVariable("attrgroupId") Long attrgroupId) {
        PageUtils page = attrService.getRelationNoAttr(params, attrgroupId);
        return R.ok().put("page", page);
    }

    /**
     * 添加属性与分组关联关系
     * 
     * @param attrGroupRelationVos
     * @return
     */
    @PostMapping("/attr/relation")
    // @RequiresPermissions("product:attrgroup:attrrelation")
    public R addRelation(@RequestBody List<AttrGroupRelationVo> attrGroupRelationVos) {
        boolean result = attrAttrgroupRelationService.saveBatchAttrRelation(attrGroupRelationVos);
        if (result) {
            return R.ok();
        } else {
            return R.error("保存失败");
        }
    }

    /**
     * 获取分类下所有分组&关联属性
     * 
     * @param catelogId
     * @return
     */
    @GetMapping("/{catelogId}/withattr")
    public R getAttrGroupWithAttrs(@PathVariable("catelogId") Long catelogId) {
        List<AttrGroupWithAttrsVo> entities = attrGroupService.getAttrGroupWithAttrsByCatelogId(catelogId);
        return R.ok().put("data", entities);
    }

    /**
     * 删除属性与分组的关联关系
     *
     * @param attrGroups
     * @return
     */
    @PostMapping("/attr/relation/delete")
    public R relationDelete(@RequestBody List<AttrGroupRelationVo> attrGroups) {
        boolean result = attrService.deleteRelation(attrGroups);
        if (result) {
            return R.ok();
        } else {
            return R.error("删除失败");
        }
    }

    /**
     * 列表
     */
    @GetMapping("/list/{catelogId}")
    // @RequiresPermissions("product:attrgroup:list")
    public R list(@RequestParam Map<String, Object> params, @PathVariable("catelogId") Long catelogId) {
        PageUtils page = attrGroupService.queryPage(params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 信息
     */
    @GetMapping("/info/{attrGroupId}")
    // @RequiresPermissions("product:attrgroup:info")
    public R info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        Long[] categoryPath = categoryService.findCatelogPath(attrGroup.getCatelogId());
        attrGroup.setCatelogPath(categoryPath);

        return R.ok().put("attrGroup", attrGroup);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:attrgroup:save")
    public R save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    // @RequiresPermissions("product:attrgroup:update")
    public R update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    // @RequiresPermissions("product:attrgroup:delete")
    public R delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return R.ok();
    }

}

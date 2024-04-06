package com.coding.fullstack.product.app;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.coding.common.constant.ProductConstant;
import com.coding.common.utils.PageUtils;
import com.coding.common.utils.R;
import com.coding.fullstack.product.entity.ProductAttrValueEntity;
import com.coding.fullstack.product.service.AttrService;
import com.coding.fullstack.product.service.ProductAttrValueService;
import com.coding.fullstack.product.vo.AttrResponseVo;
import com.coding.fullstack.product.vo.AttrVo;

/**
 * 商品属性
 *
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@RestController
@RequestMapping("product/attr")
public class AttrController {
    @Autowired
    private AttrService attrService;
    @Autowired
    private ProductAttrValueService productAttrValueService;

    /**
     * 获取spu规格
     * 
     * @param spuId
     * @return
     */
    @GetMapping("/base/listforspu/{spuId}")
    public R baseAttrListforspu(@PathVariable("spuId") Long spuId) {
        List<ProductAttrValueEntity> list = productAttrValueService.baseAttrListForSpu(spuId);
        return R.ok().put("data", list);
    }

    /**
     * attrTypeName='base' 获取分类规格参数（基本属性） attrTypeName='sale' 获取分类销售属性
     */
    @GetMapping("/{attrTypeName}/list/{catelogId}")
    // @RequiresPermissions("product:attr:list")
    public R baseAttrList(@RequestParam Map<String, Object> params, @PathVariable("attrTypeName") String attrTypeName,
        @PathVariable("catelogId") Long catelogId) {
        int attrType = "base".equals(attrTypeName) ? ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()
            : ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode(); // 默认是规格参数（基本属性）
        PageUtils page = attrService.queryBaseOrSaleAttrPage(attrType, params, catelogId);

        return R.ok().put("page", page);
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    // @RequiresPermissions("product:attr:list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = attrService.queryPage(params);

        return R.ok().put("page", page);
    }

    /**
     * 查询属性详情
     */
    @GetMapping("/info/{attrId}")
    // @RequiresPermissions("product:attr:info")
    public R info(@PathVariable("attrId") Long attrId) {
        AttrResponseVo attr = attrService.getAttrInfo(attrId);

        return R.ok().put("attr", attr);
    }

    /**
     * 保存
     */
    @PostMapping("/save")
    // @RequiresPermissions("product:attr:save")
    public R save(@RequestBody AttrVo attr) {
        attrService.saveAttr(attr);

        return R.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    // @RequiresPermissions("product:attr:update")
    public R update(@RequestBody AttrVo attr) {
        attrService.updateAttr(attr);

        return R.ok();
    }

    /**
     * 修改商品规格
     */
    @PostMapping("/update/{spuId}")
    // @RequiresPermissions("product:attr:update")
    public R updateSpuAttr(@PathVariable("spuId") Long spuId, @RequestBody List<ProductAttrValueEntity> pavs) {
        productAttrValueService.updateSpuAttr(spuId, pavs);

        return R.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    // @RequiresPermissions("product:attr:delete")
    public R delete(@RequestBody Long[] attrIds) {
        attrService.removeByIds(Arrays.asList(attrIds));

        return R.ok();
    }

}

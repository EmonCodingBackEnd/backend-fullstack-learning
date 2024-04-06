package com.coding.fullstack.product.entity;

import java.io.Serializable;

import javax.validation.constraints.*;

import org.hibernate.validator.constraints.URL;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.coding.common.validation.constraints.ListValue;
import com.coding.common.validation.group.AddGroup;
import com.coding.common.validation.group.UpdateGroup;

import lombok.Data;

/**
 * 品牌
 * 
 * @author emon
 * @email emon@gmail.com
 * @date 2024-03-01 12:50:47
 */
@Data
@TableName("pms_brand")
public class BrandEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 品牌id
     */
    @NotNull(message = "修改必须指定id", groups = {UpdateGroup.class})
    @Null(message = "新增不能指定id", groups = {AddGroup.class})
    @TableId
    private Long brandId;
    /**
     * 品牌名
     */
    @NotBlank(message = "品牌名称必须提交", groups = {AddGroup.class, UpdateGroup.class})
    private String name;
    /**
     * 品牌logo地址
     */
    @NotEmpty(groups = {AddGroup.class})
    @URL(message = "logo必须是一个合法的url地址", groups = {AddGroup.class, UpdateGroup.class})
    private String logo;
    /**
     * 介绍
     */
    private String descript;
    /**
     * 显示状态[0-不显示；1-显示]
     */
    @NotNull(groups = {AddGroup.class})
    @ListValue(value = {0, 1}, groups = {AddGroup.class, UpdateGroup.class})
    private Integer showStatus;
    /**
     * 检索首字母
     */
    @NotEmpty(groups = {AddGroup.class})
    @Pattern(message = "检索首字母必须是一个字母", regexp = "^[a-zA-Z]$", groups = {AddGroup.class, UpdateGroup.class})
    private String firstLetter;
    /**
     * 排序
     */
    @NotNull(groups = {AddGroup.class})
    @Min(value = 0, message = "排序必须为大于等于0的数字", groups = {AddGroup.class, UpdateGroup.class})
    private Integer sort;

}

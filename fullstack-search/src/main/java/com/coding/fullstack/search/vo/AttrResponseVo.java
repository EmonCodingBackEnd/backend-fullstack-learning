package com.coding.fullstack.search.vo;

import lombok.Data;

@Data
public class AttrResponseVo extends AttrVo {
    private String groupName;
    private String catelogName;

    // 分类的三级路径[父/子/孙]
    private Long[] catelogPath;
}

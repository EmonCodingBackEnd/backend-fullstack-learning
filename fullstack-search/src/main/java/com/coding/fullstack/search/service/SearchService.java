package com.coding.fullstack.search.service;

import com.coding.fullstack.search.vo.SearchParam;
import com.coding.fullstack.search.vo.SearchResult;

public interface SearchService {
    /**
     * 搜索功能
     *
     * @param searchParam - 检索的所有参数
     * @return 返回检索的结果，里面包含页面需要的所有数据
     */
    SearchResult search(SearchParam searchParam);
}

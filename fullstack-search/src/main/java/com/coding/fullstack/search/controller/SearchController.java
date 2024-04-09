package com.coding.fullstack.search.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.coding.fullstack.search.service.SearchService;
import com.coding.fullstack.search.vo.SearchParam;
import com.coding.fullstack.search.vo.SearchResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * 自动将页面提交过来的所有请求查询参数封装成指定的对象
     * 
     * @param searchParam
     * @return
     */
    @GetMapping({"/list.html"})
    public String listPage(SearchParam searchParam, Model model, HttpServletRequest request) {
        String queryString = request.getQueryString();
        searchParam.setQueryString(queryString);

        SearchResult result = searchService.search(searchParam);
        model.addAttribute("result", result);
        // 视图解析器进行拼串
        // classpath:/templates/ .html
        return "list";
    }

}

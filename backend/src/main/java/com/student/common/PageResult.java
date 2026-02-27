package com.student.common;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    
    private List<T> records;
    
    private Long total;
    
    private Long size;
    
    private Long current;
    
    private Long pages;
    
    public PageResult(Page<T> page) {
        this.records = page.getRecords();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.current = page.getCurrent();
        this.pages = page.getPages();
    }
    
    public static <T> PageResult<T> of(Page<T> page) {
        return new PageResult<>(page);
    }
}

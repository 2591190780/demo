package com.example.entity;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private Long total;
    private List<T> records;
    private Integer current;
    private Integer pages;
    private Integer size;

    public static <T> PageResult<T> of(IPage<T> page) {
        return new PageResult<>(
                page.getTotal(),
                page.getRecords(),
                (int) page.getCurrent(),
                (int) page.getPages(),
                (int) page.getSize()
        );
    }
}
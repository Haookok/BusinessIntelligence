package com.businessintelligence.Infrastracture.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    private long total;         // 总记录数
    private int pageNum;        // 当前页码
    private int pageSize;       // 每页条数
    private List<T> records;    // 当前页数据
}

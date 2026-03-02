package com.pixiv.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应结果类
 * 用于封装分页查询的返回数据
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从 1 开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 构造分页结果
     *
     * @param records 数据列表
     * @param total   总记录数
     * @param page    当前页码
     * @param size    每页大小
     */
    public PageResult(List<T> records, Long total, Integer page, Integer size) {
        this.records = records;
        this.total = total;
        this.page = page;
        this.size = size;
        this.pages = (int) Math.ceil((double) total / size);
        this.hasPrevious = page > 1;
        this.hasNext = page < this.pages;
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty(Integer page, Integer size) {
        return new PageResult<>(List.of(), 0L, page, size, 0, false, false);
    }

    /**
     * 从 Spring Data 的 Page 对象转换
     */
    public static <T> PageResult<T> of(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(
                page.getContent(),
                page.getTotalElements(),
                page.getNumber() + 1, // Spring Data 的页码从 0 开始，我们从 1 开始
                page.getSize()
        );
    }
}

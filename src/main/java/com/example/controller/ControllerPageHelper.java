package com.example.controller;

import com.example.entity.PageParam;
import com.example.entity.PageResult;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

public class ControllerPageHelper {

    /**
     * 在Controller层对Service返回的列表进行分页
     * @param fullList 完整列表
     * @param pageParam 分页参数
     * @return 分页结果
     */
    public static <T> PageResult<T> paginateList(List<T> fullList, PageParam pageParam) {
        if (fullList == null || fullList.isEmpty()) {
            return new PageResult<>(0L, Collections.emptyList(), 0, 0, 0);
        }

        // 应用排序
        applySorting(fullList, pageParam);

        // 分页计算
        int total = fullList.size();
        int pageSize = pageParam.getPageSize();
        int totalPages = (int) Math.ceil((double) total / pageSize);
        int currentPage = Math.min(pageParam.getPageNum(), totalPages);
        int start = (currentPage - 1) * pageSize;
        int end = Math.min(start + pageSize, total);

        // 获取分页数据
        List<T> pageList = fullList.subList(start, end);

        return new PageResult<>(
                (long) total,
                pageList,
                currentPage,
                totalPages,
                pageSize
        );
    }

    /**
     * 应用内存排序
     */
    private static <T> void applySorting(List<T> list, PageParam param) {
        if (StringUtils.isEmpty(param.getSortField())) return;

        list.sort((a, b) -> {
            try {
                Object valA = getFieldValue(a, param.getSortField());
                Object valB = getFieldValue(b, param.getSortField());

                if (valA instanceof Comparable && valB instanceof Comparable) {
                    int result = ((Comparable) valA).compareTo(valB);
                    return "desc".equalsIgnoreCase(param.getSortOrder()) ? -result : result;
                }
            } catch (Exception e) {
                // 排序失败时保持原顺序
            }
            return 0;
        });
    }

    /**
     * 反射获取字段值
     */
    private static Object getFieldValue(Object obj, String fieldName)
            throws NoSuchFieldException, IllegalAccessException {

        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
}

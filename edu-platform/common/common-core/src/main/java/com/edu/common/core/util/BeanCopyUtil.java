package com.edu.common.core.util;

import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Bean 拷贝工具类
 */
public class BeanCopyUtil {

    private BeanCopyUtil() {}

    /**
     * 单对象属性拷贝
     */
    public static <T> T copyBean(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.getDeclaredConstructor().newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (Exception e) {
            throw new RuntimeException("Bean拷贝失败", e);
        }
    }

    /**
     * 列表对象属性拷贝
     */
    public static <T> List<T> copyList(List<?> sourceList, Class<T> targetClass) {
        if (sourceList == null || sourceList.isEmpty()) {
            return Collections.emptyList();
        }
        return sourceList.stream()
                .map(source -> copyBean(source, targetClass))
                .collect(Collectors.toList());
    }
}

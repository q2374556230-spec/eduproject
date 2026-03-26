package com.edu.order.client.fallback;

import com.edu.common.core.result.Result;
import com.edu.order.client.CourseFeign;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * CourseFeign 熔断降级处理
 */
@Slf4j
@Component
public class CourseFeignFallback implements CourseFeign {

    @Override
    public Result<Map<String, Object>> getCourseById(Long id) {
        log.warn("课程服务不可用，触发熔断降级，courseId={}", id);
        return Result.fail("课程服务暂时不可用，请稍后重试");
    }

    @Override
    public Result<Void> incrementStudentCount(Long id) {
        log.warn("更新学生数失败，触发熔断降级，courseId={}", id);
        return Result.fail("课程服务暂时不可用");
    }
}

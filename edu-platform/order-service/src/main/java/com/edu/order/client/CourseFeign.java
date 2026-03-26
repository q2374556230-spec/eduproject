package com.edu.order.client;

import com.edu.common.core.result.Result;
import com.edu.order.client.fallback.CourseFeignFallback;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.Map;

/**
 * 课程服务 Feign 客户端（带熔断）
 */
@FeignClient(name = "course-service", fallback = CourseFeignFallback.class)
public interface CourseFeign {

    @GetMapping("/api/course/{id}")
    Result<Map<String, Object>> getCourseById(@PathVariable("id") Long id);

    @PutMapping("/api/course/{id}/student-count")
    Result<Void> incrementStudentCount(@PathVariable("id") Long id);
}

package com.edu.course.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 课程查询请求DTO
 */
@Data
@Schema(description = "课程查询参数")
public class CourseQueryRequest {

    @Schema(description = "搜索关键词（标题/描述）")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "难度")
    private String level;

    @Schema(description = "最低价格")
    private Double minPrice;

    @Schema(description = "最高价格")
    private Double maxPrice;

    @Schema(description = "排序字段：created_at/price/student_count/rating")
    private String orderBy = "created_at";

    @Schema(description = "排序方向：asc/desc")
    private String orderDir = "desc";

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;
}

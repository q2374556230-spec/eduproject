package com.edu.course.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.edu.course.dto.CourseQueryRequest;
import com.edu.course.dto.CourseVO;
import com.edu.course.entity.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 课程 Mapper
 */
@Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 带分类名称的分页查询
     */
    IPage<CourseVO> selectCourseVOPage(
            Page<CourseVO> page,
            @Param("query") CourseQueryRequest query);
}

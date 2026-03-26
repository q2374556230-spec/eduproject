package com.edu.course.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.core.exception.BusinessException;
import com.edu.common.core.result.PageResult;
import com.edu.common.core.result.ResultCode;
import com.edu.common.core.util.BeanCopyUtil;
import com.edu.course.client.ClaudeAiClient;
import com.edu.course.dto.*;
import com.edu.course.entity.Category;
import com.edu.course.entity.Course;
import com.edu.course.mapper.CategoryMapper;
import com.edu.course.mapper.CourseMapper;
import com.edu.course.service.CourseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 课程服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final CourseMapper courseMapper;
    private final CategoryMapper categoryMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ClaudeAiClient claudeAiClient;

    private static final String COURSE_CACHE_PREFIX = "course:detail:";
    private static final String CATEGORY_CACHE_KEY = "course:categories";
    private static final long CACHE_TTL_MINUTES = 30;

    @Override
    public PageResult<CourseVO> listCourses(CourseQueryRequest query) {
        Page<CourseVO> page = new Page<>(query.getPage(), query.getSize());
        Page<CourseVO> resultPage = (Page<CourseVO>) courseMapper.selectCourseVOPage(page, query);
        return PageResult.of(
                resultPage.getCurrent(), resultPage.getSize(),
                resultPage.getTotal(), resultPage.getRecords());
    }

    @Override
    @SuppressWarnings("unchecked")
    public CourseVO getCourseById(Long id) {
        // 先查缓存
        String cacheKey = COURSE_CACHE_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached instanceof CourseVO vo) {
            return vo;
        }

        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        CourseVO vo = BeanCopyUtil.copyBean(course, CourseVO.class);

        // 查分类名称
        if (course.getCategoryId() != null) {
            Category category = categoryMapper.selectById(course.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 写入缓存
        redisTemplate.opsForValue().set(cacheKey, vo, CACHE_TTL_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVO createCourse(CourseRequest request, Long teacherId, String teacherName) {
        Course course = BeanCopyUtil.copyBean(request, Course.class);
        course.setTeacherId(teacherId);
        course.setTeacherName(teacherName);
        course.setStatus(0); // 草稿状态
        course.setStudentCount(0);
        save(course);

        return getCourseById(course.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CourseVO updateCourse(Long id, CourseRequest request) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        Course update = BeanCopyUtil.copyBean(request, Course.class);
        update.setId(id);
        updateById(update);

        // 清除缓存
        redisTemplate.delete(COURSE_CACHE_PREFIX + id);
        return getCourseById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCourse(Long id) {
        if (!removeById(id)) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        redisTemplate.delete(COURSE_CACHE_PREFIX + id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishCourse(Long id) {
        Course course = getById(id);
        if (course == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }
        course.setStatus(1);
        updateById(course);
        redisTemplate.delete(COURSE_CACHE_PREFIX + id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Category> listCategories() {
        // 分类列表缓存
        Object cached = redisTemplate.opsForValue().get(CATEGORY_CACHE_KEY);
        if (cached instanceof List) {
            return (List<Category>) cached;
        }
        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>()
                        .eq(Category::getStatus, 1)
                        .orderByAsc(Category::getSort));
        redisTemplate.opsForValue().set(CATEGORY_CACHE_KEY, categories, 60, TimeUnit.MINUTES);
        return categories;
    }

    @Override
    public List<CourseVO> getAiRecommendations(Long userId, String userInterest) {
        // 获取热门课程作为候选
        CourseQueryRequest query = new CourseQueryRequest();
        query.setOrderBy("student_count");
        query.setPage(1);
        query.setSize(20);
        PageResult<CourseVO> hotCourses = listCourses(query);

        if (hotCourses.getRecords().isEmpty()) {
            return List.of();
        }

        // 构建课程摘要传给AI
        String courseSummary = hotCourses.getRecords().stream()
                .map(c -> String.format("ID:%d 《%s》 分类:%s 难度:%s 价格:%.0f元",
                        c.getId(), c.getTitle(), c.getCategoryName(), c.getLevelDesc(), c.getPrice()))
                .collect(Collectors.joining("\n"));

        // 调用 Claude AI 获取推荐
        String prompt = String.format(
                "你是一个专业的在线教育顾问。以下是我们平台的课程列表：\n%s\n\n" +
                "用户兴趣/需求：%s\n\n" +
                "请从以上课程中推荐最适合该用户的3门课程，以JSON数组格式返回，格式为：\n" +
                "[{\"id\": 课程ID, \"reason\": \"推荐理由（50字内）\"}]\n" +
                "只返回JSON，不要其他内容。",
                courseSummary,
                userInterest != null ? userInterest : "全面提升技能"
        );

        try {
            String aiResponse = claudeAiClient.chat(prompt);
            return claudeAiClient.parseRecommendations(aiResponse, hotCourses.getRecords());
        } catch (Exception e) {
            log.warn("AI推荐服务异常，返回默认热门课程: {}", e.getMessage());
            // 降级：返回前3门热门课程
            return hotCourses.getRecords().stream().limit(3)
                    .peek(c -> c.setRecommendReason("热门推荐"))
                    .collect(Collectors.toList());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementStudentCount(Long courseId) {
        update(new LambdaUpdateWrapper<Course>()
                .eq(Course::getId, courseId)
                .setSql("student_count = student_count + 1"));
        redisTemplate.delete(COURSE_CACHE_PREFIX + courseId);
    }
}

package com.edu.order.service.impl;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.edu.common.core.exception.BusinessException;
import com.edu.common.core.result.PageResult;
import com.edu.common.core.result.Result;
import com.edu.common.core.result.ResultCode;
import com.edu.order.client.CourseFeign;
import com.edu.order.config.RabbitMQConfig;
import com.edu.order.dto.CreateOrderRequest;
import com.edu.order.entity.Order;
import com.edu.order.mapper.OrderMapper;
import com.edu.order.mq.OrderPaidEvent;
import com.edu.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 订单服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final CourseFeign courseFeign;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, String username, CreateOrderRequest request) {
        // 检查是否已购买
        long existing = count(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getCourseId, request.getCourseId())
                .in(Order::getStatus, 0, 1)); // 待支付或已支付
        if (existing > 0) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_EXISTS);
        }

        // 调用课程服务获取课程信息（带熔断）
        Result<Map<String, Object>> courseResult = courseFeign.getCourseById(request.getCourseId());
        if (!courseResult.isSuccess() || courseResult.getData() == null) {
            throw new BusinessException(ResultCode.COURSE_NOT_FOUND);
        }

        Map<String, Object> courseData = courseResult.getData();
        String courseTitle = (String) courseData.get("title");
        String courseCover = (String) courseData.get("coverImage");
        Object priceObj = courseData.get("price");
        BigDecimal price = priceObj != null
                ? new BigDecimal(priceObj.toString())
                : BigDecimal.ZERO;

        Order order = new Order();
        order.setOrderNo(IdUtil.getSnowflakeNextIdStr());
        order.setUserId(userId);
        order.setUsername(username);
        order.setCourseId(request.getCourseId());
        order.setCourseTitle(courseTitle);
        order.setCourseCover(courseCover);
        order.setAmount(price);
        order.setStatus(0); // 待支付
        order.setPayMethod(request.getPayMethod());

        save(order);
        log.info("订单创建成功: orderNo={}, userId={}, courseId={}", order.getOrderNo(), userId, request.getCourseId());
        return order;
    }

    @Override
    public PageResult<Order> listOrders(Long userId, Integer status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getCurrent(), pageResult.getSize(),
                pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public Order getOrderById(Long orderId, Long userId) {
        Order order = getById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 普通用户只能查看自己的订单
        if (userId != null && !order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order payOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 模拟支付成功
        order.setStatus(1);
        order.setPaidAt(LocalDateTime.now());
        updateById(order);

        // 通知课程服务更新学生数（Feign调用）
        try {
            courseFeign.incrementStudentCount(order.getCourseId());
        } catch (Exception e) {
            log.warn("更新课程学生数失败，将异步重试: {}", e.getMessage());
        }

        // 发送支付成功消息到 RabbitMQ
        OrderPaidEvent event = OrderPaidEvent.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .username(order.getUsername())
                .courseId(order.getCourseId())
                .courseTitle(order.getCourseTitle())
                .amount(order.getAmount())
                .paidAt(order.getPaidAt())
                .build();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.ORDER_EXCHANGE,
                RabbitMQConfig.ORDER_PAID_ROUTING_KEY,
                event
        );
        log.info("订单支付成功，消息已发送: orderNo={}", order.getOrderNo());

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId) {
        Order order = getOrderById(orderId, userId);
        if (order.getStatus() != 0) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }
        order.setStatus(2);
        updateById(order);
    }

    @Override
    public PageResult<Order> listAllOrders(Integer status, int page, int size) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .orderByDesc(Order::getCreatedAt);
        if (status != null) {
            wrapper.eq(Order::getStatus, status);
        }
        Page<Order> pageResult = page(new Page<>(page, size), wrapper);
        return PageResult.of(pageResult.getCurrent(), pageResult.getSize(),
                pageResult.getTotal(), pageResult.getRecords());
    }

    @Override
    public Map<String, Object> getStats() {
        long totalOrders = count();
        long pendingOrders = count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 0));
        long paidOrders = count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        long cancelledOrders = count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 2));
        long refundedOrders = count(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3));

        List<Order> paidList = list(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1));
        BigDecimal totalRevenue = paidList.stream()
                .map(Order::getAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return Map.of(
                "totalOrders", totalOrders,
                "totalRevenue", totalRevenue,
                "pendingOrders", pendingOrders,
                "paidOrders", paidOrders,
                "cancelledOrders", cancelledOrders,
                "refundedOrders", refundedOrders
        );
    }
}

package com.edu.order.controller;

import com.edu.common.core.result.PageResult;
import com.edu.common.core.result.Result;
import com.edu.common.security.context.UserContext;
import com.edu.order.dto.CreateOrderRequest;
import com.edu.order.entity.Order;
import com.edu.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单管理", description = "选课、订单、支付相关接口")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单（选课）")
    @PostMapping("/create")
    public Result<Order> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        Long userId = UserContext.getCurrentUserId();
        String username = UserContext.getCurrentUsername();
        return Result.success("选课成功", orderService.createOrder(userId, username, request));
    }

    @Operation(summary = "我的订单列表")
    @GetMapping("/list")
    public Result<PageResult<Order>> listOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(orderService.listOrders(userId, status, page, size));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{orderId}")
    public Result<Order> getOrder(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success(orderService.getOrderById(orderId, userId));
    }

    @Operation(summary = "模拟支付订单")
    @PostMapping("/{orderId}/pay")
    public Result<Order> payOrder(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        return Result.success("支付成功", orderService.payOrder(orderId, userId));
    }

    @Operation(summary = "取消订单")
    @PostMapping("/{orderId}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long orderId) {
        Long userId = UserContext.getCurrentUserId();
        orderService.cancelOrder(orderId, userId);
        return Result.success();
    }

    @Operation(summary = "所有订单列表（管理员）")
    @GetMapping("/admin/list")
    public Result<PageResult<Order>> listAllOrders(
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listAllOrders(status, page, size));
    }

    @Operation(summary = "订单统计数据（管理员）")
    @GetMapping("/admin/stats")
    public Result<java.util.Map<String, Object>> getStats() {
        return Result.success(orderService.getStats());
    }
}

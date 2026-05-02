package com.edu.order.service;

import com.edu.common.core.result.PageResult;
import com.edu.order.dto.CreateOrderRequest;
import com.edu.order.entity.Order;

import java.util.Map;

public interface OrderService {
    Order createOrder(Long userId, String username, CreateOrderRequest request);
    PageResult<Order> listOrders(Long userId, Integer status, int page, int size);
    Order getOrderById(Long orderId, Long userId);
    Order payOrder(Long orderId, Long userId);
    void cancelOrder(Long orderId, Long userId);
    PageResult<Order> listAllOrders(Integer status, int page, int size);
    Map<String, Object> getStats();
}

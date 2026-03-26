package com.edu.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.order.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}

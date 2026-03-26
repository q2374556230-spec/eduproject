package com.edu.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.edu.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户 Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名或邮箱查询用户
     */
    User findByUsernameOrEmail(@Param("account") String account);
}

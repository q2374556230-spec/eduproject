package com.edu.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求DTO
 */
@Data
@Schema(description = "更新用户信息请求")
public class UpdateProfileRequest {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "个人简介")
    @Size(max = 200, message = "简介不超过200字")
    private String bio;

    @Schema(description = "头像URL")
    private String avatar;
}

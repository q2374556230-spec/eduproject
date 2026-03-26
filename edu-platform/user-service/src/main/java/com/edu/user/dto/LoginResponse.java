package com.edu.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问Token")
    private String accessToken;

    @Schema(description = "Token类型", example = "Bearer")
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（毫秒）")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserVO user;

    public LoginResponse(String accessToken, Long expiresIn, UserVO user) {
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.user = user;
    }
}

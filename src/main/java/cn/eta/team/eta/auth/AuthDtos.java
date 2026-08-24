// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import cn.eta.team.eta.common.Result;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 认证相关 DTO。对外暴露数据通过 {@link UserVO}，避免泄露密码等敏感字段。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
public final class AuthDtos {

    private AuthDtos() {
    }

    public record RegisterRequest(
            @NotBlank(message = "昵称不能为空")
            @Size(max = 64, message = "昵称过长")
            String nickname,
            @NotBlank(message = "邮箱不能为空")
            @Email(message = "邮箱格式不正确")
            String email,
            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 64, message = "密码长度 6-64 位")
            String password) {
    }

    public record LoginRequest(
            @NotBlank(message = "邮箱不能为空") String email,
            @NotBlank(message = "密码不能为空") String password) {
    }

    public record ChangePasswordRequest(
            @NotBlank(message = "旧密码不能为空") String oldPassword,
            @NotBlank(message = "新密码不能为空")
            @Size(min = 6, max = 64, message = "新密码长度 6-64 位") String newPassword) {
    }

    /** 登录 / 注册返回：token + 用户信息 */
    public record LoginResponse(String token, UserVO user) {
    }

    /** 对外暴露的用户信息（脱敏） */
    public record UserVO(Long id, String nickname, String email, String avatar, String bio, String createdAt) {

        public static UserVO from(User u) {
            return new UserVO(u.getId(), u.getNickname(), u.getEmail(), u.getAvatar(), u.getBio(),
                    u.getCreatedAt() == null ? null : u.getCreatedAt().toString());
        }
    }

    /** 更新资料请求 */
    public record UpdateProfileRequest(
            @NotBlank(message = "昵称不能为空") @Size(max = 64, message = "昵称过长") String nickname,
            @Size(max = 512, message = "简介过长") String bio,
            @Size(max = 512, message = "头像链接过长") String avatar) {
    }
}
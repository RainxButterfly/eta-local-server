// SPDX-FileCopyrightText: 2026 RainxButterfly 
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.security.JwtService;
import cn.eta.team.eta.security.EtaPrincipal;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import cn.eta.team.eta.auth.AuthDtos.LoginRequest;
import cn.eta.team.eta.auth.AuthDtos.LoginResponse;
import cn.eta.team.eta.auth.AuthDtos.RegisterRequest;
import cn.eta.team.eta.auth.AuthDtos.UpdateProfileRequest;
import cn.eta.team.eta.auth.AuthDtos.UserVO;

/**
 * 认证业务：注册 / 登录 / 当前用户 / 更新资料 / 改密。
 *
 * @author StarLeaf-Roxy
 * @since 2026-08-24
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public LoginResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new BizException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        User user = new User();
        user.setEmail(req.email());
        user.setNickname(req.nickname());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return buildLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.email())
                .orElseThrow(() -> new BizException(ErrorCode.EMAIL_OR_PASSWORD_ERROR));
        if (user.isDisabled()) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.EMAIL_OR_PASSWORD_ERROR);
        }
        return buildLoginResponse(user);
    }

    @Transactional(readOnly = true)
    public UserVO me(Long userId) {
        User user = requireUser(userId);
        return UserVO.from(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest req) {
        User user = requireUser(userId);
        user.setNickname(req.nickname());
        if (req.bio() != null) {
            user.setBio(req.bio());
        }
        if (req.avatar() != null) {
            user.setAvatar(req.avatar());
        }
        userRepository.save(user);
        return UserVO.from(user);
    }

    @Transactional
    public void changePassword(Long userId, AuthDtos.ChangePasswordRequest req) {
        User user = requireUser(userId);
        if (!passwordEncoder.matches(req.oldPassword(), user.getPasswordHash())) {
            throw new BizException(ErrorCode.EMAIL_OR_PASSWORD_ERROR);
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    /** logout 采用无状态 JWT，前端删除本地令牌即可；此处可扩展为令牌黑名单。 */
    public void logout() {
        // no-op：Token 服务端不保存，天然无状态登出
    }

    /** 仅供其他模块使用：解析令牌得到 userId。 */
    public Long resolveUserId(String token) {
        Claims claims = jwtService.parse(token);
        return jwtService.getUserId(claims);
    }

    private LoginResponse buildLoginResponse(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        String token = jwtService.generateToken(claims);
        return new LoginResponse(token, UserVO.from(user));
    }

    private User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
    }
}
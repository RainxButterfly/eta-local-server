// SPDX-FileCopyrightText: 2026 RainxButterfly
// SPDX-License-Identifier: AGPL-3.0-or-later
package cn.eta.team.eta.auth;

import cn.eta.team.eta.common.BizException;
import cn.eta.team.eta.common.ErrorCode;
import cn.eta.team.eta.security.JwtService;
import cn.eta.team.eta.tenant.TenantContext;
import cn.eta.team.eta.tenant.UserDatabaseInitializer;
import io.jsonwebtoken.Claims;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

import cn.eta.team.eta.auth.AuthDtos.LoginRequest;
import cn.eta.team.eta.auth.AuthDtos.LoginResponse;
import cn.eta.team.eta.auth.AuthDtos.RegisterRequest;
import cn.eta.team.eta.auth.AuthDtos.UpdateProfileRequest;
import cn.eta.team.eta.auth.AuthDtos.UserVO;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDatabaseInitializer userDatabaseInitializer;

    public AuthService(UserAccountRepository userAccountRepository,
                       UserProfileRepository userProfileRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserDatabaseInitializer userDatabaseInitializer) {
        this.userAccountRepository = userAccountRepository;
        this.userProfileRepository = userProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDatabaseInitializer = userDatabaseInitializer;
    }

    public LoginResponse register(RegisterRequest req) {
        if (userAccountRepository.existsByEmail(req.email())) {
            throw new BizException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserAccount account = new UserAccount();
        account.setEmail(req.email());
        account.setPasswordHash(passwordEncoder.encode(req.password()));
        userAccountRepository.save(account);

        userDatabaseInitializer.createDatabase(account.getId());

        UserProfile profile = new UserProfile();
        profile.setId(account.getId());
        profile.setNickname(req.nickname());

        TenantContext.set(account.getId());
        try {
            userProfileRepository.save(profile);
        } finally {
            TenantContext.clear();
        }

        return buildLoginResponse(account, profile);
    }

    public LoginResponse login(LoginRequest req) {
        UserAccount account = userAccountRepository.findByEmail(req.email())
                .orElseThrow(() -> new BizException(ErrorCode.EMAIL_OR_PASSWORD_ERROR));
        if (account.isDisabled()) {
            throw new BizException(ErrorCode.ACCOUNT_DISABLED);
        }
        if (!passwordEncoder.matches(req.password(), account.getPasswordHash())) {
            throw new BizException(ErrorCode.EMAIL_OR_PASSWORD_ERROR);
        }

        userDatabaseInitializer.ensureDatabase(account.getId());

        UserProfile profile;
        TenantContext.set(account.getId());
        try {
            profile = userProfileRepository.findById(account.getId()).orElse(null);
        } finally {
            TenantContext.clear();
        }

        return buildLoginResponse(account, profile);
    }

    public UserVO me(String userId) {
        UserAccount account = TenantContext.runAsDefault(() -> requireAccount(userId));
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        return UserVO.from(account, profile);
    }

    public UserVO updateProfile(String userId, UpdateProfileRequest req) {
        UserProfile profile = userProfileRepository.findById(userId)
                .orElseGet(() -> {
                    UserProfile p = new UserProfile();
                    p.setId(userId);
                    return p;
                });
        profile.setNickname(req.nickname());
        if (req.bio() != null) {
            profile.setBio(req.bio());
        }
        if (req.avatar() != null) {
            profile.setAvatar(req.avatar());
        }
        userProfileRepository.save(profile);

        UserAccount account = TenantContext.runAsDefault(() -> requireAccount(userId));
        return UserVO.from(account, profile);
    }

    public void changePassword(String userId, AuthDtos.ChangePasswordRequest req) {
        TenantContext.runAsDefault(() -> {
            UserAccount account = requireAccount(userId);
            if (!passwordEncoder.matches(req.oldPassword(), account.getPasswordHash())) {
                throw new BizException(ErrorCode.BAD_REQUEST, "旧密码不正确");
            }
            account.setPasswordHash(passwordEncoder.encode(req.newPassword()));
            userAccountRepository.save(account);
            return null;
        });
    }

    public void logout() {
    }

    public String resolveUserId(String token) {
        Claims claims = jwtService.parse(token);
        return jwtService.getUserId(claims);
    }

    private LoginResponse buildLoginResponse(UserAccount account, UserProfile profile) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", account.getId());
        claims.put("email", account.getEmail());
        String token = jwtService.generateToken(claims);
        return new LoginResponse(token, UserVO.from(account, profile));
    }

    private UserAccount requireAccount(String userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BizException(ErrorCode.UNAUTHORIZED));
    }
}

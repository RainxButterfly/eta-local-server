package cn.eta.team.eta.auth;

import cn.eta.team.eta.common.Result;
import cn.eta.team.eta.security.EtaPrincipal;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cn.eta.team.eta.auth.AuthDtos.ChangePasswordRequest;
import cn.eta.team.eta.auth.AuthDtos.LoginRequest;
import cn.eta.team.eta.auth.AuthDtos.LoginResponse;
import cn.eta.team.eta.auth.AuthDtos.RegisterRequest;
import cn.eta.team.eta.auth.AuthDtos.UpdateProfileRequest;
import cn.eta.team.eta.auth.AuthDtos.UserVO;

/**
 * 认证接口，路径前缀由全局 context-path(/api/v1) 提供。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 注册 */
    @PostMapping("/register")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    /** 登出（无状态，客户端删除令牌即可） */
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    /** 当前登录用户信息 */
    @GetMapping("/me")
    public Result<UserVO> me(@AuthenticationPrincipal EtaPrincipal principal) {
        return Result.ok(authService.me(principal.userId()));
    }

    /** 更新当前用户资料 */
    @PutMapping("/me")
    public Result<UserVO> updateMe(@AuthenticationPrincipal EtaPrincipal principal,
                                   @Valid @RequestBody UpdateProfileRequest req) {
        return Result.ok(authService.updateProfile(principal.userId(), req));
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@AuthenticationPrincipal EtaPrincipal principal,
                                       @Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(principal.userId(), req);
        return Result.ok();
    }
}
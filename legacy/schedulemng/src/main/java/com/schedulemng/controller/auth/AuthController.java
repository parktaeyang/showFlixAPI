package com.schedulemng.controller.auth;

import com.schedulemng.entity.User;
import com.schedulemng.repository.UserRepository;
import com.schedulemng.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/")
    public String rootRedirect(Authentication auth) {
        if (isAuthenticated(auth)) {
            return "redirect:/schedule/calendar";
        }

        return "redirect:/login";
    }

    @GetMapping("/login")
    public String loginForm(Authentication auth) {
        if (isAuthenticated(auth)) {
            return "redirect:/schedule/calendar";
        }

        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String userid,
                           @RequestParam String username,
                           @RequestParam String password,
                           @RequestParam(defaultValue = "false") boolean isAdmin) {

        userService.registerUser(userid, username, password, isAdmin);
        return "redirect:/auth/login?registered";
    }

    /**
     * 인증된 사용자인지 확인
     */
    private boolean isAuthenticated(Authentication auth) {
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
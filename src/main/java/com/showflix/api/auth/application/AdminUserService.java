package com.showflix.api.auth.application;

import com.showflix.api.auth.domain.User;
import com.showflix.api.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Application Layer - 관리자 계정관리 서비스
 */
@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void createUser(String userid, String username, String rawPassword, boolean admin) {
        if (userid == null || userid.isBlank()) {
            throw new IllegalArgumentException("아이디를 입력해주세요.");
        }
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        // 중복 아이디 체크
        userRepository.findByUserid(userid).ifPresent(u -> {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다: " + userid);
        });

        User user = new User();
        user.setUserid(userid);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setAdmin(admin);

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(String userid, String username, boolean admin) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("이름을 입력해주세요.");
        }

        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        user.setUsername(username);
        user.setAdmin(admin);

        userRepository.update(user);
    }

    @Transactional
    public void changePassword(String userid, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("비밀번호를 입력해주세요.");
        }

        userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        userRepository.updatePassword(userid, passwordEncoder.encode(rawPassword));
    }

    @Transactional
    public void deleteUser(String userid, String currentUserid) {
        if (userid.equals(currentUserid)) {
            throw new IllegalArgumentException("본인 계정은 삭제할 수 없습니다.");
        }

        userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다: " + userid));

        userRepository.delete(userid);
    }
}

package com.schedulemng.service;

import com.schedulemng.entity.User;
import com.schedulemng.repository.UserRepository;
import com.schedulemng.security.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(String userid, String username, String password, boolean isAdmin) {
        User user = new User();
        user.setUserid(userid);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    // 새로운 계정 생성 메서드 (역할 포함)
    public User createUser(String userid, String username, String phoneNumber, User.AccountType accountType, User.Role role) {
        User user = new User();
        user.setUserid(userid);
        user.setUsername(username);
        user.setPhoneNumber(phoneNumber);
        user.setAccountType(accountType);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode("showflix")); // 기본 비밀번호
        user.setAdmin(accountType == User.AccountType.ADMIN); // 관리자 계정유형이면 관리자 권한
        user.setCreatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))); // 생성일 설정
        return userRepository.save(user);
    }

    // 기존 메서드 (역할 없이)
    public User createUser(String userid, String username, String phoneNumber, User.AccountType accountType) {
        return createUser(userid, username, phoneNumber, accountType, null);
    }

    // 계정 유형에 따른 사용 가능한 역할 목록 조회
    public List<User.Role> getAvailableRoles(User.AccountType accountType) {
        switch (accountType) {
            case STAFF:
                return Arrays.asList(User.Role.DOOR, User.Role.HOLEMAN
                ,User.Role.OPER, User.Role.KITCHEN);
            case ACTOR:
                return Arrays.asList(
                    User.Role.MALE1, User.Role.MALE2, User.Role.MALE3,
                    User.Role.FEMALE1, User.Role.FEMALE2, User.Role.FEMALE3
                );
            case CAPTAIN:
            case ADMIN:
                return List.of(); // 캡틴과 관리자는 역할이 없음
            default:
                return List.of();
        }
    }

    // 모든 역할 목록 조회
    public List<User.Role> getAllRoles() {
        return Arrays.asList(User.Role.values());
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 사용자명으로 사용자 찾기
    public User findByUsername(String username) {
        log.info("Looking for user with username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElse(null);
        log.info("Found userName: {}", user.toString());
        return user;
    }

    // userid로 사용자 찾기
    public User findByUserid(String userid) {
        log.info("Looking for user with userid: {}", userid);
        User user = userRepository.findByUserid(userid)
                .orElse(null);
        log.info("Found userId: {}", user.toString());
        return user;
    }

    // 다음 아이디 생성
    public String getNextUserId(User.AccountType accountType) {
        String prefix = getPrefixByAccountType(accountType);
        String lastUserId = userRepository.findTopByAccountTypeOrderByUseridDesc(accountType);
        
        if (lastUserId == null) {
            return prefix + "0001";
        }
        
        // 마지막 번호 추출 (예: A0001 -> 1)
        String numberPart = lastUserId.substring(1);
        int nextNumber = Integer.parseInt(numberPart) + 1;
        
        // 4자리 숫자로 포맷팅
        return prefix + String.format("%04d", nextNumber);
    }

    private String getPrefixByAccountType(User.AccountType accountType) {
        switch (accountType) {
            case ACTOR: return "A";
            case STAFF: return "S";
            case CAPTAIN: return "C";
            case ADMIN: return "W";
            default: throw new IllegalArgumentException("Unknown account type: " + accountType);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userid));
        return new CustomUserDetails(user);
    }

    /**
     * 핸드폰 번호 업데이트
     */
    @Transactional
    public User updatePhoneNumber(String phoneNumber) throws IllegalStateException {
        User currentUser = getCurrentAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자 정보를 찾을 수 없습니다.\n재로그인 후 시도해주세요."));

        currentUser.setPhoneNumber(phoneNumber);
        return userRepository.save(currentUser);
    }

    /**
     * 현재 인증된 사용자 정보를 조회
     */
    private Optional<User> getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String userid = ((CustomUserDetails) authentication.getPrincipal()).getUser().getUserid();
        return userRepository.findByUserid(userid);
    }

    /**
     * 비밀번호 업데이트
     */
    @Transactional
    public User updatePassword(String currentPassword, String newPassword, String confirmNewPassword) {
        User currentUser = getCurrentAuthenticatedUser()
                .orElseThrow(() -> new IllegalStateException("현재 로그인된 사용자 정보를 찾을 수 없습니다.\n재로그인 후 시도해주세요."));

        // 현재 비밀번호 일치 검증
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        // 새 비밀번호 일치 검증
        if (!newPassword.equals(confirmNewPassword)) {
            throw new IllegalStateException("새 비밀번호가 일치하지 않습니다.");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(currentUser);
    }

    /**
     * 관리자용 비밀번호 초기화 (userid로 사용자 찾아서 비밀번호를 1234로 초기화)
     */
    @Transactional
    public User resetPasswordByUserid(String userid) {
        User user = userRepository.findByUserid(userid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userid));
        
        user.setPassword(passwordEncoder.encode("1234"));
        return userRepository.save(user);
    }
}